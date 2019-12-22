package com.technion.cue.ClientFeatures;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.firebase.Timestamp;
import com.technion.cue.R;
import com.technion.cue.data_classes.Appointment;

public class ClientAppointmentPage extends AppCompatActivity {
// TODO: BUILD ACTIVITY FUNCTIONALITY.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_appointment_page);
        Intent intent = getIntent();
        String b_name = intent.getExtras().getString("business_name"); //  appointment id
        String a_type = intent.getExtras().getString("appointment_type");
        String a_notes = intent.getExtras().getString("appointment_notes");
        String a_date = intent.getExtras().getString("appointment_date");
        // toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.appointment_page_toolbar);
        setSupportActionBar(toolbar);
        // add back arrow to toolbar
        if (getSupportActionBar() != null){
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


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        } else if (item.getItemId() == R.id.client_appointment_toolbar_edit) { // edit button

        } else { // cancel meeting

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


}
