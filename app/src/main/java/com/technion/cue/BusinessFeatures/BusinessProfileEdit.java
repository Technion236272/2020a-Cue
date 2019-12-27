package com.technion.cue.BusinessFeatures;

import androidx.appcompat.app.AppCompatActivity;


import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.technion.cue.R;
import com.technion.cue.annotations.ModuleAuthor;
import com.technion.cue.data_classes.Business;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.technion.cue.FirebaseCollections.BUSINESSES_COLLECTION;

@ModuleAuthor("Ophir Eyal")
public class BusinessProfileEdit extends AppCompatActivity {

    private static final int GET_LOGO = 0;
    private BusinessUploader uploader;
    private Uri logoData;
    private Map<String, String> open_hours = new HashMap<>();
    private String[] days =
            {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
    private String lastUsedKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_profile_edit);
        TextInputEditText businessName = findViewById(R.id.businessNameEditText);
        TextInputEditText businessDescription = findViewById(R.id.businessDescriptionEditText);
        TextInputEditText phone = findViewById(R.id.businessPhoneEditText);
        TextInputEditText state = findViewById(R.id.businessStateEditText);
        TextInputEditText city = findViewById(R.id.businessCityEditText);
        TextInputEditText address = findViewById(R.id.businessAddressEditText);
        Business business = (Business) getIntent().getSerializableExtra("business");
        Uri logoData = getIntent().getData();
        open_hours = business.open_hours;
        businessName.setText(business.business_name);
        businessDescription.setText(business.description);
        phone.setText(business.phone_number);
        state.setText(business.location.get("state"));
        city.setText(business.location.get("city"));
        address.setText(business.location.get("address"));
        uploader = new BusinessUploader(business, logoData, findViewById(R.id.businessLogoEdit));
        uploader.loadLogo();

        AutoCompleteTextView openHoursDays = findViewById(R.id.days_filled_exposed_dropdown);
        openHoursDays.setAdapter(new ArrayAdapter<>(this,
                R.layout.dropdown_menu_popup_item, days));

        TextView openHours = findViewById(R.id.openingHoursEditText);
        TextView closeHours = findViewById(R.id.closingHoursEditText);
        openHours.setClickable(false);
        closeHours.setClickable(false);

        openHoursDays.setOnItemClickListener((parent, view, position, id) -> {

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
            } else {
                FirebaseFirestore.getInstance()
                        .collection(BUSINESSES_COLLECTION)
                        .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .get()
                        .addOnSuccessListener(ds -> {
                            Business b = ds.toObject(Business.class);
                            String hours = b.open_hours.get(key);
                            open_hours.put(key, hours);
                            String open_hour = "", close_hour = "";
                            if (hours.contains("-")) {
                                open_hour = hours.split("-")[0];
                                close_hour = hours.split("-")[1];
                            }
                            openHours.setText(open_hour);
                            closeHours.setText(close_hour);
                            lastUsedKey = key;
                        });
            }

            openHours.setClickable(true);
            closeHours.setClickable(true);

        });

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

    /**
     * opens up the phone's gallery for picture upload
     */
    public void uploadLogoToFireBase(View view) {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, GET_LOGO);
    }

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        if (reqCode == GET_LOGO && data != null) {
            logoData = data.getData();
            final InputStream imageStream;
            try {
                imageStream = getContentResolver().openInputStream(logoData);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                CircularImageView logo = findViewById(R.id.businessLogoEdit);
                logo.setImageBitmap(selectedImage);
            } catch (FileNotFoundException e) {
                Toast.makeText(this, "image not found", Toast.LENGTH_SHORT).show();
            }

        }
    }

    public void saveChanges(View view) {
        Intent intent = new Intent();
        TextInputEditText businessName = findViewById(R.id.businessNameEditText);
        TextInputEditText businessDescription = findViewById(R.id.businessDescriptionEditText);
        TextInputEditText phone = findViewById(R.id.businessPhoneEditText);
        TextInputEditText state = findViewById(R.id.businessStateEditText);
        TextInputEditText city = findViewById(R.id.businessCityEditText);
        TextInputEditText address = findViewById(R.id.businessAddressEditText);
        uploader.business.business_name = businessName.getText().toString();
        uploader.business.description = businessDescription.getText().toString();
        uploader.business.phone_number = phone.getText().toString();
        uploader.business.location.put("state", state.getText().toString());
        uploader.business.location.put("city", city.getText().toString());
        uploader.business.location.put("address", address.getText().toString());
        uploader.business.open_hours = open_hours;
        uploader.uploadLogo(logoData);
        uploader.updateBusiness();
        intent.setData(logoData);
        intent.putExtra("business", uploader.business);
        setResult(RESULT_OK, intent);
        finish();
    }
}
