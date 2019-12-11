package com.technion.cue;

import androidx.appcompat.app.AppCompatActivity;
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
import android.os.Bundle;

public class BOSignUp extends AppCompatActivity {

    FirebaseAuth mAuth;
    private static final String TAG = "BOSignUpActivity";
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bo_sign_up);
        mAuth=FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        final Button button_sign_up= (Button) findViewById(R.id.b_button_signup);
        button_sign_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button_sign_up.setEnabled(false);
                final String user_password= ((EditText) findViewById(R.id.b_password)).getText().toString();
                final String email= ((EditText)findViewById(R.id.b_email_address)).getText().toString();
                final String full_name = ((EditText)findViewById(R.id.b_full_name)).getText().toString();
                final String phone_number = ((EditText)findViewById(R.id.b_phone_number)).getText().toString();
                final String business_name = ((EditText)findViewById(R.id.business_name)).getText().toString();
                //final String logo_path = ((EditText)findViewById(R.id.logo_path)).getText().toString();



                if(!user_password.isEmpty() && !email.isEmpty() && !full_name.isEmpty() &&
                        !phone_number.isEmpty() && !business_name.isEmpty()){
                    mAuth.createUserWithEmailAndPassword(email,user_password).
                            addOnCompleteListener(BOSignUp.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()){
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        Business business = new Business(user.getUid(),business_name,full_name);
                                        db.collection("Businesses")
                                                .document(user.getUid())
                                                .set(business);
                                        //updateUI(user);
                                        finish();
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Toast.makeText(BOSignUp.this, "failed",Toast.LENGTH_LONG).show();
                                        updateUI(null);
                                    }
                                }
                            });

                }
                else {
                    Toast.makeText(BOSignUp.this, "empty fields, try again!",
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