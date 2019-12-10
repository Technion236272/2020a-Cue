package com.technion.cue;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.technion.cue.annotations.Author;

public class BOHomePage extends AppCompatActivity {

    // inner enum, designating current mode of the activity
    private enum Mode { EDIT, READ }

    private static final int GET_LOGO = 0;
    private Mode mode = Mode.READ;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser() ;
    private Business bo_data = new Business();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bohome_page);
        loadDataFromFirebase();
    }

    @Override
    @Author("Ophir Eyal")
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        if (reqCode == GET_LOGO) {
            Log.v(this.toString(), "success");
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            Uri imageUri = data.getData();

            // TODO: perhaps set image business_name as business u_id from the FireBase database?
            final StorageReference logosRef = storageRef.child("business_logos/" +
                    "wpD3ST4T9dLhlz09SbpB"); // TODO: change this to currentUser.getUid()
            UploadTask uploadTask = logosRef.putFile(imageUri);

            // Register observers to listen for when the download is done or if it fails
            StorageTask<UploadTask.TaskSnapshot> taskSnapshotStorageTask =
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Log.v(this.toString(), "failure to upload business logo");
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Log.v(this.toString(), "success to upload business logo");
                            bo_data.logo_path = logosRef.getPath();
                            BOHomePage.this.loadLogoFromFireBase();
                        }
                    });
        }
    }

    /**
     * loads data from Firebase into matching fields in the BO homepage activity
     */
    @Author("Ophir Eyal")
    private void loadDataFromFirebase() {
        final DocumentReference bo_doc = db.collection("Businesses")
                .document("wpD3ST4T9dLhlz09SbpB"); // TODO: change to currentUser.getUid()
        bo_doc.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                // TODO: see if we should initialize bo_data once / each time we call the method
                bo_data = documentSnapshot.toObject(Business.class);
                loadFieldsFromFirebase();
                loadLogoFromFireBase();
            }
        });
    }

    /**
     * loads fields from Firebase into BO object
     */
    @Author("Ophir Eyal")
    private void loadFieldsFromFirebase() {
        TextView name = findViewById(R.id.business_name);
        TextView desc = findViewById(R.id.business_description);
        name.setText(bo_data.business_name);
        desc.setText(bo_data.description);
    }


    /**
     * change BOHomePage into "edit mode", where the BO will able to change his business's profile
     * @param view
     */
    @Author("Ophir Eyal")
    public void editBOHomePage(View view) {

        switch (mode) {
            case READ:
                findViewById(R.id.image_upload).setVisibility(View.VISIBLE);
                final TextView bo_name = findViewById(R.id.business_name);
                final TextView bo_desc = findViewById(R.id.business_description);
                bo_name.setVisibility(View.INVISIBLE);
                bo_desc.setVisibility(View.INVISIBLE);
                final EditText bo_name_edit = findViewById(R.id.business_name_edit);
                final EditText bo_desc_edit = findViewById(R.id.business_description_edit);
                bo_name_edit.setText(bo_name.getText());
                bo_desc_edit.setText(bo_desc.getText());
                bo_name_edit.setVisibility(View.VISIBLE);
                bo_desc_edit.setVisibility(View.VISIBLE);
                mode = Mode.EDIT;
                break;
            case EDIT:
                uploadBusinessData();
                findViewById(R.id.image_upload).setVisibility(View.INVISIBLE);
                findViewById(R.id.business_name).setVisibility(View.VISIBLE);
                findViewById(R.id.business_description).setVisibility(View.VISIBLE);
                findViewById(R.id.business_name_edit).setVisibility(View.INVISIBLE);
                findViewById(R.id.business_description_edit).setVisibility(View.INVISIBLE);
                mode = Mode.READ;
                break;
        }
    }

    /**
     * uploads business data (after changes) to Firebase database
     */
    @Author("Ophir Eyal")
    private void uploadBusinessData() {

        final EditText name_edit = findViewById(R.id.business_name_edit);
        final EditText desc_edit = findViewById(R.id.business_description_edit);
        final DocumentReference bo_doc = db.collection("Businesses")
                .document("wpD3ST4T9dLhlz09SbpB"); // TODO: change to currentUser.getUid()
        bo_doc.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Business bo = documentSnapshot.toObject(Business.class);
                        bo.business_name = name_edit.getText().toString();
                        bo.description = desc_edit.getText().toString();
                        bo_doc.set(bo);
                        TextView name = findViewById(R.id.business_name);
                        TextView desc = findViewById(R.id.business_description);
                        name.setText(bo.business_name);
                        desc.setText(bo.description);
                    }
                });
    }

    /**
     * opens up the phone's gallery for picture upload
     * TODO: consider moving this into it's own class
     * @param view
     */
    @Author("Ophir Eyal")
    public void uploadLogoToFireBase(View view) {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, GET_LOGO);
    }

    /**
     * loads logo from fireBase into the "business_logo" ImageView
     * uses the Glide framework for image download & processing
     */
    @Author("Ophir Eyal")
    private void loadLogoFromFireBase() {
        CircularImageView logo = findViewById(R.id.business_logo);
        StorageReference logoRef = FirebaseStorage.getInstance().getReference()
                .child(bo_data.logo_path.substring(bo_data.logo_path.indexOf("business_logos")));
        Glide.with(logo.getContext())
                .load(logoRef)
                .error(R.drawable.ic_person_outline_black_24dp)
                .diskCacheStrategy(DiskCacheStrategy.NONE )
                .skipMemoryCache(true)
                .into(logo);
    }
}

