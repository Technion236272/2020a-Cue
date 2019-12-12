package com.technion.cue;

import androidx.appcompat.app.AppCompatActivity;

import android.widget.Button;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.technion.cue.data_classes.Client;

public class ClientSignUp extends AppCompatActivity {
    FirebaseAuth mAuth;
    private static final String TAG = "ClientSignUpActivity";
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_sign_up);
        mAuth=FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        final Button button_sign_up= findViewById(R.id.c_button_signup);
        button_sign_up.setOnClickListener(v -> {

            button_sign_up.setEnabled(false);
            final EditText user_password= findViewById(R.id.c_password);
            final EditText email= findViewById(R.id.c_email_address);
            final EditText full_name = findViewById(R.id.c_full_name);
            final EditText phone_number = findViewById(R.id.c_phone_number);

            // TODO: maybe require more complex conditions (e.g., minimum length for password)
            if(!user_password.getText().toString().isEmpty() &&
                    !email.getText().toString().isEmpty() &&
                    !full_name.getText().toString().isEmpty() &&
                    !phone_number.getText().toString().isEmpty()) {
                mAuth.createUserWithEmailAndPassword
                        (email.getText().toString(), user_password.getText().toString()).
                        addOnSuccessListener(l -> {
                            FirebaseUser user = mAuth.getCurrentUser();
                            Client client = new Client(user.getUid(), user.getEmail(),
                                    full_name.getText().toString(),
                                    phone_number.getText().toString());
                            db.collection("Clients")
                                    .document(user.getUid())
                                    .set(client);
                            finish();
                        }).addOnFailureListener(l -> {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(ClientSignUp.this,
                            "failed",Toast.LENGTH_LONG).show();
                            updateUI(null);
                        });
            } else {
                Toast.makeText(ClientSignUp.this,
                        "email or password are empty, try again",
                        Toast.LENGTH_SHORT).show();
            }

            button_sign_up.setEnabled(true);
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    public void  updateUI(FirebaseUser account){
        if(account != null){
            //TODO: do something here.
        }
    }


}


