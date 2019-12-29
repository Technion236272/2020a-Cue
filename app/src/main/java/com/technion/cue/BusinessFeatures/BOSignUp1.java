package com.technion.cue.BusinessFeatures;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.technion.cue.R;
import com.technion.cue.SignInActivity;
import com.technion.cue.annotations.ModuleAuthor;

@ModuleAuthor("Topaz")
public class BOSignUp1 extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bo_sign_up);

        final Button btn_next = findViewById(R.id.btn_next1);
        TextInputEditText email = findViewById(R.id.businessEmailEditText);
        TextInputEditText password = findViewById(R.id.businessPasswordEditText);
        TextInputEditText boFullName = findViewById(R.id.businessFullNameEditText);

        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (inputIsValid()) {
                    Intent in = new Intent(getBaseContext(), BOSignUp2.class);
                    in.putExtra("email", email.getText().toString());
                    in.putExtra("password", password.getText().toString());
                    in.putExtra("boName", boFullName.getText().toString());
                    startActivity(in);
                    finish();
                }
            }
        });
    }

    private boolean inputIsValid() {
        if (inputNotEmpty()) {
            TextInputEditText email = findViewById(R.id.businessEmailEditText);
            TextInputEditText password = findViewById(R.id.businessPasswordEditText);
            TextInputEditText boFullName = findViewById(R.id.businessFullNameEditText);
            if(!isEmailValid(email.getText().toString())){
                return false;
            }
            if(password.getText().toString().length() < 6){
                Toast.makeText(this, "Password should be at least six characters", Toast.LENGTH_SHORT).show();
                return false;
            }
            if(!boFullName.getText().toString().contains(" ")){
                Toast.makeText(this, "Full name should be at least two words", Toast.LENGTH_SHORT).show();
                return false;
            }
            return true;
        }
        return false;
    }

    private boolean inputNotEmpty(){
        ViewGroup vg = findViewById(R.id.business_sign_up1);
        for (int i = 0; i < vg.getChildCount(); i++) {
            if (vg.getChildAt(i) instanceof TextInputEditText) {
                if (((EditText) vg.getChildAt(i)).getText().toString().isEmpty()){
                    Toast.makeText(this, "There is empty Fields", Toast.LENGTH_SHORT).show();
                    return false;
                }

            }
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
}
