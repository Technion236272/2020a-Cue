package com.technion.cue;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.technion.cue.annotations.Author;

public class BOHomePage extends AppCompatActivity {

    private static final int GET_LOGO = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bohome_page);

        loadLogoFromFireBase();
    }


    /**
     * change BOHomePage into "edit mode", where the BO will able to change his business's profile
     * @param view
     */
    @Author("Ophir Eyal")
    public void editBOHomePage(View view) {
        ImageButton imageUploadButton = findViewById(R.id.image_upload);
        imageUploadButton.setVisibility(View.VISIBLE);
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

            // TODO: perhaps set image name as business u_id from the FireBase database?
            final StorageReference logosRef = storageRef.child("business_logos/" +
                    ((TextView) findViewById(R.id.business_name)).getText());
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
                    BOHomePage.this.loadLogoFromFireBase();
                }
            });
        }
    }

    @Author("Ophir Eyal")
    public void uploadLogoToFireBase(View view) {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, GET_LOGO);
    }

    @Author("Ophir Eyal")
    private void loadLogoFromFireBase() {
        ImageView logo = findViewById(R.id.business_logo);
        StorageReference logoRef = FirebaseStorage.getInstance().getReference()
                .child("business_logos/" + ((TextView) findViewById(R.id.business_name)).getText());
        Glide.with(logo.getContext())
                .load(logoRef)
                .error(R.drawable.ic_person_outline_black_24dp)
                .diskCacheStrategy(DiskCacheStrategy.NONE )
                .skipMemoryCache(true)
                .into(logo);
    }
}

