package com.technion.cue.ClientFeatures;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.technion.cue.R;
import com.technion.cue.data_classes.Appointment;
import com.technion.cue.data_classes.Business;

import java.util.Date;

import static com.technion.cue.FirebaseCollections.APPOINTMENTS_COLLECTION;
import static com.technion.cue.FirebaseCollections.BUSINESSES_COLLECTION;
import static com.technion.cue.FirebaseCollections.REVIEWS_COLLECTION;


public class ClientHomePage extends AppCompatActivity  {


    FirebaseFirestore db;
    FirebaseAuth mAuth;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_home_page);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();


        TabLayout tabLayout = findViewById(R.id.tabLayout);

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

        /** ----- */

        ViewPager viewPager = findViewById(R.id.pagerww);
        ClientPagerAdapter adapter = new ClientPagerAdapter(getSupportFragmentManager(),tabLayout.getTabCount());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        Bundle b = getIntent().getExtras();
        if (b != null && b.containsKey("year")) {
            Fragment bsd = new ClientAppointmentsPerDayFragment();
            b.putBoolean("useBack", false);
            bsd.setArguments(b);
            findViewById(R.id.day_fragment).setVisibility(View.VISIBLE);
            findViewById(R.id.container).setVisibility(View.GONE);
            findViewById(R.id.tabLayout).setVisibility(View.GONE);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.day_fragment, bsd)
                    .addToBackStack(null)
                    .commit();
        }



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
                                if ((appointmentReview.date.compareTo(currentTime)) < 0) {

                                    if (appointmentReview.askedForReview == false) {
                                        final View view = getLayoutInflater().inflate(R.layout.review_dialog_layout, null);

                                        MaterialAlertDialogBuilder alertDialog = new MaterialAlertDialogBuilder(this);

                                        alertDialog.setCancelable(false);

                                        final EditText etComments = view.findViewById(R.id.etComments);
                                        final TextView etComments_text = view.findViewById(R.id.etComments_title);
                                        etComments_text.setText("Tell us how was your experience with " + b.business_name + ".");
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


        checkForReview();
    }

    @Override
    protected void onResume() {
        super.onResume();



    }



    public void Settings(View v) {

        Intent getIntentBOPage = new Intent(getBaseContext(), ClientSettingsActivity.class);
        startActivity(getIntentBOPage);

    }

    public void showBusinesses(View v) {
        ClientBusinessList fragment = new ClientBusinessList();
        // R.id.container - the id of a view that will hold your fragment; usually a FrameLayout
        getSupportFragmentManager().beginTransaction()
                .addToBackStack("BLF")
                .add(android.R.id.content,fragment,"BLF")
                .setCustomAnimations(android.R.anim.slide_in_left,android.R.anim.slide_out_right)
                .commit();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.client_homepage_top_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setCustomView(R.layout.client_homepage_action_bar);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        switch (item.getItemId()) {
            case android.R.id.home:
                getSupportFragmentManager().popBackStack();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onBackPressed() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setCustomView(R.layout.client_homepage_action_bar);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        int count = getSupportFragmentManager().getBackStackEntryCount();
        if (count == 0) {
            super.onBackPressed();
        } else {
            finish();
        }
    }
}