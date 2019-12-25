package com.technion.cue;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import androidx.annotation.NonNull;

import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.technion.cue.BusinessFeatures.BOBusinessHomePage;
import com.technion.cue.BusinessFeatures.BusinessSignUp;
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

        mAuth = FirebaseAuth.getInstance();
        // checking if already sign in - ben
        if (mAuth.getCurrentUser()!= null)  {
            startClientHomepage();
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
            final Intent intent1 = new Intent(getBaseContext(), BusinessSignUp.class);
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
                            Log.d(TAG, "signInWithEmail:success");
                            updateUI(mAuth.getCurrentUser().getUid());
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
                    }
                });
    }

    private void startClientHomepage() {
        // open up client homepage, if he was found
        startActivity(new Intent(getBaseContext(), ClientHomePage.class));

        finish();
    }

    private void searchForBO(String uid) {
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
                        }
                    }).addOnFailureListener(l ->
                    Toast.makeText(SignInActivity.this,
                            "Authentication failed.##",
                            Toast.LENGTH_LONG).show());
    }
}