package com.technion.cue.ClientFeatures;

import androidx.appcompat.app.AppCompatActivity;

import android.view.ViewGroup;
import android.widget.Button;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.technion.cue.R;
import com.technion.cue.data_classes.Client;

import static com.technion.cue.FirebaseCollections.CLIENTS_COLLECTION;

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
            if(allTextsValid()) {
                mAuth.createUserWithEmailAndPassword
                        (email.getText().toString(), user_password.getText().toString()).
                        addOnSuccessListener(l -> {
                            FirebaseUser user = mAuth.getCurrentUser();
                            Client client = new Client(user.getEmail(),
                                    full_name.getText().toString(),
                                    phone_number.getText().toString());
                            db.collection(CLIENTS_COLLECTION)
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

    private Boolean allTextsValid() {
        ViewGroup vg = findViewById(R.id.client_sign_up);
        for (int i = 0 ;
             i < vg.getChildCount() ;
             i++) {
            if (vg.getChildAt(i) instanceof EditText) {
                if (((EditText) vg.getChildAt(i)).getText().toString().isEmpty())
                    return false;
            }
        }
        return true;
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


