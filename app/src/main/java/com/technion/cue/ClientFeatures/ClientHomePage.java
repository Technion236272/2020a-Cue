package com.technion.cue.ClientFeatures;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import android.content.DialogInterface;
import android.content.Intent;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.viewpager.widget.ViewPager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.technion.cue.LauncherActivity;

import com.google.firebase.messaging.FirebaseMessaging;
import com.technion.cue.R;
import com.technion.cue.data_classes.Appointment;
import com.technion.cue.data_classes.Business;

import java.util.Calendar;
import java.util.Date;

import static com.technion.cue.FirebaseCollections.APPOINTMENTS_COLLECTION;
import static com.technion.cue.FirebaseCollections.BUSINESSES_COLLECTION;
import static com.technion.cue.FirebaseCollections.REVIEWS_COLLECTION;


public class ClientHomePage extends AppCompatActivity {


    FirebaseFirestore db;
    FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_home_page);

        db = FirebaseFirestore.getInstance();
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

    public void checkForReview() {

        db.collection(APPOINTMENTS_COLLECTION)
                .whereEqualTo("client_id",mAuth.getUid())
                .whereEqualTo("askedForReview",false)
                .get().addOnCompleteListener(l -> {
            for (DocumentSnapshot document : l.getResult().getDocuments()) {
                if ((document.exists())) {
                     db.collection(BUSINESSES_COLLECTION).document(document.get("business_id").toString()).get().addOnSuccessListener(businessDoc->{
                            if (businessDoc.exists()) {
                                Business b = businessDoc.toObject(Business.class);
                                Appointment appointmentReview = document.toObject(Appointment.class);
                                Date currentTime = new Date();
                                if ((appointmentReview.date.compareTo(currentTime)) < 0) {// TODO : need to update

                                    if (appointmentReview.askedForReview == false) {
                                        final View view = getLayoutInflater().inflate(R.layout.review_dialog_layout, null);

                                        MaterialAlertDialogBuilder alertDialog = new MaterialAlertDialogBuilder(this);

                                        alertDialog.setCancelable(false);

                                        final EditText etComments = (EditText) view.findViewById(R.id.etComments);
                                        final TextView etComments_text = (TextView) view.findViewById(R.id.etComments_title);
                                        etComments_text.setText("Review About " + b.business_name);
                                        alertDialog.setPositiveButton("Send", (t, t2) -> {
                                            if (!etComments.getText().equals("")) {
                                                // add a review
                                                db.collection(BUSINESSES_COLLECTION + "/" + appointmentReview.business_id + "/" + REVIEWS_COLLECTION)
                                                        .document().set(new Business.Review(mAuth.getUid(), etComments.getText().toString(), Timestamp.now()));

                                                // update "askedForReview to TRUE

                                                appointmentReview.askedForReview = true;
                                                String id = document.getId();
                                                db.collection(APPOINTMENTS_COLLECTION).document(id).set(appointmentReview); //Set student object

                                            }
                                        });

                                        alertDialog.setNegativeButton("Skip", (t, t2) -> {
                                            // update "askedForReview to TRUE
                                            appointmentReview.askedForReview = true;
                                            String id = document.getId();
                                            db.collection(APPOINTMENTS_COLLECTION).document(id).set(appointmentReview); //Set student object
                                        });


                                        alertDialog.setView(view);
                                        alertDialog.show();
                                    }
                            }

                            }

                     });


                }
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        /**
         * Check if user is signin
         * */
        if (mAuth.getCurrentUser() == null) {
            Intent getIntentBOPage = new Intent(getBaseContext(), LauncherActivity.class);
            startActivity(getIntentBOPage);
            finish();
        }


        checkForReview();
    }

    @Override
    protected void onResume() {
        super.onResume();

        /**
         * Check if user is signin
         * */
        if (mAuth.getCurrentUser() == null) {
            Intent getIntentBOPage = new Intent(getBaseContext(), LauncherActivity.class);
            startActivity(getIntentBOPage);
            finish();
        }
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