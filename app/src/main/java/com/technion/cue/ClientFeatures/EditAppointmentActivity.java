package com.technion.cue.ClientFeatures;


import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;


import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


import com.technion.cue.R;
import com.technion.cue.data_classes.Appointment;
import com.technion.cue.data_classes.Business;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static com.technion.cue.FirebaseCollections.APPOINTMENTS_COLLECTION;
import static com.technion.cue.FirebaseCollections.BUSINESSES_COLLECTION;
import static com.technion.cue.FirebaseCollections.TYPES_COLLECTION;


public class EditAppointmentActivity extends AppCompatActivity
        implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener{

    String b_id;
    String a_id;
    FirebaseFirestore db;
    Business business;
    Intent intent;
    String a_type;
    Calendar c;
    Appointment appointment;
    FirebaseAuth mAuth;
    String radioButton_id;

    /**
    *
     * Business Owner that want to create an NEW appointment*
     * should add bundle with "business_id" and "client_id"*
     * Business Owner that want to create an EDIT appointment
     * should add bundle with "appointment_id" and "business_id"*
     *
     * Client  that want to create an NEW appointment*
     * should add bundle with "business_id" *
     * Client that want to create an EDIT appointment
     * should add bundle with "appointment_id" and "business_id"*
    * **/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_appointment);
        findViewById(R.id.loadingPanelEditAppointment).setVisibility(View.GONE);

        db=FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        radioButton_id="";
        intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            if (extras.containsKey("appointment_id") && extras.containsKey("business_id")) { // you are in edit appointment
                a_id = intent.getExtras().getString("appointment_id");
                b_id = intent.getExtras().getString("business_id");

                loadAppointmentDetails();
            } else if (extras.containsKey("business_id") && extras.containsKey("client_id")){ // newAppointment as business owner

                b_id = intent.getExtras().getString("business_id");
                a_id ="";
                a_type="";
                appointment = new Appointment();
                appointment.id="";
                appointment.business_id=b_id;
                appointment.client_id = intent.getExtras().getString("client_id");
                loadNewAppointment();

            } else if (extras.containsKey("business_id")){ // newAppointment as client
                b_id = intent.getExtras().getString("business_id");
                a_id ="";
                a_type="";
                // -- setting appointment object
                appointment = new Appointment();
                appointment.id="";
                appointment.business_id=b_id;
                appointment.client_id = mAuth.getCurrentUser().getUid();
                loadNewAppointment();

            }
        }


    }

    public void loadAppointmentDetails() {
        loadAppointmentData();
        loadBusinessData();

    }

    private void loadBusinessData() {
        db.collection(BUSINESSES_COLLECTION)
                .document(b_id)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    business = documentSnapshot.toObject(Business.class);
                    if (business!=null) {
                        TextView locationView = findViewById(R.id.edit_appointment_address_text);
                        locationView.setText(business.location.get("city") + "," +
                                business.location.get("state") + "," +
                                business.location.get("street") + "," +
                                business.location.get("number") + ".");
                    }


                });
    }

    private void loadTypes() {
        FirebaseFirestore.getInstance()
                .collection(BUSINESSES_COLLECTION)
                .document(b_id)
                .collection(TYPES_COLLECTION)
                .get()
                .addOnSuccessListener(l -> {
                    RadioGroup rg = findViewById(R.id.edit_appoinment_radiogroup);
                    // add a listener to check button
                    rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
                    {
                        public void onCheckedChanged(RadioGroup group, int checkedId)
                        {
                            // This will get the radiobutton that has changed in its check state
                            RadioButton checkedRadioButton = (RadioButton)group.findViewById(checkedId);
                            // This puts the value (true/false) into the variable
                            boolean isChecked = checkedRadioButton.isChecked();
                            // If the radiobutton that has changed in check state is now checked...
                            if (isChecked)
                            {
                                radioButton_id = l.getDocuments().get(checkedId).getId();
                            }
                        }
                    });
                    // ----
                    // load new radioview  buttons :
                    for (DocumentSnapshot document : l.getDocuments()) {
                        RadioButton[] rb = new RadioButton[l.getDocuments().size()];
                        for(int i=0; i<l.getDocuments().size(); i++){
                            rb[i]  = new RadioButton(this);
                            rg.addView(rb[i]);
                            rb[i].setText(document.getString("name"));
                            // ---
                            rb[i].setId(i);
                            if (document.getId().equals(a_type) ) {
                                rb[i].setChecked(true);
                                radioButton_id = document.getId();
                            }
                        }

                    }




                });


    }

    private void loadAppointmentData() {
        db.collection(APPOINTMENTS_COLLECTION)
                .document(a_id)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    appointment = documentSnapshot.toObject(Appointment.class);
                    if (appointment!=null) {
                        appointment.id = documentSnapshot.getId();
                        TextView notes = findViewById(R.id.edit_appointment_notes_text);
                        notes.setText(appointment.notes);
                        TextView date = findViewById(R.id.edit_appointment_time_text);
                        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM/YYYY");
                        date.setText(sdf.format(appointment.date ));
                        a_type = appointment.type;
                    }
                    loadTypes(); // load appointments type

                });
    }
    public void loadNewAppointment() {
        // load business details
        loadBusinessData();
        loadTypes();
        ((TextView)findViewById(R.id.edit_appointment_time_text)).setText("Choose a time");


    }

    public void changeDate(View view) {
        ClientChooseDateFragment newFragment = new ClientChooseDateFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    public void exitEditAppointment(View view) {
        // exit and dont save
        finish();
    }

    public void saveChanges(View view) {
        findViewById(R.id.loadingPanelEditAppointment).setVisibility(View.VISIBLE);

        // exit and save
        if ((((TextView)findViewById(R.id.edit_appointment_time_text)).getText() == "Choose a time") ||
        (!didChooseType())){
            findViewById(R.id.loadingPanelEditAppointment).setVisibility(View.GONE);
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Please Choose a date and Appointment Type")
                    .setMessage("")
                    .setPositiveButton("Ok", null)
                    .setNegativeButton("Nevermind", null)
                    .show();

        } else {
            if (appointment.id != "")  {// reschedule  appointment
                appointment.type = radioButton_id;
                appointment.notes ="notes about the appointments - per type";
                appointment.id="";// avoid adding old appointmet_id to  firestore
                      FirebaseFirestore.getInstance()
                            .collection(APPOINTMENTS_COLLECTION)
                            .document()
                            .set(appointment).addOnCompleteListener(task ->
                              FirebaseFirestore.getInstance().collection(APPOINTMENTS_COLLECTION)
                                      .document(a_id).delete().addOnSuccessListener(result -> { // deleting old appointment
                                findViewById(R.id.loadingPanelEditAppointment).setVisibility(View.GONE);
                                Toast.makeText(getApplicationContext(), "Appointment Rescheduled Successfully ", Toast.LENGTH_LONG).show();
                                // --
                                finish();
                                //--
                            }));


            } else {                                // new appointment
                appointment.notes ="notes about the appointments - per type"; // TODO: need to be change per type
                appointment.type = radioButton_id;
                FirebaseFirestore.getInstance()
                        .collection(APPOINTMENTS_COLLECTION)
                        .document()
                        .set(appointment).addOnCompleteListener(task -> {
                        Toast.makeText(getApplicationContext(), "Appointment scheduled Successfully ", Toast.LENGTH_LONG).show();
                        findViewById(R.id.loadingPanelEditAppointment).setVisibility(View.GONE);
                        finish();
                    });


                }

        }
    }



    @SuppressLint("ResourceType")
    public Boolean didChooseType() {
        RadioGroup rg = findViewById(R.id.edit_appoinment_radiogroup);
        return !radioButton_id.equals("");
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
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
        // TODO: will change to a chosen type in Sprint #2
        Timestamp timestampConvertFrom = new Timestamp(c.getTime());
        String oldDate =  ((TextView)findViewById(R.id.edit_appointment_time_text)).getText().toString();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM/YYYY");
        ((TextView)findViewById(R.id.edit_appointment_time_text)).setText(sdf.format(c.getTime()));
        c.add(Calendar.HOUR,1);       // adding time of type_0
        Timestamp end = new Timestamp(c.getTime());
        checkIfDateAviable(timestampConvertFrom,end,oldDate);

    }


    public void checkIfDateAviable(Timestamp start,Timestamp end,String oldDate) {
            // TODO : WE NEED TO CHECK TIME PER OPEN HOURS !!
        findViewById(R.id.loadingPanelEditAppointment).setVisibility(View.VISIBLE);
        // setting up appointment object
        Date old = appointment.date;
        appointment.date = c.getTime();
       db.collection(APPOINTMENTS_COLLECTION)
               .whereEqualTo("business_id",b_id)
               .whereGreaterThanOrEqualTo("date",start)
               .whereLessThanOrEqualTo("date",end)
               .get().addOnCompleteListener(l -> {
                    findViewById(R.id.loadingPanelEditAppointment).setVisibility(View.GONE);
                    for (DocumentSnapshot document : l.getResult().getDocuments()) {
                        if ((document.exists()) && (document.getId() != a_id)) {
                            // return to old date in textview
                            ((TextView)findViewById(R.id.edit_appointment_time_text)).setText(oldDate);
                            appointment.date = old;
                            //--

                            new MaterialAlertDialogBuilder(this)
                                    .setTitle("Date is not available ")
                                    .setMessage("Please Choose another one. ")
                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {

                                            ClientChooseDateFragment newFragment = new ClientChooseDateFragment();
                                            newFragment.show(getSupportFragmentManager(), "datePicker");


                                        }
                                    })
                                    .setNegativeButton("Nevermind", null)
                                    .show();
                        }
                    }

       });


    }

}
