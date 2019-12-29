package com.technion.cue.ClientFeatures;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.view.ViewGroup;
import android.widget.Button;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.technion.cue.BusinessFeatures.BOSignUp3;
import com.technion.cue.R;
import com.technion.cue.annotations.ModuleAuthor;
import com.technion.cue.data_classes.Client;

import static com.technion.cue.FirebaseCollections.CLIENTS_COLLECTION;

@ModuleAuthor("Topaz")
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
            final TextInputEditText user_password= findViewById(R.id.clientPasswordEditText);
            final TextInputEditText email= findViewById(R.id.clientEmailEditText);
            final TextInputEditText full_name = findViewById(R.id.clientFullNameEditText);
            final TextInputEditText phone_number = findViewById(R.id.clientPhoneEditText);

            String u_password = user_password.getText().toString();
            String u_email = email.getText().toString();
            String u_name = full_name.getText().toString();
            String u_number = phone_number.getText().toString();

            if(inputIsValid(u_password,u_email,u_name,u_number)) {
                mAuth.createUserWithEmailAndPassword
                        (email.getText().toString(), user_password.getText().toString()).
                        addOnSuccessListener(l -> {
                            sendVerificationEmail();
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
            }
            button_sign_up.setEnabled(true);
        });

    }

    private boolean inputIsValid(String password, String email, String name, String phone) {
        if (inputNotEmpty(password, email, name, phone)) {
            if(!isEmailValid(email)){
                return false;
            }
            if(password.length() < 6){
                Toast.makeText(this, "Password should be at least six characters", Toast.LENGTH_SHORT).show();
                return false;
            }
            if(!name.contains(" ")){
                Toast.makeText(this, "Full name should be at least two words", Toast.LENGTH_SHORT).show();
                return false;
            }
            return true;
        }
        return false;
    }

    private boolean inputNotEmpty(String password, String email, String name, String phone) {
        if(password.isEmpty() || email.isEmpty() || name.isEmpty() || phone.isEmpty()){
            Toast.makeText(this, "There is empty Fields", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public boolean isEmailValid(String email) {
        if (android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            return true;
        }
        else {
            Toast.makeText(this, "Please enter a Valid E-Mail Address!", Toast.LENGTH_SHORT).show();
            return false;
        }
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

    public void sendVerificationEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(ClientSignUp.this,
                                        "Signup successful. Verification email sent",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }

    }


}


