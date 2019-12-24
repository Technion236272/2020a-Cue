package com.technion.cue.BusinessFeatures;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.technion.cue.R;
import com.technion.cue.annotations.ModuleAuthor;
import com.technion.cue.data_classes.Business;

import java.io.FileNotFoundException;
import java.io.InputStream;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.technion.cue.FirebaseCollections.BUSINESSES_COLLECTION;

@ModuleAuthor("Ophir Eyal")
public class BusinessProfileEdit extends AppCompatActivity {

    private static final int GET_LOGO = 0;
    private BusinessUploader uploader;
    Uri logoData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_profile_edit);
        FirebaseFirestore.getInstance()
                .collection(BUSINESSES_COLLECTION)
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(ds -> {
                    Business business = ds.toObject(Business.class);
                    TextInputEditText businessName = findViewById(R.id.businessNameEditText);
                    TextInputEditText businessDescription = findViewById(R.id.businessDescriptionEditText);
                    businessName.setText(business.business_name);
                    businessDescription.setText(business.description);
                    uploader = new BusinessUploader(business,
                            findViewById(R.id.businessLogoEdit));
                    uploader.loadLogo();
                });
    }

    /**
     * opens up the phone's gallery for picture upload
     */
    public void uploadLogoToFireBase(View view) {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, GET_LOGO);
    }

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        if (reqCode == GET_LOGO && data != null) {
            logoData = data.getData();
            final InputStream imageStream;
            try {
                imageStream = getContentResolver().openInputStream(logoData);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                CircularImageView logo = findViewById(R.id.businessLogoEdit);
                logo.setImageBitmap(selectedImage);
            } catch (FileNotFoundException e) {
                Toast.makeText(this, "image not found", Toast.LENGTH_SHORT).show();
            }

        }
    }

    public void saveChanges(View view) {
        TextInputEditText businessName = findViewById(R.id.businessNameEditText);
        TextInputEditText businessDescription = findViewById(R.id.businessDescriptionEditText);
        uploader.business.business_name = businessName.getText().toString();
        uploader.business.description = businessDescription.getText().toString();
        uploader.uploadLogo(logoData);
        uploader.updateBusiness();
        Intent data = new Intent();
        data.setData(logoData);
        setResult(RESULT_OK, data);
        data.putExtra("businessName", businessName.getText().toString());
        data.putExtra("businessDescription", businessDescription.getText().toString());
        finish();
    }
}
