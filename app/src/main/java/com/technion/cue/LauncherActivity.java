package com.technion.cue;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.technion.cue.BusinessFeatures.BOBusinessHomePage;
import com.technion.cue.BusinessFeatures.BusinessSignUp;
import com.technion.cue.ClientFeatures.ClientHomePage;
import com.technion.cue.ClientFeatures.ClientSignUp;
import com.technion.cue.annotations.ModuleAuthor;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import static com.technion.cue.FirebaseCollections.BUSINESSES_COLLECTION;
import static com.technion.cue.FirebaseCollections.CLIENTS_COLLECTION;

@ModuleAuthor("ben")
public class LauncherActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        getSupportActionBar().hide();

        //   check if already signup

        mAuth = FirebaseAuth.getInstance();

        // checking if already sign in - ben
        if (mAuth.getCurrentUser()!= null)  {

            SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
            if (pref.contains(mAuth.getCurrentUser().getUid())) {
                // all ready signin before - searching for local
                if (pref.getString(mAuth.getCurrentUser().getUid(), null).equals("Client")) {
                    startClientHomepage();
                } else {
                    searchForBO(mAuth.getCurrentUser().getUid());
                }
            } else {
                findViewById(R.id.loadingPanelLauncher).setVisibility(View.VISIBLE);
                FirebaseFirestore.getInstance()
                        .collection(CLIENTS_COLLECTION)
                        .document(mAuth.getCurrentUser().getUid())
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                if (task.getResult().exists()) {
                                    updateLocalSettings("Client");
                                    startClientHomepage();
                                } else {
                                    updateLocalSettings("Business");
                                    searchForBO(mAuth.getCurrentUser().getUid());

                                }
                            } else {
                                Toast.makeText(LauncherActivity.this,
                                        "Authentication failed.##",
                                        Toast.LENGTH_LONG).show();
                            }
                        });
            }
        } else {
            startActivity(new Intent(getBaseContext(), SignInActivity.class));
            findViewById(R.id.loadingPanelLauncher).setVisibility(View.GONE);
            finish();

        }



    }

    private void searchForBO(String uid) {
        findViewById(R.id.loadingPanelLauncher).setVisibility(View.VISIBLE);
        FirebaseFirestore.getInstance()
                .collection(BUSINESSES_COLLECTION)
                .document(uid)
                .get()
                .addOnSuccessListener(l -> {
                    if (l.exists()) {
                        startActivity(new Intent(getBaseContext(), BOBusinessHomePage.class));
                        finish();
                    } else {
                        Toast.makeText(LauncherActivity.this,
                                "Authentication failed : Email us your username.##", // - ben - 17/12 - when user is not client and not bo
                                Toast.LENGTH_LONG).show();
                        findViewById(R.id.loadingPanelLauncher).setVisibility(View.GONE);
                    }
                }).addOnFailureListener(l ->
                Toast.makeText(LauncherActivity.this,
                        "Authentication failed.##",
                        Toast.LENGTH_LONG).show());
        findViewById(R.id.loadingPanelLauncher).setVisibility(View.GONE);
    }

    private void startClientHomepage() {
        // open up client homepage, if he was found
        startActivity(new Intent(getBaseContext(), ClientHomePage.class));
        findViewById(R.id.loadingPanelLauncher).setVisibility(View.GONE);
        finish();
    }

    // Remember localy if a user is a client or business owner
    private void updateLocalSettings(String state) {
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(mAuth.getCurrentUser().getUid(),state);
        editor.commit();
    }
}
