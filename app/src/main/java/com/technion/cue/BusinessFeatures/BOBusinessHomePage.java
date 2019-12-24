package com.technion.cue.BusinessFeatures;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.technion.cue.R;
import com.technion.cue.annotations.ModuleAuthor;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class BOBusinessHomePage extends AppCompatActivity {

    private static final int EDIT = 1;

    public void openBusinessCalendar(View view) {
        final Intent intent = new Intent(getBaseContext(),BusinessSchedule.class);
        startActivity(intent);
    }

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser() ;
    private BusinessLoader loader;

    private View fragment_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bohome_page);
        fragment_view = findViewById(R.id.business_info);
        loader = new BusinessLoader(fragment_view, db, currentUser.getUid());

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDIT) {
            if (resultCode == RESULT_OK) {

                try {
                    InputStream imageStream = getContentResolver().openInputStream(data.getData());
                    final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                    CircularImageView logo = fragment_view.findViewById(R.id.business_logo);
                    logo.setImageBitmap(selectedImage);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                TextView businessName =
                        fragment_view.findViewById(R.id.homepageBusinessName);
                TextView businessDescription =
                        fragment_view.findViewById(R.id.homepageBusinessDescription);
                businessName.setText(data.getStringExtra("businessName"));
                businessDescription.setText(data.getStringExtra("businessDescription"));
            }
        }
    }

    /**
     * onClick method for the edit button.
     * change BOBusinessHomePage into / from "edit mode",
     * where the BO will able to change the profile of his business
     */
    @ModuleAuthor("Ophir Eyal")
    public void editBOHomePage(View view) {

        Bundle bundle = new Bundle();
        // TODO: add data to bundle
        Intent i = new Intent(this, BusinessProfileEdit.class);
        i.putExtras(bundle);
        startActivityForResult(i, EDIT);

    }


}

