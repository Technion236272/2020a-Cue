package com.technion.cue;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import androidx.annotation.NonNull;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.technion.cue.BusinessFeatures.BOBusinessHomePage;
import com.technion.cue.BusinessFeatures.BOSignUp1;
import com.technion.cue.ClientFeatures.ClientHomePage;
import com.technion.cue.ClientFeatures.ClientSignUp;
import com.technion.cue.annotations.ModuleAuthor;

import android.util.Log;

import static com.technion.cue.FirebaseCollections.BUSINESSES_COLLECTION;
import static com.technion.cue.FirebaseCollections.CLIENTS_COLLECTION;


/**
 * this is the app's main activity
 */
public class SignInActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private static final String TAG = "SignInActivity";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_main);
//      check if already signup
        findViewById(R.id.loadingPanelSignin).setVisibility(View.GONE);
        getSupportActionBar().hide();
        mAuth = FirebaseAuth.getInstance();
        // checking if already sign in - ben
        if (mAuth.getCurrentUser()!= null)  {
            findViewById(R.id.loadingPanelSignin).setVisibility(View.VISIBLE);
            FirebaseFirestore.getInstance()
                    .collection(CLIENTS_COLLECTION)
                    .document(mAuth.getCurrentUser().getUid())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if (task.getResult().exists()) {
                                startClientHomepage();
                            } else {
                                searchForBO(mAuth.getCurrentUser().getUid());
                            }
                        } else {
                            Toast.makeText(SignInActivity.this,
                                    "Authentication failed.##",
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        }


        final Button button_sign_in = findViewById(R.id.button_signin);
        final TextView client_sign_up = findViewById(R.id.client_join);
        final TextView bo_sign_up = findViewById(R.id.business_join);
        

        // open up sign up activity for clients
        client_sign_up.setOnClickListener(v -> {
            client_sign_up.setEnabled(false);
            final Intent intent = new Intent(getBaseContext(), ClientSignUp.class);
            client_sign_up.setEnabled(false);
            startActivity(intent);
        });

        // open up sign up activity for business owners
        bo_sign_up.setOnClickListener(v -> {
            bo_sign_up.setEnabled(false);
            final Intent intent1 = new Intent(getBaseContext(), BOSignUp1.class);
            bo_sign_up.setEnabled(true);
            startActivity(intent1);
        });

        button_sign_in.setOnClickListener(v -> {

            button_sign_in.setEnabled(false);
            final EditText user_password = findViewById(R.id.password);
            final EditText email = findViewById(R.id.email_address);

            if (!user_password.getText().toString().isEmpty() &&
                    !email.getText().toString().isEmpty()) {
                mAuth.signInWithEmailAndPassword(email.getText().toString(),
                        user_password.getText().toString())
                        .addOnSuccessListener(l -> {
                            boolean res = checkIfEmailVerified();
                            if(res) {
                                Log.d(TAG, "signInWithEmail:success");
                                updateUI(mAuth.getCurrentUser().getUid());
                            }
                            else {
                                Toast.makeText(SignInActivity.this,
                                        "Please verify your email!",
                                        Toast.LENGTH_LONG).show();
                            }
                        }).addOnFailureListener(l -> {
                            Log.w(TAG, "signInWithEmail:failure");
                            Toast.makeText(SignInActivity.this,
                                    "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        });
            } else {
                Toast.makeText(SignInActivity.this,
                        "email or password are empty, try again",
                        Toast.LENGTH_SHORT).show();
            }
            button_sign_in.setEnabled(true);

        });
    }

    public void updateUI(@NonNull String uid) {
        findViewById(R.id.loadingPanelSignin).setVisibility(View.VISIBLE);
        FirebaseFirestore.getInstance()
                .collection(CLIENTS_COLLECTION)
                .document(uid)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().exists()) {
                            startClientHomepage();
                        } else {
                            searchForBO(uid);
                        }
                    } else {
                            Toast.makeText(SignInActivity.this,
                                    "Authentication failed.##",
                                    Toast.LENGTH_LONG).show();
                                     findViewById(R.id.loadingPanelSignin).setVisibility(View.GONE);
                    }
                });
    }

    private void startClientHomepage() {
        // open up client homepage, if he was found
        startActivity(new Intent(getBaseContext(), ClientHomePage.class));
        findViewById(R.id.loadingPanelSignin).setVisibility(View.GONE);
        finish();
    }

    private void searchForBO(String uid) {
            findViewById(R.id.loadingPanelSignin).setVisibility(View.VISIBLE);
            FirebaseFirestore.getInstance()
                    .collection(BUSINESSES_COLLECTION)
                    .document(uid)
                    .get()
                    .addOnSuccessListener(l -> {
                        if (l.exists()) {
                            startActivity(new Intent(getBaseContext(), BOBusinessHomePage.class));
                            finish();
                        } else {
                            Toast.makeText(SignInActivity.this,
                                    "Authentication failed : Email us your username.##", // - ben - 17/12 - when user is not client and not bo
                                    Toast.LENGTH_LONG).show();
                                    findViewById(R.id.loadingPanelSignin).setVisibility(View.GONE);
                        }
                    }).addOnFailureListener(l ->
                    Toast.makeText(SignInActivity.this,
                            "Authentication failed",
                            Toast.LENGTH_LONG).show());
                            findViewById(R.id.loadingPanelSignin).setVisibility(View.GONE);
    }
    private boolean checkIfEmailVerified()
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user.isEmailVerified())
        {
            // user is verified, so you can finish this activity or send user to activity which you want.
            Toast.makeText(SignInActivity.this, "Successfully logged in", Toast.LENGTH_SHORT).show();
            return true;
        }
        else
        {
            // email is not verified, so just prompt the message to the user and restart this activity.
            // NOTE: don't forget to log out the user.
            FirebaseAuth.getInstance().signOut();
            return false;

            //restart this activity

        }
    }
}