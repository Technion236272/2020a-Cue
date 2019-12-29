package com.technion.cue.BusinessFeatures;

import android.Manifest;
import android.app.TimePickerDialog;
import java.text.ParseException;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.technion.cue.R;
import com.technion.cue.SignInActivity;
import com.technion.cue.annotations.ModuleAuthor;
import com.technion.cue.data_classes.Business;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static com.technion.cue.FirebaseCollections.BUSINESSES_COLLECTION;

@ModuleAuthor("Topaz")
public class BOSignUp3 extends AppCompatActivity {
    private String[] days =
            {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
    private String lastUsedKey;
    private Map<String, String> open_hours = new HashMap<>();
    FirebaseAuth mAuth;
    FirebaseFirestore db;
    private Task uploadTask;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bo_sign_up3);

        mAuth=FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        for (String day : new String[]{"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"}) {
            this.open_hours.put(day, "");
        }


        String email = getIntent().getExtras().getString("email");
        String password = getIntent().getExtras().getString("password");
        String bo_name = getIntent().getExtras().getString("boName");
        String b_name = getIntent().getExtras().getString("bName");
        String b_desc =getIntent().getExtras().getString("bDesc");
        String b_phone = getIntent().getExtras().getString("bPhone");
        Uri logo = (Uri)getIntent().getExtras().get("logoData");


        Button done_btn = findViewById(R.id.btn_done1);
        TextInputEditText state = findViewById(R.id.businessStateEditText);
        TextInputEditText city = findViewById(R.id.businessCityEditText);
        TextInputEditText address = findViewById(R.id.businessAddressEditText);

