package com.technion.cue.ClientFeatures;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.technion.cue.R;
import com.technion.cue.annotations.ModuleAuthor;

import java.util.HashMap;
import java.util.Map;

import static com.technion.cue.FirebaseCollections.BUSINESSES_COLLECTION;
import static com.technion.cue.FirebaseCollections.CLIENTELE_COLLECTION;
import static com.technion.cue.FirebaseCollections.CLIENTS_COLLECTION;

/*
        Still need to set all up
 */

@ModuleAuthor("Ben")
public class ClientBusinessHomepage extends AppCompatActivity {

    Bundle bundle;
    FirebaseFirestore db;
    ClientBusinessLoader businessLoader;
    Boolean favorite;
    ClientBusinessLoader f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_business_homepage);
        db = FirebaseFirestore.getInstance();

        //getting the b_id from client home page
        this.bundle = getIntent().getExtras();

        favorite=false;

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setCustomView(R.layout.client_business_homepage_actionbar);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setElevation(0);

       }

        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(this, pendingDynamicLinkData -> {
                            // Get deep link from result (may be null if no link is found)
                            if (pendingDynamicLinkData != null) {
                                FirebaseFirestore.getInstance()
                                        .collection(CLIENTS_COLLECTION)
                                        .document(FirebaseAuth.getInstance().getUid())
                                        .get()
                                        .addOnSuccessListener(documentSnapshot -> {
                                            if (documentSnapshot.exists()) {
                                                findViewById(R.id.view).setVisibility(View.VISIBLE);
                                                Uri deepLink = pendingDynamicLinkData.getLink();
                                                String formattedDeepLink = deepLink.toString()
                                                        .substring(deepLink.toString().indexOf('=') + 1)
                                                        .replace('+', ' ');
                                                findViewById(R.id.switch_to_date_time_fragments)
                                                        .setVisibility(View.VISIBLE);
                                                f = new ClientBusinessLoader(findViewById(android.R.id.content), db, formattedDeepLink);
                                                this.bundle.putString("business_id", formattedDeepLink);
                                                f.load();
                                                checkIfFavorite();
                                            } else {
                                                Toast.makeText(getBaseContext(),
                                                        "You are not a client. " +
                                                                "Please log in from a client account.",
                                                        Toast.LENGTH_SHORT)
                                                        .show();
                                                finish();
                                            }

                                        });
                            } else {
                                findViewById(R.id.view).setVisibility(View.VISIBLE);
                            }
                        });


        findViewById(R.id.switch_to_date_time_fragments).setOnClickListener(l -> {
            FirebaseFirestore.getInstance()
                    .collection(BUSINESSES_COLLECTION)
                    .document(FirebaseAuth.getInstance().getUid())
                    .collection(CLIENTELE_COLLECTION)
                    .document(FirebaseAuth.getInstance().getUid())
                    .get()
                    .addOnSuccessListener(ds -> {
                        if (ds.exists() && ds.contains("blocked") && ds.getBoolean("blocked"))
                            Toast.makeText(getBaseContext(),
                                    "This client is blocked, so you can't schedule appointments with him/her",
                                    Toast.LENGTH_SHORT).show();
                        else {
                            Intent intent = new Intent(this, EditAppointmentActivity.class);
                            intent.putExtras(this.bundle);
                            startActivity(intent);
                            findViewById(R.id.switch_to_date_time_fragments).setVisibility(View.VISIBLE);
                        }
                    });
        });

        if (bundle.containsKey("business_id") == true) {
            businessLoader = new ClientBusinessLoader(this.findViewById(R.id.view),db,bundle.getString("business_id"));
            businessLoader.load();
            checkIfFavorite();

        }


    }




    private void  checkIfFavorite() {
        if (bundle.containsKey("favorite")) { // if came from client homepage
            ((ImageButton) findViewById(R.id.favoriteStar)).setImageResource(R.drawable.ic_star_black_30dp);
            favorite=true;
        } else {                              // check on firebase
            FirebaseFirestore.getInstance().collection(CLIENTS_COLLECTION + "/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/Favorites")
                    .whereEqualTo("business_id", bundle.get("business_id"))
                    .limit(1)
                    .get()
                    .addOnSuccessListener(l -> {
                        if (!l.isEmpty()) {
                            ((ImageButton) findViewById(R.id.favoriteStar)).setImageResource(R.drawable.ic_star_black_30dp);
                            favorite = true;
                        } else {
                            ((ImageButton) findViewById(R.id.favoriteStar)).setImageResource(R.drawable.ic_star_border_grey_30dp);
                            favorite= false;
                        }
                    })
                    .addOnFailureListener(l -> {
                        ((ImageButton) findViewById(R.id.favoriteStar)).setImageResource(R.drawable.ic_star_border_grey_30dp);
                        favorite= false;
                    });

        }
    }

    public  void addOrRemoveFromFavorite(View view) { //
        findViewById(R.id.client_business_hp_progress_bar).setVisibility(View.VISIBLE);
        if (favorite == false)   {
            Map<String, Object> docData = new HashMap<>();
            docData.put("business_id", bundle.get("business_id"));
            FirebaseFirestore.getInstance()
                    .collection(CLIENTS_COLLECTION + "/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/Favorites")
                    .document()
                    .set(docData).addOnCompleteListener(task -> {
                            favorite=true;
                            ((ImageButton) findViewById(R.id.favoriteStar)).setImageResource(R.drawable.ic_star_black_30dp);

                            findViewById(R.id.client_business_hp_progress_bar).setVisibility(View.GONE);

            });
        } else { // remove

            Map<String, Object> docData = new HashMap<>();
            docData.put("business_id", bundle.get("business_id"));
            FirebaseFirestore.getInstance()
                    .collection(CLIENTS_COLLECTION + "/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/Favorites")
                    .whereEqualTo("business_id",bundle.get("business_id"))
                    .limit(1)
                    .get().addOnSuccessListener(l->{ for(DocumentSnapshot document:l.getDocuments()) {document.getReference().delete(); }}) ;
            favorite = false;
            ((ImageButton) findViewById(R.id.favoriteStar)).setImageResource(R.drawable.ic_star_border_grey_30dp);
            findViewById(R.id.client_business_hp_progress_bar).setVisibility(View.GONE);


        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()) {
            case android.R.id.home:
                if (bundle.containsKey("appointment_id")) { // back to appointment page
                    Intent intent = new Intent(this, ClientAppointmentPage.class);
                    intent.putExtras(bundle);
                    startActivity(intent);
                    finish();
                    overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                } else {
                    finish();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public void makeACall(View view) {
        String phoneNumber = businessLoader.getPhoneNumber();
        Intent mIntent = new Intent(Intent.ACTION_CALL);
        mIntent.setData(Uri.parse("tel:" + phoneNumber));

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CALL_PHONE},
                    0);

        } else {
            //You already have permission
            try {
                startActivity(mIntent);
            } catch(SecurityException e) {
                e.printStackTrace();
            }
        }

    }

    public void makeAMap(View view) {
        String location = businessLoader.getLocation();
        Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + location);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }

}
