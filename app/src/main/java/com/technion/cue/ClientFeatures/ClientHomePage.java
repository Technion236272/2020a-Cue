package com.technion.cue.ClientFeatures;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.viewpager.widget.ViewPager;


import com.google.android.material.tabs.TabLayout;

import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.technion.cue.FirebaseCollections;
import com.technion.cue.R;

import static com.technion.cue.FirebaseCollections.CLIENTS_COLLECTION;


public class ClientHomePage extends AppCompatActivity  {



    FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_home_page);

        mAuth = FirebaseAuth.getInstance();
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.addTab(tabLayout.newTab().setText("Home"));
        tabLayout.addTab(tabLayout.newTab().setText("Calendar"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        /*
         Addition by Ophir on 10/1
         */

        FirebaseMessaging
                .getInstance()
                .subscribeToTopic(FirebaseAuth.getInstance().getUid());



        /** Set Action Bar */
        ActionBar actionBar = getSupportActionBar();
        actionBar.setCustomView(R.layout.client_homepage_action_bar);

        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
//        actionBar.setDisplayShowHomeEnabled(false);
        //actionBar.setTitle("My Appointments");


        /** ----- */




        ViewPager viewPager = findViewById(R.id.pagerww);
        ClientPagerAdapter adapter = new ClientPagerAdapter(getSupportFragmentManager(),tabLayout.getTabCount());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));



        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {


            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }


        });


    }


    public void Settings(View v) {
        Intent getIntentBOPage = new Intent(getBaseContext(), ClientSettingsActivity.class);
       startActivity(getIntentBOPage);
        overridePendingTransition(R.anim.animation_left_to_right,0);
    }

    public void showBusinesses(View v) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here

        return super.onOptionsItemSelected(item);
    }


    // - menu - ben 17.12
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.client_homepage_top_menu, menu);
        return true;
    }





}