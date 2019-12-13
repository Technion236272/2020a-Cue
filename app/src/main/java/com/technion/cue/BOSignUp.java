package com.technion.cue;

import androidx.appcompat.app.AppCompatActivity;
import android.view.ViewGroup;
import android.widget.Button;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.technion.cue.data_classes.Business;

import static com.technion.cue.FirebaseCollections.*;

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

        final Button button_sign_up= findViewById(R.id.b_button_signup);
        button_sign_up.setOnClickListener(v -> {
            button_sign_up.setEnabled(false);
            final String user_password= ((EditText) findViewById(R.id.b_password)).getText().toString();
            final String email= ((EditText)findViewById(R.id.b_email_address)).getText().toString();
            final String full_name = ((EditText)findViewById(R.id.b_full_name)).getText().toString();
            final String phone_number = ((EditText)findViewById(R.id.b_phone_number)).getText().toString();
            final String business_name = ((EditText)findViewById(R.id.business_name)).getText().toString();

            if(allTextsValid()){
                mAuth.createUserWithEmailAndPassword(email,user_password).
                        addOnSuccessListener(l -> {
                            FirebaseUser user = mAuth.getCurrentUser();
                            Business business = new Business(business_name, full_name, phone_number);
                            db.collection(BUSINESSES_COLLECTION)
                                    .document(user.getUid())
                                    .set(business);
                            finish();
                        }).addOnFailureListener(l ->
                        // If sign in fails, display a message to the user.
                        Toast.makeText(BOSignUp.this,
                                "failed",
                                Toast.LENGTH_LONG).show());

            }
            else {
                Toast.makeText(BOSignUp.this, "empty fields, try again!",
                        Toast.LENGTH_SHORT).show();
            }
            button_sign_up.setEnabled(true);

        });

    }

    private Boolean allTextsValid() {
        ViewGroup vg = findViewById(R.id.business_sign_up);
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