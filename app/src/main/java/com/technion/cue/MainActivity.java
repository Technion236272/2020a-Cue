package com.technion.cue;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import android.util.Log;


public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private static final String TAG = "MainActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button button_sign_in = (Button) findViewById(R.id.button_signin);

        mAuth = FirebaseAuth.getInstance();

        final EditText password = (EditText) findViewById(R.id.password);
        final EditText email = (EditText) findViewById(R.id.email_address);

        button_sign_in.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                button_sign_in.setEnabled(false);
                final EditText user_password = (EditText) findViewById(R.id.password);
                final EditText email = (EditText) findViewById(R.id.email_address);

                if (!user_password.getText().toString().isEmpty() && !email.getText().toString().isEmpty()) {

                    mAuth.signInWithEmailAndPassword(email.getText().toString(), user_password.getText().toString())
                            .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "signInWithEmail:success");
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        updateUI(user);

                                    } else {
                                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                                        Toast.makeText(MainActivity.this, "Authentication failed.",
                                                Toast.LENGTH_SHORT).show();
//                                        updateUI(null);
                                    }

                                }

                            });
                } else {
                    Toast.makeText(MainActivity.this, "email or password are empty, try again",
                            Toast.LENGTH_SHORT).show();
                }
                button_sign_in.setEnabled(true);

            }
        });
    }

    public void updateUI(final FirebaseUser user) {
        if (user != null) {
            final FirebaseFirestore fs = FirebaseFirestore.getInstance();
            CollectionReference cr = fs.collection("Clients");
            fs.collection("Clients").whereEqualTo("user_uid", user.getUid())
                    .limit(1).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        startActivity(new Intent(getBaseContext(), ClientHomePage.class));
                        finish();
                    } else {
                        fs.collection("Business Owner").whereEqualTo("user_uid", user.getUid())
                                .limit(1).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    startActivity(new Intent(getBaseContext(), BOHomePage.class));
                                    finish();
                                } else {
                                    Toast.makeText(MainActivity.this,
                                            "Authentication failed.##", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                }
            });
        }
    }
}