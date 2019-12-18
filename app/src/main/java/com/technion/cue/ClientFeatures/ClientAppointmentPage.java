package com.technion.cue.ClientFeatures;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import com.technion.cue.R;
public class ClientAppointmentPage extends AppCompatActivity {
// TODO: BUILD ACTIVITY FUNCTIONALITY.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_appointment_page);
        Intent intent = getIntent();
        String a_id = intent.getExtras().getString("appointment_id"); //  appointment id
        //System.out.println("------- test " + a_id +  "---------------------" );
    }
}
