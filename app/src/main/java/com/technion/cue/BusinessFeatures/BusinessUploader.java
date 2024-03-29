package com.technion.cue.BusinessFeatures;

import android.net.Uri;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.technion.cue.R;
import com.technion.cue.annotations.ModuleAuthor;
import com.technion.cue.data_classes.Business;

import java.util.Random;

import static com.technion.cue.FirebaseCollections.BUSINESSES_COLLECTION;

/**
 * This class is used to upload data about the business to Firestore
 */
@ModuleAuthor("Ophir Eyal")
class BusinessUploader {

    private final String business_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private final Uri logoRefUri;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CircularImageView logoResource;
    private Task uploadTask;

    Business business;

    BusinessUploader(Business business, Uri logoRef, CircularImageView logoResource) {
        this.business = business;
        this.logoResource = logoResource;
        this.logoRefUri = logoRef;
        Glide.with(logoResource.getContext())
                .load(logoRef)
                .error(R.drawable.person_icon)
                .into(logoResource);
    }

    private String getLogoPath() {
        return business.logo_path.substring(business.logo_path.indexOf("business_logos"));
    }

    /**
     * Updates business data on Firestore
     */
    void updateBusiness() {
        if (uploadTask == null) {
            db.collection(BUSINESSES_COLLECTION)
                    .document(business_id)
                    .set(business);
        } else {
            Tasks.whenAll(uploadTask).addOnSuccessListener(sl -> {
                db.collection(BUSINESSES_COLLECTION)
                        .document(business_id)
                        .set(business);
            });
        }
    }

    /**
     * Uploads business logo to Firebase Storage
     * @param data: the uri of the business'es logo
     */
    void uploadLogo(Uri data) {

        if (data == null)
            return;

        final StorageReference logosRef = FirebaseStorage.getInstance().getReference()
                .child("business_logos/" + business_id + new Random().nextInt());
        UploadTask uploadTask = logosRef.putFile(data);

        // Register observers to listen for when the download is done or if it fails
        this.uploadTask = uploadTask.addOnSuccessListener(taskSnapshot ->  {
            Log.d(this.toString(), "succeeded to upload business logo");

            // deleting previous logo
            if (business.logo_path.contains("business_logos")) {
                FirebaseStorage.getInstance()
                        .getReference()
                        .child(getLogoPath())
                        .delete().addOnSuccessListener(l ->
                        Log.d(this.toString(), "succeeded to delete previous logo"));
            }

            business.logo_path = logosRef.getPath();
        });
    }

    /**
     * supporting method for loading business logo from Firebase Storage
     */
    void loadLogo() {

        if (logoRefUri != null)
            return;

        StorageReference logoRef = null;
        if (business != null && !business.logo_path.equals("")) {
            logoRef = FirebaseStorage.getInstance().getReference().child(getLogoPath());
        }
        Glide.with(logoResource.getContext())
                .load(logoRef)
                .error(R.drawable.person_icon)
                .into(logoResource);
    }

}
