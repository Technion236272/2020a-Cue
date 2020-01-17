package com.technion.cue.ClientFeatures;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;

import android.content.pm.PackageManager;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.technion.cue.BusinessFeatures.BusinessInfoFragment;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_business_homepage);
        db = FirebaseFirestore.getInstance();
        //getting the b_id from client home page
        this.bundle = getIntent().getExtras();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setElevation(0);
            // set action bar color colorPrimaryDark
        //   actionBar.setBackgroundDrawable(new ColorDrawable(Color.rgb(0, 121, 107)));


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
                                        Uri deepLink = pendingDynamicLinkData.getLink();
                                        String formattedDeepLink = deepLink.toString()
                                                .substring(deepLink.toString().indexOf('=') + 1)
                                                .replace('+', ' ');
                                        findViewById(R.id.switch_to_date_time_fragments)
                                                .setVisibility(View.VISIBLE);
                                        Fragment f = new BusinessInfoFragment();
                                        bundle.putString("business_id", formattedDeepLink);
                                        f.setArguments(bundle);
                                        getSupportFragmentManager()
                                                .beginTransaction()
                                                .replace(R.id.fragment_holder_business_client, f)
                                                .commit();
                                    } else {
                                        Toast.makeText(getBaseContext(),
                                                "You are not a client. " +
                                                        "Please log in from a client account.",
                                                Toast.LENGTH_SHORT)
                                                .show();
                                        finish();
                                    }
                                });
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
            // --- //
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
                        ((ImageButton) findViewById(R.id.favoriteStar)).setImageResource(R.drawable.ic_star_black_30dp);
                        favorite=true;
                    })
                    .addOnFailureListener(l -> {
                        ((ImageButton) findViewById(R.id.favoriteStar)).setImageResource(R.drawable.ic_star_border_grey_30dp);
                        favorite= false;
                    });

        }
    }

    public  void addOrRemoveFromFavorite(View view) { //

        if (favorite == false)   {
            Map<String, Object> docData = new HashMap<>();
            docData.put("business_id", bundle.get("business_id"));
            FirebaseFirestore.getInstance()
                    .collection(CLIENTS_COLLECTION + "/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/Favorites")
                    .document()
                    .set(docData).addOnCompleteListener(task -> {
                            favorite=true;
                            ((ImageButton) findViewById(R.id.favoriteStar)).setImageResource(R.drawable.ic_star_black_30dp);

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

        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
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





    @Override
public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.business_menu, menu);
    menu.getItem(1).setVisible(false);
    menu.getItem(2).setVisible(false);
//    menu.getItem(3).setVisible(false);
    menu.getItem(0).setVisible(false);
//        menu.getItem(0).setOnMenuItemClickListener(cl -> {
//            FirebaseDynamicLinks.getInstance()
//                    .createDynamicLink()
//                    .setLink(Uri.parse("https://cueapp.com/?name=" +
//                            bundle.getString("business_id")))
//                    .setDomainUriPrefix("https://cueapp.page.link")
//                    .setAndroidParameters(
//                            new DynamicLink.AndroidParameters
//                                    .Builder("com.technion.cue")
//                                    .build())
//                    .buildShortDynamicLink()
//                    .addOnSuccessListener(this, shortLink -> {
//                        // Short link created
//                        ClipboardManager clipboard = (ClipboardManager)
//                                getBaseContext()
//                                        .getSystemService(Context.CLIPBOARD_SERVICE);
//                        ClipData clip =
//                                ClipData.newPlainText("copied to clipboard",
//                                        shortLink.getShortLink().toString());
//                        Toast.makeText(getBaseContext(), "copied link to clipboard",
//                                Toast.LENGTH_SHORT).show();
//                        clipboard.setPrimaryClip(clip);
//                    });
//            return true;
//        });
    return true;
}

}
