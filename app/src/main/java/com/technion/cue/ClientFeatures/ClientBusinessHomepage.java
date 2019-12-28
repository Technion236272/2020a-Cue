package com.technion.cue.ClientFeatures;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.firestore.FirebaseFirestore;
import com.technion.cue.BusinessFeatures.BusinessInfoFragment;
import com.technion.cue.FirebaseCollections;
import com.technion.cue.R;
import com.technion.cue.annotations.ModuleAuthor;

import static com.technion.cue.FirebaseCollections.CLIENTS_COLLECTION;

/*
        Still need to set all up
 */

@ModuleAuthor("Ben")
public class ClientBusinessHomepage extends AppCompatActivity {

    Bundle bundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_business_homepage);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Favorite");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // getting the b_id from client home page
        this.bundle = getIntent().getExtras();
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
                                        findViewById(R.id.switch_to_date_time_fragments).setVisibility(View.VISIBLE);
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
            Intent intent = new Intent(this, EditAppointmentActivity.class);
            intent.putExtras(this.bundle);
            startActivity(intent);
            findViewById(R.id.switch_to_date_time_fragments).setVisibility(View.VISIBLE);
        });



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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.business_menu, menu);
        menu.getItem(1).setVisible(false);
        menu.getItem(2).setVisible(false);
        menu.getItem(3).setVisible(false);
        menu.getItem(0).setOnMenuItemClickListener(cl -> {
            FirebaseDynamicLinks.getInstance()
                    .createDynamicLink()
                    .setLink(Uri.parse("https://cueapp.com/?name=" +
                            bundle.getString("business_id")))
                    .setDomainUriPrefix("https://cueapp.page.link")
                    .setAndroidParameters(
                            new DynamicLink.AndroidParameters
                                    .Builder("com.technion.cue")
                                    .build())
                    .buildShortDynamicLink()
                    .addOnSuccessListener(this, shortLink -> {
                        // Short link created
                        ClipboardManager clipboard = (ClipboardManager)
                                getBaseContext()
                                        .getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip =
                                ClipData.newPlainText("copied to clipboard",
                                        shortLink.getShortLink().toString());
                        Toast.makeText(getBaseContext(), "copied link to clipboard",
                                Toast.LENGTH_SHORT).show();
                        clipboard.setPrimaryClip(clip);
                    });
            return true;
        });
        return true;
    }



}
