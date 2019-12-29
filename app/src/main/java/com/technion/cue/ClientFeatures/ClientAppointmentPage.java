package com.technion.cue.ClientFeatures;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ParseException;
import android.os.Bundle;
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

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import static com.technion.cue.FirebaseCollections.APPOINTMENTS_COLLECTION;
import static com.technion.cue.FirebaseCollections.BUSINESSES_COLLECTION;
import static com.technion.cue.FirebaseCollections.TYPES_COLLECTION;

public class ClientAppointmentPage extends AppCompatActivity  {

    Intent intent;
    Calendar c;
    String b_name,a_type,a_notes,a_date,a_id,b_id;
    FirebaseFirestore db;
    Business business;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_appointment_page);
        intent = getIntent();
        b_name = intent.getExtras().getString("business_name"); //  appointment id
        a_type = intent.getExtras().getString("appointment_type");
        a_notes = intent.getExtras().getString("appointment_notes");
        a_date = intent.getExtras().getString("appointment_date");
        a_id = intent.getExtras().getString("appointment_id");
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
            getSupportActionBar().setTitle(b_name);

        }
        //TextView business_name = findViewById(R.id.client_appointment_business_Name);
        //business_name.setText(b_name);
        TextView type = findViewById(R.id.client_appointment_business_type);
        type.setText(a_type);
        TextView notes = findViewById(R.id.client_appointment_page_notes_text);
        notes.setText(a_notes);
        TextView date = findViewById(R.id.client_appointment_time_text);
        date.setText(a_date);

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
                                business.location.get("state") + "," +
                                business.location.get("street") + "," +
                                business.location.get("number") + ".");
                    }


                });
    }


    // attach to an onclick handler to show the date picker
    public void reschedThisAppointment(MenuItem v) {
        //System.out.println("----------------------" + a_id);
        Intent intent = new Intent(this, EditAppointmentActivity.class);
        intent.putExtra("appointment_id",a_id);
        intent.putExtra("business_id",b_id);
        startActivity(intent);
        finish();


    }

    public void abortAppointment(MenuItem v) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Cancel this appointment")
                .setMessage("Cancellation is Irreversible. ")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
                        FirebaseFirestore.getInstance().collection(APPOINTMENTS_COLLECTION)
                                .document(a_id).delete().addOnSuccessListener(result -> {
                            findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                            Toast.makeText(getApplicationContext(), "Appointment canceled  Successfully ", Toast.LENGTH_LONG).show();
                            finish();
                    });
                    }
                })
                .setNegativeButton("No",null)
                .show();
    }



}
