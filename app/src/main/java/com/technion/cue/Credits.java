package com.technion.cue;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.technion.cue.annotations.ModuleAuthor;

/** TODO : NEED TO CHANGE TO SIMPLE DIALOG INSTAD OF ACTIVITY */
//                new MaterialAlertDialogBuilder(getContext())
//                        .setTitle("Our App Credits ")
//                        .setMessage("\nLOPEZ Mikhael\nIcon made by Freepik from www.flaticon.com")
//                        .setPositiveButton("Ok", null)
//                        .show();





@ModuleAuthor("Topaz")
public class Credits extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credits);
    }
}