        done_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                done_btn.setEnabled(false);
                boolean res = inputIsValid();
                if (res) {
                    mAuth.createUserWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {

                        @Override
                        public void onSuccess(AuthResult authResult) {
                            sendVerificationEmail();
                            //System.out.println("==========sign success============");
                            FirebaseUser user = mAuth.getCurrentUser();
                            //system.out.println("==========get current user============");
                            Business business = new Business(b_name, bo_name, b_phone, b_desc,
                                    state.getText().toString(), city.getText().toString(),
                                    address.getText().toString(),open_hours);
                            //System.out.println("==========created business============");
                            db.collection(BUSINESSES_COLLECTION).document(user.getUid()).set(business);
                            uploadLogo(logo);
                            Toast.makeText(BOSignUp3.this, "Sign up done!", Toast.LENGTH_LONG).show();
                            Intent in = new Intent(getBaseContext(), SignInActivity.class);
                            startActivity(in);
                            done_btn.setEnabled(true);
                            finish();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // If sign in fails, display a message to the user.
                            //System.out.println("===this is e =======");
                            //System.out.println("===start =======");
                            //System.out.println(e);
                            //System.out.println("===finish =======");
                            if(e.equals("The email address is already in use by another account.")){
                                Toast.makeText(BOSignUp3.this,
                                        "email is already in use, please sign in", Toast.LENGTH_LONG).show();
                                return;
                            }
                            Toast.makeText(BOSignUp3.this,
                                    "Sign up failed", Toast.LENGTH_LONG).show();


                        }
                    });
                }
            }
        });

        AutoCompleteTextView openHoursDays = findViewById(R.id.days_filled_exposed_dropdown);
        openHoursDays.setAdapter(new ArrayAdapter<>(this,
                R.layout.dropdown_menu_popup_item, days));

        TextView openHours = findViewById(R.id.openingHoursEditText);
        TextView closeHours = findViewById(R.id.closingHoursEditText);
        openHours.setClickable(false);
        closeHours.setClickable(false);

        openHoursDays.setOnItemClickListener((parent, view, position, id) -> {

            // we use the map open_hours in order to store the operating hours of the business
            // we check if the chosen day's operating hours are already stored in the map
            // if they aren't, we store them. Regardless, we set the opening hour and closing hour
            // text fields content according to this day's stored values.
            String key = parent.getItemAtPosition(position).toString();
            if (open_hours.containsKey(key)) {
                String open_hour = "", close_hour = "";
                String hours = open_hours.get(key);
                if (hours.contains("-")) {
                    open_hour = hours.split("-")[0];
                    close_hour = hours.split("-")[1];
                }
                openHours.setText(open_hour);
                closeHours.setText(close_hour);
                lastUsedKey = key;
            }

            openHours.setClickable(true);
            closeHours.setClickable(true);

        });

        // setting the opening hours of the business. we use a TimePickerDialog to let
        // the business owner to choose a new opening time, which will be displayed in the
        // opening hour text field.
        // Additionally, we make sure that the chosen opening time precedes the chosen closing time.
        openHours.setOnClickListener(cl -> {
            int hour, minute;
            Calendar c = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            try {
                Date d = sdf.parse(openHours.getText().toString());
                c.setTime(d);
                hour = c.get(Calendar.HOUR_OF_DAY);
                minute = c.get(Calendar.MINUTE);
            } catch (ParseException e) {
                hour = c.get(Calendar.HOUR_OF_DAY);
                minute = c.get(Calendar.MINUTE);
            }

            TimePickerDialog mTimePicker = new TimePickerDialog(this,
                    (timePicker, selectedHour, selectedMinute) -> {

                        String closeHoursText = closeHours.getText().toString();
                        if (closeHoursText.contains(":")) {
                            String closeHour = closeHoursText.split(":")[0];
                            String closeMinute = closeHoursText.split(":")[1];
                            if ((selectedHour > Integer.valueOf(closeHour)) ||
                                    (selectedHour == Integer.valueOf(closeHour)
                                            && selectedMinute >= Integer.valueOf(closeMinute))) {
                                Toast.makeText(this,
                                        "Opening time must precede closing time. Please refill the form.",
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }

                        String selectedMinuteFormatted =
                                selectedMinute == 0 ? "00" : String.valueOf(selectedMinute);
                        String  selectedHourFormatted =
                                selectedHour <= 9 ? "0" + selectedHour : String.valueOf(selectedHour);
                        String value_to_put = selectedHourFormatted + ":" + selectedMinuteFormatted;

                        openHours.setText(value_to_put);
                        if (lastUsedKey == null)
                            return;
                        if (open_hours.get(lastUsedKey).contains("-")) {
                            open_hours.put(lastUsedKey, value_to_put + "-" +
                                    open_hours.get(lastUsedKey).split("-")[1]);
                        } else {
                            open_hours.put(lastUsedKey, value_to_put + "-");
                        }
                    }, hour, minute, true);
            mTimePicker.setTitle("Select opening hour");
            mTimePicker.show();
        });

        // same as above, but for the closing time
        closeHours.setOnClickListener(cl -> {
            int hour, minute;
            Calendar c = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            try {
                Date d = sdf.parse(openHours.getText().toString());
                c.setTime(d);
                hour = c.get(Calendar.HOUR_OF_DAY);
                minute = c.get(Calendar.MINUTE);
            } catch (ParseException e) {
                hour = c.get(Calendar.HOUR_OF_DAY);
                minute = c.get(Calendar.MINUTE);
            }

            TimePickerDialog mTimePicker = new TimePickerDialog(this,
                    (timePicker, selectedHour, selectedMinute) -> {

                        String openHoursText = openHours.getText().toString();
                        if (openHoursText.contains(":")) {
                            String openHour = openHoursText.split(":")[0];
                            String openMinute = openHoursText.split(":")[1];
                            if ((selectedHour < Integer.valueOf(openHour)) ||
                                    (selectedHour == Integer.valueOf(openHour)
                                            && selectedMinute <= Integer.valueOf(openMinute))) {
                                Toast.makeText(this,
                                        "Opening time must precede closing time. Please refill the form.",
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }

                        String selectedMinuteFormatted =
                                selectedMinute == 0 ? "00" : String.valueOf(selectedMinute);
                        String  selectedHourFormatted =
                                selectedHour <= 9 ? "0" + selectedHour : String.valueOf(selectedHour);
                        String value_to_put = selectedHourFormatted + ":" + selectedMinuteFormatted;
                        closeHours.setText(value_to_put);
                        if (lastUsedKey == null)
                            return;
                        if (open_hours.get(lastUsedKey).contains("-")) {
                            open_hours.put(lastUsedKey,
                                    open_hours.get(lastUsedKey).split("-")[0] + "-" + value_to_put);
                        } else {
                            open_hours.put(lastUsedKey, "-" + value_to_put);
                        }
                    }, hour, minute, true);
            mTimePicker.setTitle("Select closing hour");
            mTimePicker.show();
        });
    }

    private boolean inputIsValid() {
        if (inputNotEmpty()) {
                return true;
        }
        return false;
    }

    private boolean inputNotEmpty(){
        ViewGroup vg = findViewById(R.id.business_sign_up3);
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

    public void sendVerificationEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(BOSignUp3.this,
                                        "Signup successful. Verification email sent",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }

    }

    void uploadLogo(Uri data) {

        if (data == null)
            return;

        final String business_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final StorageReference logosRef = FirebaseStorage.getInstance().getReference()
                .child("business_logos/" + business_id + new Random().nextInt());

        UploadTask uploadTask = logosRef.putFile(data);

        // Register observers to listen for when the download is done or if it fails
        this.uploadTask = uploadTask.addOnSuccessListener(taskSnapshot ->  {
            Log.d(this.toString(), "succeeded to upload business logo");
        });
    }

}
