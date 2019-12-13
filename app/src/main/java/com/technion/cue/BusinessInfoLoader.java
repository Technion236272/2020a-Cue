package com.technion.cue;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.technion.cue.annotations.ModuleAuthor;
import com.technion.cue.data_classes.Business;

import java.util.Objects;
import java.util.Random;

import static com.technion.cue.FirebaseCollections.BUSINESSES_COLLECTION;

class BusinessInfoLoader {

    private View view;
    private FirebaseFirestore db;
    private Business business;
    private FirebaseUser currentUser;
    private Task businessLoadTask;

    BusinessInfoLoader(View view, FirebaseFirestore db, FirebaseUser currentUser) {
        this.view = view;
        this.db = db;
        this.currentUser = currentUser;
        businessLoadTask = db.collection(BUSINESSES_COLLECTION)
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot ->
                        business = documentSnapshot.toObject(Business.class));
    }

    /**
     * loads data from Firebase into matching fields in the BO homepage activity
     */
    @ModuleAuthor("Ophir Eyal")
    void loadDataFromFB() {
        Tasks.whenAll(businessLoadTask).addOnCompleteListener(task -> {
            loadInfoFromFB();
            loadLogoFromFireBase();
        });
    }

    @ModuleAuthor("Ophir Eyal")
    void uploadLogoToFB(Intent data) {

        Tasks.whenAll(businessLoadTask).addOnCompleteListener(task -> {
            final StorageReference logosRef = FirebaseStorage.getInstance().getReference()
                    .child("business_logos/" + currentUser.getUid() + new Random().nextInt());
            UploadTask uploadTask = logosRef.putFile(Objects.requireNonNull(data.getData()));

            // Register observers to listen for when the download is done or if it fails
            uploadTask.addOnFailureListener(exception -> {
                Log.d(this.toString(), "failed to upload business logo");
            }).addOnSuccessListener(taskSnapshot ->  {
                Log.d(this.toString(), "succeeded to upload business logo");

                // deleting previous logo
                FirebaseStorage.getInstance()
                        .getReference()
                        .child(getLogoPath())
                        .delete().addOnSuccessListener(l -> {
                            Log.d(this.toString(), "succeeded to delete previous logo");
                        });
                business.logo_path = logosRef.getPath();
                updateBusiness();
                loadLogoFromFireBase();
            });
        });
    }

    private void updateBusiness() {
        final DocumentReference bo_doc =
                db.collection(BUSINESSES_COLLECTION).document(currentUser.getUid());
        bo_doc.get().addOnSuccessListener(documentSnapshot -> bo_doc.set(business));
    }

    private String getLogoPath() {
        return business.logo_path.substring(business.logo_path.indexOf("business_logos"));
    }

    /**
     * loads fields from Firebase into BO object
     */
    @ModuleAuthor("Ophir Eyal")
    private void loadInfoFromFB() {

        Tasks.whenAll(businessLoadTask).addOnCompleteListener(task -> {
            TextView name = view.findViewById(R.id.business_name);
            TextView desc = view.findViewById(R.id.business_description);
            name.setText(business.business_name);
            desc.setText(business.description);
        });

    }

    /**
     * loads logo from fireBase into the "business_logo" ImageView
     * uses the Glide framework for image download & processing
     */
    @ModuleAuthor("Ophir Eyal")
    private void loadLogoFromFireBase()
    {
        Tasks.whenAll(businessLoadTask).addOnCompleteListener(task -> {
            CircularImageView logo = view.findViewById(R.id.business_logo);
            StorageReference logoRef = null;
            if (!business.logo_path.equals("")) {
                logoRef = FirebaseStorage.getInstance().getReference().child(getLogoPath());
            }
            Glide.with(logo.getContext())
                    .load(logoRef)
                    .error(R.drawable.ic_person_outline_black_24dp)
                    .into(logo);
        });
    }

    /**
     * uploads business data (after changes) to Firebase database
     */
    @ModuleAuthor("Ophir Eyal")
    void uploadBusinessFromFB() {
        DocumentReference bo_doc =
                db.collection(BUSINESSES_COLLECTION).document(currentUser.getUid());
        bo_doc.get().addOnSuccessListener(documentSnapshot -> {
            final EditText name_edit = view.findViewById(R.id.business_name_edit);
            final EditText desc_edit = view.findViewById(R.id.business_description_edit);
            Business bo = documentSnapshot.toObject(Business.class);
            bo.business_name = name_edit.getText().toString();
            bo.description = desc_edit.getText().toString();
            bo_doc.set(bo);
            TextView name = view.findViewById(R.id.business_name);
            TextView desc = view.findViewById(R.id.business_description);
            name.setText(bo.business_name);
            desc.setText(bo.description);
        });
    }
}
