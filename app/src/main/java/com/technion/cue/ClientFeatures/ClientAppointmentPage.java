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

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import static com.technion.cue.FirebaseCollections.APPOINTMENTS_COLLECTION;
import static com.technion.cue.FirebaseCollections.BUSINESSES_COLLECTION;
import static com.technion.cue.FirebaseCollections.TYPES_COLLECTION;

public class ClientAppointmentPage extends AppCompatActivity  implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
// TODO: CHECK AVIBILITY OF THE EDITED APPOINTMENT
    Intent intent;
    Calendar c;
    String b_name,a_type,a_notes,a_date,a_id,b_id;
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
        b_id = intent.getExtras().getString("business_id");
        // toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.appointment_page_toolbar);
        setSupportActionBar(toolbar);
        // add back arrow to toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(b_name);

        }
        TextView type = findViewById(R.id.client_appointment_page_type);
        type.setText(a_type);
        TextView notes = findViewById(R.id.client_appointment_page_notes);
        notes.setText(a_notes);
        TextView date = findViewById(R.id.client_appointment_page_date);
        date.setText(a_date);

        findViewById(R.id.loadingPanel).setVisibility(View.GONE);


    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity
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


    // attach to an onclick handler to show the date picker
    public void reschedThisAppointment(MenuItem v) {
        ClientChooseDateFragment newFragment = new ClientChooseDateFragment();

        newFragment.show(getSupportFragmentManager(), "datePicker");

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




    // handle the date selected
    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        // store the values selected into a Calendar instance
        c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, monthOfYear);
        c.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        ClientChooseTimeFragment newFragment = new ClientChooseTimeFragment();
        newFragment.show(getSupportFragmentManager(), "timePicker");

    }

    // handle the time selected
    @Override
    public void onTimeSet(TimePicker view, int hour, int minute) {
        // store the values selected into a Calendar instance
        c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
        findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
        // TODO: will change to a chosen type in Sprint #2
        Timestamp timestampConvertFrom = new Timestamp(c.getTime());
        FirebaseFirestore.getInstance()
                .collection(BUSINESSES_COLLECTION)
                .document(b_id)
                .collection(TYPES_COLLECTION)
                .whereEqualTo("name", "type_0")
                .get()
                .addOnSuccessListener(l -> {
                    Appointment appointment = new Appointment(b_id,
                            FirebaseAuth.getInstance().getCurrentUser().getUid(),
                            l.getDocuments().get(0).getId(), "BO notes per appointment type" ,timestampConvertFrom,a_id);
                    FirebaseFirestore.getInstance()
                            .collection(APPOINTMENTS_COLLECTION)
                            .document()
                            .set(appointment).addOnCompleteListener(task -> {

                        FirebaseFirestore.getInstance().collection(APPOINTMENTS_COLLECTION)
                                .document(a_id).delete().addOnSuccessListener(result ->{
                                    findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                                    Toast.makeText(getApplicationContext(),"Appointment Reschedualed Successfully ",Toast.LENGTH_LONG).show();
                                    finish();
                        });


                    });
                });


    }

}
