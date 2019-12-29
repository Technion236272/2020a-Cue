package com.technion.cue.BusinessFeatures;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.technion.cue.R;
import com.technion.cue.annotations.ModuleAuthor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static android.content.Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION;
import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;

@ModuleAuthor("Topaz")
public class BOSignUp2 extends AppCompatActivity {
    private static final int GET_LOGO = 0;
    private Uri logoData;
    private Button btn_next;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bo_sign_up2);

        String email = getIntent().getExtras().getString("email");
        String password = getIntent().getExtras().getString("password");
        String bo_name = getIntent().getExtras().getString("boName");

        btn_next = findViewById(R.id.btn_next2);
        TextInputEditText business_name = findViewById(R.id.businessNameEditText);
        TextInputEditText business_desc = findViewById(R.id.businessDescriptionEditText);
        TextInputEditText business_phone = findViewById(R.id.businessPhoneEditText);

        btn_next.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                btn_next.setEnabled(false);
                if(inputIsValid()){
                    Intent in = new Intent(getBaseContext(), BOSignUp3.class);
                    in.putExtra("bName",business_name.getText().toString());
                    in.putExtra("bDesc",business_desc.getText().toString());
                    in.putExtra("bPhone",business_phone.getText().toString());
                    in.putExtra("email",email);
                    in.putExtra("password",password);
                    in.putExtra("boName",bo_name);
                    //in.putExtra("logoData",logoData);
                    startActivity(in);
                    btn_next.setEnabled(true);
                    finish();
                }
                btn_next.setEnabled(true);
            }
        });
    }

    private boolean inputIsValid() {
        TextInputEditText business_name = findViewById(R.id.businessNameEditText);
        TextInputEditText business_desc = findViewById(R.id.businessDescriptionEditText);
        TextInputEditText business_phone = findViewById(R.id.businessPhoneEditText);
        String bName = business_name.getText().toString();
        String bDesc = business_desc.getText().toString();
        String bPhone = business_phone.getText().toString();
        if (inputNotEmpty(bName, bDesc, bPhone)) {
//            if (logoData == null) {
//                Toast.makeText(this, "please upload logo", Toast.LENGTH_SHORT).show();
//                return false;
//            }
            return true;
        }
        return false;
    }

    private boolean inputNotEmpty(String bName, String bDesc, String bPhone){
        if(bName.isEmpty() || bDesc.isEmpty() || bPhone.isEmpty()){
            return false;
        }
        return true;
    }


    public void openImageGallery(View view) {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        if (photoPickerIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(Intent.createChooser(photoPickerIntent, "Select File"), GET_LOGO);
        }

//        startActivityForResult(photoPickerIntent, GET_LOGO);
    }

    /**
     * Set the displayed logo to the one chosen from the image gallery in
     * {@link #openImageGallery(View view)}
     * @param reqCode
     * @param resultCode
     * @param data
     */
//    @Override
//    public void onActivityResult(int reqCode, int resultCode, Intent data) {
//        super.onActivityResult(reqCode, resultCode, data);
//        if (reqCode == GET_LOGO && data != null) {
//            logoData = data.getData();
//            final InputStream imageStream;
//            try {
//                imageStream = getContentResolver().openInputStream(logoData);
//                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
//                CircularImageView logo = findViewById(R.id.businessLogoEdit);
//                logo.setImageBitmap(selectedImage);
//                FileOutputStream outStream = new FileOutputStream(new File(getCacheDir(), "tempBMP"));
//                selectedImage.compress(Bitmap.CompressFormat.JPEG, 75, outStream);
//                outStream.close();
//            } catch (FileNotFoundException e) {
//                Toast.makeText(this, "image not found", Toast.LENGTH_SHORT).show();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//        }
//    }


}