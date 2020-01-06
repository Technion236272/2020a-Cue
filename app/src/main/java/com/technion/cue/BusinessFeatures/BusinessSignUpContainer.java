package com.technion.cue.BusinessFeatures;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.technion.cue.R;
import com.technion.cue.SignInActivity;
import com.technion.cue.data_classes.Business;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static com.technion.cue.ConstantsCollection.MY_PERMISSIONS_REQUEST_READ_MEDIA;
import static com.technion.cue.FirebaseCollections.BUSINESSES_COLLECTION;

public class BusinessSignUpContainer extends AppCompatActivity {

    public TextInputEditText email;
    public TextInputEditText password;
    public TextInputEditText bo_name;
    public TextInputEditText name;
    public TextInputEditText description;
    public TextInputEditText phone;
    public Uri logoData;
    public Button done_btn;
    public TextInputEditText state;
    public TextInputEditText city;
    public TextInputEditText address;
    public Map<String, String> open_hours = new HashMap<>();

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private UploadTask uploadTask;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_sign_up);

        ViewPager viewPager = findViewById(R.id.business_sign_up_pager);
        viewPager.setAdapter(new SignUpPagerAdapter(getSupportFragmentManager()));

        // ask permission to upload logo
        askPermission();
    }

    void askPermission() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?

            // No explanation needed; request the permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_MEDIA);

            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant. The callback method gets the
            // result of the request.
        }
    }

    public void sendVerificationEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(getBaseContext(),
                                    "Sign-Up was successful. Please check your email " +
                                            "for a verification message",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    void uploadLogo(Uri data) {

        if (data == null)
            return;

        final String business_id = FirebaseAuth.getInstance().getUid();
        final StorageReference logosRef = FirebaseStorage.getInstance().getReference()
                .child("business_logos/" + business_id + new Random().nextInt());

        uploadTask = logosRef.putFile(data);
    }

    private boolean isInputNotEmpty(String ... fields){
        for (String f : fields) {
            if (f.isEmpty())
                return false;
        }

        return true;
    }

    private boolean isEmailValid(String email) {
        if (android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            return true;
        }
        else {
            Toast.makeText(this,
                    "Please enter a Valid E-Mail Address!",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private boolean isInputValid() {

        String bEmail = email.getText().toString();
        String bPassword = password.getText().toString();
        String boName = bo_name.getText().toString();
        String bName = name.getText().toString();
        String bDesc = description.getText().toString();
        String bPhone = phone.getText().toString();
        String bState = state.getText().toString();
        String bCity = city.getText().toString();
        String bAddress = address.getText().toString();

        // check if fields were filled
        if (isInputNotEmpty(bName, bEmail, bPassword, boName,
                bDesc, bPhone, bState, bCity, bAddress)){

            // if email is invalid
            if(!isEmailValid(bEmail)){
                return false;
            }

            // if password is too short
            if(bPassword.length() < 6){
                Toast.makeText(this,
                        "Password should be at least six characters long",
                        Toast.LENGTH_SHORT).show();
                return false;
            }

            // if the BO's bo_name doesn't contains both first & last names
            if(!boName.contains(" ")){
                Toast.makeText(this,
                        "your name should contain both first and last names",
                        Toast.LENGTH_SHORT).show();
                return false;
            }

            return true;
        }
        return false;
    }

    // called when the business owner finishes sign up
    public void onFinished() {

        String bEmail = email.getText().toString();
        String bPassword = password.getText().toString();

        done_btn.setEnabled(false);
        boolean res = isInputValid();

        if (res) {
            mAuth.createUserWithEmailAndPassword(bEmail, bPassword)
                    .addOnSuccessListener(r -> {
                        sendVerificationEmail();
                        FirebaseUser user = mAuth.getCurrentUser();
                        uploadLogo(logoData);
                        if (uploadTask == null) {
                            uploadBusinessAndFinish(user, null);
                        } else {
                            uploadTask.addOnSuccessListener(storageRef ->
                                    uploadBusinessAndFinish(user, storageRef));
                        }
                    }).addOnFailureListener(e -> {
                        // If sign in fails, display a message to the user.
                        if(e.getMessage().equals("The email address is already in use by another account.")) {
                            Toast.makeText(getBaseContext(),
                                    "email is already in use, please change your email or sign in",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getBaseContext(),
                                    "Sign up failed",
                                    Toast.LENGTH_LONG).show();
                        }
                        done_btn.setEnabled(true);
            });
        } else {
            Toast.makeText(getBaseContext(),
                    "Some fields are empty. Please fill the missing fields",
                    Toast.LENGTH_SHORT).show();
            done_btn.setEnabled(true);
        }
    }

    private void uploadBusinessAndFinish(FirebaseUser user, UploadTask.TaskSnapshot sl) {

        String boName = bo_name.getText().toString();
        String bName = name.getText().toString();
        String bDesc = description.getText().toString();
        String bPhone = phone.getText().toString();
        String bState = state.getText().toString();
        String bCity = city.getText().toString();
        String bAddress = address.getText().toString();

        String logo_path = "";
        if (sl != null)
            logo_path = sl.getMetadata().getPath();
        Business business = new Business(bName, boName, bPhone, bDesc,
                bState, bCity, bAddress, open_hours, logo_path);
        db.collection(BUSINESSES_COLLECTION)
                .document(user.getUid())
                .set(business);
        Toast.makeText(getBaseContext(), "Sign up done!",
                Toast.LENGTH_LONG).show();
        Intent in = new Intent(getBaseContext(), SignInActivity.class);
        startActivity(in);
        done_btn.setEnabled(true);
        finish();
    }

    class SignUpPagerAdapter extends FragmentStatePagerAdapter {

        private static final int SIGN_UP_PAGE_COUNT = 3;

        SignUpPagerAdapter(@NonNull FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            switch(position) {
                case 0:
                    return new BusinessSignUp1(BusinessSignUpContainer.this);
                case 1:
                    return new BusinessSignUp2(BusinessSignUpContainer.this);
                case 2:
                    return new BusinessSignUp3(BusinessSignUpContainer.this);
                default:
                    return new BusinessScheduleFragmentPlaceholder();
            }
        }

        @Override
        public int getCount() {
            return SIGN_UP_PAGE_COUNT;
        }
    }

}
