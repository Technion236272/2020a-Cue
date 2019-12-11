package com.technion.cue;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.technion.cue.annotations.Author;

public class BOBusinessHomePage extends AppCompatActivity {

    // inner enum, designating current mode of the activity
    private enum Mode { EDIT, READ }
    private Mode mode = Mode.READ;
    private static final int GET_LOGO = 0;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser() ;
    private BusinessInfoLoader loader;

    private View fragment_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bohome_page);
        fragment_view = findViewById(R.id.business_info);
        loader = new BusinessInfoLoader(fragment_view, db, currentUser);
        loader.loadDataFromFB();
    }


    /**
     * onClick method for the edit button.
     * change BOBusinessHomePage into / from "edit mode",
     * where the BO will able to change his business's profile
     * @param view
     */
    @Author("Ophir Eyal")
    public void editBOHomePage(View view) {

        switch (mode) {
            case READ:
                findViewById(R.id.image_upload).setVisibility(View.VISIBLE);
                final TextView bo_name = fragment_view.findViewById(R.id.business_name);
                final TextView bo_desc = fragment_view.findViewById(R.id.business_description);
                bo_name.setVisibility(View.INVISIBLE);
                bo_desc.setVisibility(View.INVISIBLE);
                final EditText bo_name_edit = fragment_view.findViewById(R.id.business_name_edit);
                final EditText bo_desc_edit = fragment_view.findViewById(R.id.business_description_edit);
                bo_name_edit.setText(bo_name.getText());
                bo_desc_edit.setText(bo_desc.getText());
                bo_name_edit.setVisibility(View.VISIBLE);
                bo_desc_edit.setVisibility(View.VISIBLE);
                mode = BOBusinessHomePage.Mode.EDIT;
                break;
            case EDIT:
                loader.uploadBusinessFromFB();
                findViewById(R.id.image_upload).setVisibility(View.INVISIBLE);
                fragment_view.findViewById(R.id.business_name).setVisibility(View.VISIBLE);
                fragment_view.findViewById(R.id.business_description).setVisibility(View.VISIBLE);
                fragment_view.findViewById(R.id.business_name_edit).setVisibility(View.INVISIBLE);
                fragment_view.findViewById(R.id.business_description_edit).setVisibility(View.INVISIBLE);
                mode = BOBusinessHomePage.Mode.READ;
                break;
        }
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

    @Override
    @Author("Ophir Eyal")
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        if (reqCode == GET_LOGO) {
            loader.uploadLogoToFB(data);
        }
    }
}

