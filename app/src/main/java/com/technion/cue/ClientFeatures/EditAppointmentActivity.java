package com.technion.cue.ClientFeatures;


import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;


import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


import com.technion.cue.R;
import com.technion.cue.data_classes.Appointment;
import com.technion.cue.data_classes.Business;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.technion.cue.FirebaseCollections.APPOINTMENTS_COLLECTION;
import static com.technion.cue.FirebaseCollections.BUSINESSES_COLLECTION;
import static com.technion.cue.FirebaseCollections.TYPES_COLLECTION;


public class EditAppointmentActivity extends AppCompatActivity
        implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener{

    String business_id;
    String appointment_id;
    FirebaseFirestore db;
    Business business;
    Intent intent;
    String appointment_type;
    Calendar calendar;
    Appointment appointment;
    FirebaseAuth mAuth;
    String radioButton_id;

    /**
    *
     * Business Owner that want to create an NEW appointment*
     * should add bundle with "business_id" and "client_name"*
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
                appointment_id = intent.getExtras().getString("appointment_id");
                business_id = intent.getExtras().getString("business_id");

                loadAppointmentDetails();
            } else if (extras.containsKey("business_id") && extras.containsKey("client_name")){ // newAppointment as business owner

                business_id = intent.getExtras().getString("business_id");
                appointment_id ="";
                appointment_type ="";
                appointment = new Appointment();
                appointment.id="";
                appointment.business_id=business_id;
                appointment.client_id = intent.getExtras().getString("client_name");
                loadNewAppointment();

            } else if (extras.containsKey("business_id")){ // newAppointment as client
                business_id = intent.getExtras().getString("business_id");
                appointment_id ="";
                appointment_type ="";
                // -- setting appointment object
                appointment = new Appointment();
                appointment.id="";
                appointment.business_id=business_id;
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
                .document(business_id)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    business = documentSnapshot.toObject(Business.class);
                    if (business!=null) {
                        TextView locationView = findViewById(R.id.edit_appointment_address_text);
                        locationView.setText(
                                business.location.get("address") +
                                        ", " + business.location.get("city") +
                                        ", " + business.location.get("state")
                        );
                    }


                });
    }

    private void loadTypes() {
        FirebaseFirestore.getInstance()
                .collection(BUSINESSES_COLLECTION)
                .document(business_id)
                .collection(TYPES_COLLECTION)
                .get()
                .addOnSuccessListener(l -> {
                    RadioGroup rg = findViewById(R.id.edit_appoinment_radiogroup);
                    // add a listener to check button
                    rg.setOnCheckedChangeListener((group, checkedId) -> {
                        // This will get the radiobutton that has changed in its check state
                        RadioButton checkedRadioButton = group.findViewById(checkedId);
                        // This puts the value (true/false) into the variable
                        boolean isChecked = checkedRadioButton.isChecked();
                        // If the radiobutton that has changed in check state is now checked...
                        if (isChecked)
                        {
                            appointment_type = l.getDocuments().get(checkedId).getId();
                            radioButton_id = l.getDocuments().get(checkedId).getId();
                        }
                    });
                    // ----
                    // load new radioview  buttons :
                    for (DocumentSnapshot document : l.getDocuments()) {
                        RadioButton[] rb = new RadioButton[l.getDocuments().size()];
                        for(int i=0; i<l.getDocuments().size(); i++){
                            rb[i]  = new RadioButton(this);
                            rg.addView(rb[i]);
                            rb[i].setText(document.getString("bo_name"));
                            // ---
                            rb[i].setId(i);
                            if (document.getId().equals(appointment_type) ) {
                                rb[i].setChecked(true);
                                radioButton_id = document.getId();
                            }
                        }

                    }
                });
    }

    private void loadAppointmentData() {
        db.collection(APPOINTMENTS_COLLECTION)
                .document(appointment_id)
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
                        appointment_type = appointment.type;
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
        if (didChooseType()) {
            ClientChooseDateFragment newFragment = new ClientChooseDateFragment();
            newFragment.show(getSupportFragmentManager(), "datePicker");
        } else {
            Toast.makeText(getBaseContext(),
                    "please choose an appointment type first",
                    Toast.LENGTH_SHORT).show();
        }
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
            if (!appointment.id.equals(""))  {// reschedule  appointment
                appointment.type = radioButton_id;
                appointment.notes ="notes about the appointments - per type";
                appointment.id="";// avoid adding old appointmet_id to  firestore
                      FirebaseFirestore.getInstance()
                            .collection(APPOINTMENTS_COLLECTION)
                            .document()
                            .set(appointment).addOnCompleteListener(task ->
                              FirebaseFirestore.getInstance().collection(APPOINTMENTS_COLLECTION)
                                      .document(appointment_id).delete().addOnSuccessListener(result -> { // deleting old appointment
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
        RadioGroup radioGroup = findViewById(R.id.edit_appoinment_radiogroup);
        return !radioButton_id.equals("");
    }

    // handle the date selected
    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        // store the values selected into a Calendar instance
        calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, monthOfYear);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        ClientChooseTimeFragment newFragment = new ClientChooseTimeFragment();
        newFragment.show(getSupportFragmentManager(), "timePicker");

    }

    // handle the time selected
    @Override
    public void onTimeSet(TimePicker view, int hour, int minute) {
        // store the values selected into a Calendar instance
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        // TODO: will change to a chosen type in Sprint #2
        Date start = calendar.getTime();
        String oldDate =  ((TextView)findViewById(R.id.edit_appointment_time_text)).getText().toString();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM/YYYY");
        ((TextView)findViewById(R.id.edit_appointment_time_text)).setText(sdf.format(calendar.getTime()));
        FirebaseFirestore.getInstance()
                .collection(BUSINESSES_COLLECTION)
                .document(business.id)
                .collection(TYPES_COLLECTION)
                .document(appointment_type)
                .get()
                .addOnSuccessListener(ds -> {
                    calendar.add(Calendar.MINUTE, Integer.valueOf(
                            ((Map<String, String>)ds.get("attributes")).get("duration"))
                    );
                    Date end = calendar.getTime();
                    appointment.date = start;
                    checkIfDateAvailable(start, end, oldDate);
                });
    }


    public void checkIfDateAvailable(Date start, Date end, String oldDate) {
        // TODO : WE NEED TO CHECK TIME PER OPEN HOURS !!

        Map<String, Integer> atm = new HashMap<>();
        Task t = db.collection(BUSINESSES_COLLECTION)
                .document(business.id)
                .collection(TYPES_COLLECTION)
                .get()
                .addOnSuccessListener(qs -> {
                    for (DocumentSnapshot d : qs.getDocuments()) {
                        atm.put(d.getId(), Integer.valueOf(
                                ((Map<String, String>)d.get("attributes")).get("duration"))
                        );
                    }
                });

        findViewById(R.id.loadingPanelEditAppointment).setVisibility(View.VISIBLE);
        // setting up appointment object
        Date old = appointment.date;

        String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};

        db.collection(BUSINESSES_COLLECTION)
                .document(business.id)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                    Calendar c = Calendar.getInstance();
                    c.setTime(start);
                    Map<String, String> open_hours = (Map<String, String>)documentSnapshot.get("open_hours");
                    String currentDayOpenHours = open_hours.get(days[c.get(Calendar.DAY_OF_WEEK) - 1]);
                    try {
                        Date openingHour = sdf.parse(currentDayOpenHours.split("-")[0]);
                        Calendar opening_c = Calendar.getInstance();
                        opening_c.setTime(openingHour);
                        opening_c.set(c.get(Calendar.YEAR),
                                c.get(Calendar.MONTH),
                                c.get(Calendar.DAY_OF_MONTH),
                                opening_c.get(Calendar.HOUR_OF_DAY),
                                opening_c.get(Calendar.MINUTE));
                        Date closingHour = sdf.parse(currentDayOpenHours.split("-")[1]);
                        Calendar closing_c = Calendar.getInstance();
                        closing_c.setTime(closingHour);
                        closing_c.set(c.get(Calendar.YEAR),
                                c.get(Calendar.MONTH),
                                c.get(Calendar.DAY_OF_MONTH),
                                closing_c.get(Calendar.HOUR_OF_DAY),
                                closing_c.get(Calendar.MINUTE));
                        if (end.getTime() > closing_c.getTimeInMillis() ||
                                start.getTime() < opening_c.getTimeInMillis()) {
                            onUnavailableAppointment(oldDate, old);
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                });

        Tasks.whenAll(t).addOnSuccessListener(sl ->
                db.collection(APPOINTMENTS_COLLECTION)
                        .whereEqualTo("business_id",business_id)
                        .orderBy("date")
                        .get().addOnCompleteListener(l -> {
                            findViewById(R.id.loadingPanelEditAppointment).setVisibility(View.GONE);
                            for (DocumentSnapshot document : l.getResult().getDocuments()) {
                                if ((document.exists()) && (document.getId() != appointment_id)) {
                                    Date appointmentDate = ((Timestamp)document.get("date")).toDate();
                                    int duration = atm.get(document.getString("type"));
                                    Calendar c = Calendar.getInstance();
                                    c.setTime(appointmentDate);
                                    c.add(Calendar.MINUTE, duration);

                                    /*
                                    cases where scheduling is impossible:
                                    1) appointment starts in the middle of another appointment
                                    2) appointment ends in the middle of another appointment
                                    3) appointment is scheduled when the business is closed (checked above)
                                     */
                                    if ((start.getTime() > appointmentDate.getTime()
                                            && start.getTime() < c.getTimeInMillis()) ||
                                            ((end.getTime() < c.getTimeInMillis())
                                            && end.getTime() > appointmentDate.getTime())) {
                                        onUnavailableAppointment(oldDate, old);
                                        break;
                                    }
                        }
                    }
                }));
    }

    private void onUnavailableAppointment(String oldDate, Date old) {
        // return to old date in textview
        ((TextView) findViewById(R.id.edit_appointment_time_text))
                .setText(oldDate);
        appointment.date = old;
        //--

        new MaterialAlertDialogBuilder(this)
                .setTitle("Date is not available ")
                .setMessage("Please Choose another one. ")
                .setPositiveButton("Ok", (dialog, id) -> {
                    ClientChooseDateFragment newFragment =
                            new ClientChooseDateFragment();
                    newFragment.show(getSupportFragmentManager(),
                            "datePicker");
                })
                .setNegativeButton("Nevermind", null)
                .show();
        return;
    }
}
