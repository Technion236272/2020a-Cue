package com.technion.cue;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.User;
import com.technion.cue.BusinessFeatures.BOBusinessHomePage;
import com.technion.cue.BusinessFeatures.BusinessSettings;
import com.technion.cue.BusinessFeatures.BusinessSignUpContainer;
import com.technion.cue.ClientFeatures.ClientHomePage;
import com.technion.cue.ClientFeatures.ClientSignUp;
import com.technion.cue.data_classes.Client;

import android.util.Log;

import static com.technion.cue.FirebaseCollections.BUSINESSES_COLLECTION;
import static com.technion.cue.FirebaseCollections.CLIENTS_COLLECTION;


/**
 * this is the app's main activity
 */
public class SignInActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private static final String TAG = "SignInActivity";
    int RC_SIGN_IN = 454;
    GoogleSignInClient mGoogleSignInClient;
    FirebaseFirestore db;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        db = FirebaseFirestore.getInstance();

        // check if already signed up
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
                            findViewById(R.id.loadingPanelSignin).setVisibility(View.GONE);
                            Toast.makeText(SignInActivity.this,
                                    "Authentication failed in create",
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        }

        //start of the google sign in

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .requestProfile()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        com.google.android.gms.common.SignInButton google_button = findViewById(R.id.sign_in_button);
        google_button.setOnClickListener(v->{
            google_button.setEnabled(false);
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
            google_button.setEnabled(true);

        });

        //end of the google sign in


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
            final Intent intent1 = new Intent(getBaseContext(), BusinessSignUpContainer.class);
            bo_sign_up.setEnabled(true);
            startActivity(intent1);
        });

        button_sign_in.setOnClickListener(v -> {

            findViewById(R.id.loadingPanelSignin).setVisibility(View.VISIBLE);

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
                                findViewById(R.id.loadingPanelSignin).setVisibility(View.GONE);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try{
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                // ...
            }
            //handleSignInResult(task);
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            boolean new_user = task.getResult().getAdditionalUserInfo().isNewUser();
                            if (new_user) {
                                updateUIGoogle(user);
                            }
                            else {
                                updateUI(user.getUid());
                            }

                            // ...
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(SignInActivity.this,
                                    "Sign in failed. make sure your email isn't already in use",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                }).addOnFailureListener(f->{
            Toast.makeText(SignInActivity.this,
                    "Sign in failed. make sure your email isn't already in use",
                    Toast.LENGTH_LONG).show();
        });
    }


    class CustomListener implements View.OnClickListener {
        private final Dialog dialog;
        private final String email;
        private final String uid;

        public CustomListener(Dialog dialog, String email, String uid) {
            this.dialog = dialog;
            this.email = email;
            this.uid = uid;
        }

        @Override
        public void onClick(View v) {
            com.google.android.material.textfield.TextInputEditText full_name = dialog.findViewById(R.id.clientFullNameEditText);
            com.google.android.material.textfield.TextInputEditText phone = dialog.findViewById(R.id.clientPhoneEditText);

            String sfull_name = full_name.getText().toString();
            String sphone = phone.getText().toString();

            if(sfull_name.isEmpty() || sphone.isEmpty()){
                Toast.makeText(SignInActivity.this,
                        "Please fill in all the fields",
                        Toast.LENGTH_LONG).show();
            }
            else if(!sfull_name.contains(" ")){
                Toast.makeText(SignInActivity.this,
                        "Full name should contain both first and last names",
                        Toast.LENGTH_LONG).show();
            }
            else {
                Client client = new Client(email,
                        sfull_name,
                        sphone);
                db.collection(CLIENTS_COLLECTION)
                        .document(uid)
                        .set(client).addOnCompleteListener(l->{
                            if(l.isSuccessful()){
                                startClientHomepage();
                            }else{
                                Toast.makeText(SignInActivity.this,
                                        "Authentication failed",
                                        Toast.LENGTH_LONG).show();
                            }
                    findViewById(R.id.loadingPanelSignin).setVisibility(View.GONE);
                });
            }
        }
    }

    public void updateUIGoogle(FirebaseUser user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(SignInActivity.this);

        builder.setCancelable(false)
                .setMessage("In order to continue, please insert this following details:")
                .setView(R.layout.sign_in_dialog)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        Button save_button = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        save_button.setOnClickListener
                (new CustomListener(alertDialog, user.getEmail(), user.getUid()));


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
                                    "Authentication failed",
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
                            findViewById(R.id.loadingPanelSignin).setVisibility(View.GONE);
                            Toast.makeText(SignInActivity.this,
                                    "Authentication failed : Email us your username", // - ben - 17/12 - when user is not client and not bo
                                    Toast.LENGTH_LONG).show();
                                    findViewById(R.id.loadingPanelSignin).setVisibility(View.GONE);
                        }
                    }).addOnFailureListener(l -> {
                        findViewById(R.id.loadingPanelSignin).setVisibility(View.GONE);
                        Toast.makeText(SignInActivity.this,
                                "Authentication failed",
                                Toast.LENGTH_LONG).show();
                        findViewById(R.id.loadingPanelSignin).setVisibility(View.GONE);
                    });

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

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}