package com.technion.cue;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

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

        final Button button_sign_up= (Button) findViewById(R.id.c_button_signup);
        button_sign_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button_sign_up.setEnabled(false);
                final EditText user_password= (android.widget.EditText) findViewById(R.id.c_password);
                final EditText email= (EditText)findViewById(R.id.c_email_address);
                final EditText full_name = (EditText)findViewById(R.id.c_full_name);
                final EditText phone_number = (EditText)findViewById(R.id.c_phone_number);


                if(!user_password.getText().toString().isEmpty() && !email.getText().toString().isEmpty()
                && !full_name.getText().toString().isEmpty() && !phone_number.getText().toString().isEmpty()){
                    mAuth.createUserWithEmailAndPassword
                            (email.getText().toString(),user_password.getText().toString()).
                            addOnCompleteListener(ClientSignUp.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                FirebaseUser user = mAuth.getCurrentUser();
                                Client client = new Client(user.getUid(),user.getEmail(), full_name.getText().toString(), phone_number.getText().toString());
                                db.collection("Clients").add(client).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        Log.d(TAG,"what it is for?");
                                    }
                                }).
                                        addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.w(TAG, "error");
                                            }
                                        });

                                //updateUI(user);
                                finish();
                            } else {
                                // If sign in fails, display a message to the user.
                                Toast.makeText(ClientSignUp.this, "failed",Toast.LENGTH_LONG).show();
                                updateUI(null);
                            }
                        }
                    });

                }
                else {
                    Toast.makeText(ClientSignUp.this, "email or password are empty, try again",
                            Toast.LENGTH_SHORT).show();
                }
                button_sign_up.setEnabled(true);

            }
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


