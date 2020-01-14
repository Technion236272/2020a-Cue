package com.technion.cue.ClientFeatures;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.graphics.drawable.DrawableWrapper;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;


import com.technion.cue.R;
import com.technion.cue.data_classes.Appointment;
import com.technion.cue.BusinessFeatures.BusinessInfoFragment;
import com.technion.cue.data_classes.Business;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import static com.technion.cue.FirebaseCollections.APPOINTMENTS_COLLECTION;
import static com.technion.cue.FirebaseCollections.APPOINTMENT_ACTIONS_COLLECTION;
import static com.technion.cue.FirebaseCollections.BUSINESSES_COLLECTION;
import static com.technion.cue.FirebaseCollections.CLIENTS_COLLECTION;
import static com.technion.cue.FirebaseCollections.TYPES_COLLECTION;

public class ClientAppointmentPage extends AppCompatActivity  {

    Intent intent;
    Calendar c;
    String b_name,a_type,a_notes,a_date,a_id,b_id;
    FirebaseFirestore db;
    Business business;

/*
* Appointment Page for BO and for Client
* HOWTO : put extras "name" - for appointment id
* */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_appointment_page);
        intent = getIntent();
        b_name = intent.getExtras().getString("name"); //  appointment id
        a_type = intent.getExtras().getString("appointment_type");
        a_notes = intent.getExtras().getString("appointment_notes");
        a_date = intent.getExtras().getString("appointment_date");
        a_id = intent.getExtras().getString("notes");
        //System.out.println("----------------------" + a_id);

        b_id = intent.getExtras().getString("business_id");
        db = FirebaseFirestore.getInstance();
        loadBusinessData(b_id);


        // toolbar
       // Toolbar toolbar = (Toolbar) findViewById(R.id.appointment_page_toolbar);
       // setSupportActionBar(toolbar);
        // add back arrow to toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("");
            getSupportActionBar().setElevation(0);

        }
        //TextView name = findViewById(R.id.client_appointment_business_Name);
        //name.setText(b_name);
        TextView type = findViewById(R.id.client_appointment_business_type);
        type.setText(a_type);
        TextView notes = findViewById(R.id.client_appointment_page_notes_text);
        notes.setText(a_notes);
        TextView date = findViewById(R.id.client_appointment_time_text);
        date.setText(a_date);
        TextView title = findViewById(R.id.client_appointment_business_Name);
        title.setText(b_name);

        findViewById(R.id.loadingPanel).setVisibility(View.GONE);


    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;// close this activity and return to preview activity
        }

        return super.onOptionsItemSelected(item);
    }


    // - menu - ben 17.12
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.client_appointment_page_toolbar, menu);
        return true;
    }

    private void loadBusinessData(String business_id) {
        db.collection(BUSINESSES_COLLECTION)
                .document(business_id)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    business = documentSnapshot.toObject(Business.class);
                    if (business!=null) {
                        TextView phoneView = (TextView) findViewById(R.id.client_appointment_phone_text);
                        phoneView.setText(business.phone_number);
                        TextView locationView = (TextView) findViewById(R.id.client_appointment_address_text);
                        locationView.setText(business.location.get("city") + "," +
                                (business.location.get("state") != null ? business.location.get("state") + "," : "" ) +
                                (business.location.get("street") != null ? business.location.get("street") + "," : "" ) +
                                (business.location.get("number") != null ? business.location.get("number") + "" : "" ));

                        SpannableString content = new SpannableString(locationView.getText());
                        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
                        locationView.setText(content);

                        SpannableString contentPhone = new SpannableString(phoneView.getText());
                        contentPhone.setSpan(new UnderlineSpan(), 0, contentPhone.length(), 0);
                        phoneView.setText(contentPhone);


                        //locationView.setTextColor(getResources().getColor(R.color.primaryDarkColor));
                    }


                });
    }


    // attach to an onclick handler to show the date picker
    public void reschedThisAppointment(MenuItem v) {
        Intent intent = new Intent(this, EditAppointmentActivity.class);
        intent.putExtra("notes",a_id);
        intent.putExtra("business_id",b_id);
        startActivity(intent);
        finish();


    }


    public void goToMap(View view) {
        String location = ((TextView)findViewById(R.id.client_appointment_address_text)).getText().toString();
        Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + location);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }


    public void makeACall(View view) {
        String phoneNumber = ((TextView)findViewById(R.id.client_appointment_phone_text)).getText().toString();
        Intent mIntent = new Intent(Intent.ACTION_CALL);
        mIntent.setData(Uri.parse("tel:" + phoneNumber));

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CALL_PHONE},
                    0);

        } else {
            //You already have permission
            try {
                startActivity(mIntent);
            } catch(SecurityException e) {
                e.printStackTrace();
            }
        }

    }



    public void abortAppointment(MenuItem v) {
        String doer;
        if (FirebaseAuth.getInstance().getUid()
                .equals(b_id)) {
            doer = "business";
        } else {
            doer = "client";
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle("Cancel this appointment")
                .setMessage("Cancellation is Irreversible. ")
                .setPositiveButton("Yes", (dialog, id) -> {
                    findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
                    FirebaseFirestore.getInstance().collection(APPOINTMENTS_COLLECTION)
                            .document(a_id).delete().addOnSuccessListener(result -> {
                                FirebaseFirestore.getInstance()
                                        .collection(CLIENTS_COLLECTION)
                                        .document(FirebaseAuth.getInstance().getUid())
                                        .get()
                                        .addOnSuccessListener(ds -> {
                                            SimpleDateFormat sdf =
                                                    new SimpleDateFormat("HH:mm dd/MM/YYYY");
                                            try {
                                                Business.AppointmentAction aa = new Business.AppointmentAction(
                                                        "cancellation",
                                                        ds.getString("name"),
                                                        new Date(),
                                                        sdf.parse(a_date),
                                                        sdf.parse(a_date),
                                                        a_type,
                                                        a_type,
                                                        doer,
                                                        a_id
                                                );
                                                FirebaseFirestore.getInstance()
                                                        .collection(BUSINESSES_COLLECTION)
                                                        .document(b_id)
                                                        .collection(APPOINTMENT_ACTIONS_COLLECTION)
                                                        .document()
                                                        .set(aa);
                                            } catch (ParseException e) {
                                                e.printStackTrace();
                                            }
                                        });

                        findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                        Toast.makeText(getApplicationContext(), "Appointment canceled  Successfully ", Toast.LENGTH_LONG).show();
                        finish();
                });
                })
                .setNegativeButton("No",null)
                .show();
    }



}
