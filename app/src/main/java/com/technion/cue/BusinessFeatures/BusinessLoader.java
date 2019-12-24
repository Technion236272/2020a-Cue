package com.technion.cue.BusinessFeatures;

import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.technion.cue.R;
import com.technion.cue.annotations.ModuleAuthor;
import com.technion.cue.data_classes.Business;

import static com.technion.cue.FirebaseCollections.BUSINESSES_COLLECTION;

class BusinessLoader {

    private View view;
    private FirebaseFirestore db;
    private Business business;
    private String business_id;

    BusinessLoader(FirebaseFirestore db, String business_to_fetch) {
        this.db = db;
        this.business_id = business_to_fetch;
//        businessLoadTask = db.collection(BUSINESSES_COLLECTION)
//                .document(business_id)
//                .get()
//                .addOnSuccessListener(documentSnapshot ->
//                        business = documentSnapshot.toObject(Business.class));
    }

    BusinessLoader(View view, FirebaseFirestore db, String business_to_fetch) {
        this(db, business_to_fetch);
        this.view = view;
    }

    /**
     * loads data from Firebase into matching fields in the BO homepage activity
     */
    @ModuleAuthor("Ophir Eyal")
    void load() {
        db.collection(BUSINESSES_COLLECTION)
                .document(business_id)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                            business = documentSnapshot.toObject(Business.class);
                            loadBusinessData();
                            loadLogoFromFireBase();
                });
    }


    private String getLogoPath() {
        return business.logo_path.substring(business.logo_path.indexOf("business_logos"));
    }

    /**
     * loads fields from Firebase into BO object
     */
    @ModuleAuthor("Ophir Eyal")
    private void loadBusinessData() {

        TextView name = view.findViewById(R.id.homepageBusinessName);
        TextView desc = view.findViewById(R.id.homepageBusinessDescription);
        name.setText(business.business_name);
        desc.setText(business.description);
    }

    /**
     * loads logo from fireBase into the "business_logo" ImageView
     * uses the Glide framework for image download & processing
     */
    @ModuleAuthor("Ophir Eyal")
    private void loadLogoFromFireBase()
    {
        CircularImageView logo = view.findViewById(R.id.business_logo);
        StorageReference logoRef = null;
        if (business != null && !business.logo_path.equals("")) { // ben - adding checking if null
            logoRef = FirebaseStorage.getInstance().getReference().child(getLogoPath());
        }
        Glide.with(logo.getContext())
                .load(logoRef)
                .error(R.drawable.ic_person_outline_black_24dp)
                .into(logo);
    }
}
