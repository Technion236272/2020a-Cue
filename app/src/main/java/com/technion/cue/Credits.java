package com.technion.cue;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.technion.cue.annotations.ModuleAuthor;

@ModuleAuthor("Topaz")
public class Credits extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credits);
    }
}
