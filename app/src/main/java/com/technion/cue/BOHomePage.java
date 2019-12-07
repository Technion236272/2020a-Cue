package com.technion.cue;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.technion.cue.annotations.Author;

public class BOHomePage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bohome_page);
    }


    /**
     * change BOHomePage into "edit mode", where the BO will able to change his business's profile
     * @param view
     */
    @Author("Ophir Eyal")
    public void editBOHomePage(View view) {
        ImageButton imageUploadButton = this.findViewById(R.id.image_upload);
        imageUploadButton.setVisibility(View.VISIBLE);
    }
}
