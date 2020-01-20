package com.technion.cue.ClientFeatures;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.technion.cue.LauncherActivity;
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
import static com.technion.cue.FirebaseCollections.APPOINTMENT_ACTIONS_COLLECTION;
import static com.technion.cue.FirebaseCollections.BUSINESSES_COLLECTION;
import static com.technion.cue.FirebaseCollections.CLIENTELE_COLLECTION;
import static com.technion.cue.FirebaseCollections.CLIENTS_COLLECTION;
import static com.technion.cue.FirebaseCollections.TYPES_COLLECTION;

enum UserType {
    BusinessOwner,
    Client
}
public class EditAppointmentActivity extends AppCompatActivity
        implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener{

    FirebaseFirestore db;
    Business business;
    Intent intent;
    Calendar calendar;
    Appointment appointment;
    FirebaseAuth mAuth;
    String radioButton_id;
    Map<Integer, String> typesIdAndNotes = new HashMap<>();
    UserType userType;
    Boolean changed=false;
    Boolean firstEdit;// needed to change
    private Date old_appointment_date;
    private String old_appointment_type;

    /**
    *
     * Business Owner that want to create an NEW appointment*
     * should add bundle with "business_id" and "client_name"*
     * Business Owner that want to create an EDIT appointment
     * should add bundle with "notes" and "business_id"*
     *
     * Client  that want to create an NEW appointment*
     * should add bundle with "business_id" *
     * Client that want to create an EDIT appointment
     * should add bundle with "notes" and "business_id"*
    * **/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_appointment);

        db=FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        appointment = new Appointment();
        radioButton_id="";
        firstEdit=true;
        intent = getIntent();



        Bundle extras = intent.getExtras();
        if (extras != null) {
            if (extras.containsKey("appointment_id") && extras.containsKey("business_id")) { // you are in edit appointment
                changed=false;
                appointment.id = intent.getExtras().getString("appointment_id");
                appointment.business_id = intent.getExtras().getString("business_id");
                loadAppointmentDetails();
            } else if (extras.containsKey("business_id") && extras.containsKey("client_name")){ // newAppointment as business owner

                userType = UserType.BusinessOwner;
                appointment.id = "";
                appointment.business_id = intent.getExtras().getString("business_id");
                appointment.type="";
                appointment.client_id = intent.getExtras().getString("client_name");
                ((RelativeLayout)findViewById(R.id.delete_button_bottom)).setVisibility(View.GONE);

                ((TextView)findViewById(R.id.noteTitle)).setVisibility(View.GONE);
                ((TextView)findViewById(R.id.edit_appointment_notes_text)).setVisibility(View.INVISIBLE);
                findViewById(R.id.edit_appointment_note_laylout).setVisibility(View.VISIBLE);
                changed=true;
                loadNewAppointment();
            } else if (extras.containsKey("business_id")){ // newAppointment as client
                changed=true;
                userType = UserType.Client;
                appointment.id="";
                appointment.type="";
                appointment.business_id=intent.getExtras().getString("business_id");
                appointment.client_id = mAuth.getCurrentUser().getUid();
                loadNewAppointment();
                findViewById(R.id.delete_button_bottom).setVisibility(View.GONE);

            }
        } else {

            Intent intent = new Intent(this, LauncherActivity.class);
            startActivity(intent);
            finish();
        }

        createNotificationChannel();
        /** Set Action Bar */
        ActionBar actionBar = getSupportActionBar();
        actionBar.setCustomView(R.layout.edit_appointment_actionbar);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);



    }

    public void loadAppointmentDetails() {
        loadAppointmentData();
        loadBusinessData();
    }

    private void loadBusinessData() {
        db.collection(BUSINESSES_COLLECTION)
                .document(appointment.business_id)
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
                .document(appointment.business_id)
                .collection(TYPES_COLLECTION)
                .get()
                .addOnSuccessListener(l -> {
                    ChipGroup rg = findViewById(R.id.edit_appoinment_radiogroup);
                    // add a listener to check button
                    rg.setOnCheckedChangeListener((group, checkedId) -> {
                        if (checkedId != -1) {
                            if (firstEdit == false ) {
                                appointment.notes = typesIdAndNotes.get(checkedId);
                                changed = true;
                                ((TextView)findViewById(R.id.edit_appointment_time_text)).setText("Choose a Date");
                                changeDate(findViewById(R.id.edit_appointment_time_text));
                            }
                            else {
                                firstEdit=false;
                            }
                            appointment.type = l.getDocuments().get(checkedId).getId();
                            if (userType == UserType.BusinessOwner) {
                                ((com.google.android.material.textfield.TextInputEditText)(findViewById(R.id.edit_appointment_notes_text_edit_text))).setText(appointment.notes);
                            } else {
                                    ((TextView) findViewById(R.id.edit_appointment_notes_text)).setText(appointment.notes);
                                    firstEdit = false;

                            }
                            radioButton_id = l.getDocuments().get(checkedId).getId();
                        } else {
                            radioButton_id="";
                            appointment.type="";
                        }

                    });
                    int i=0;
                    for (DocumentSnapshot document : l.getDocuments()) {

                        Chip[] rb = new Chip[l.getDocuments().size()];
                            rb[i]  = (Chip)getLayoutInflater().inflate(R.layout.layout_chip_choice, rg, false);
                            rb[i].setText(document.getString("name"));
                            rg.addView(rb[i]);
                            // Get types notes :
                            typesIdAndNotes.put(i, (((Map<String, String>)document.get("attributes")).get("notes" +
                                    "") != null ? ((Map<String, String>)document.get("attributes")).get("notes") : "No notes yet."));
                            rb[i].setId(i);
                            if (document.getId().equals(appointment.type) ) {
                                rg.check(rb[i].getId());
                                radioButton_id = document.getId();
                            }
                            i++;
                    }
                    findViewById(R.id.client_edit_appointment_progress_bar).setVisibility(View.GONE);
                });
    }


    private void loadAppointmentData() {

        db.collection(APPOINTMENTS_COLLECTION)
                .document(appointment.id)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    appointment = documentSnapshot.toObject(Appointment.class);
                    if (appointment!=null) {
                        old_appointment_date = appointment.date;
                        old_appointment_type = appointment.type;
                        if (appointment.business_id.equals(mAuth.getUid())) {
                            userType = UserType.BusinessOwner;

                            findViewById(R.id.noteTitle).setVisibility(View.GONE);
                            findViewById(R.id.edit_appointment_notes_text).setVisibility(View.INVISIBLE);
                            findViewById(R.id.edit_appointment_note_laylout).setVisibility(View.VISIBLE);
                            ((EditText)findViewById(R.id.edit_appointment_notes_text_edit_text)).addTextChangedListener(new TextWatcher() {
                                @Override
                                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                                }

                                @Override
                                public void onTextChanged(CharSequence s, int start, int before, int count) {
                                    changed = true;
                                }

                                @Override
                                public void afterTextChanged(Editable s) {

                                }
                            });
                            ((com.google.android.material.textfield.TextInputEditText)(findViewById(R.id.edit_appointment_notes_text_edit_text))).setText(appointment.notes);

                        } else {
                            userType = UserType.Client;
                            ((TextView)findViewById(R.id.edit_appointment_notes_text)).setText(appointment.notes);
                        }

                        appointment.id = documentSnapshot.getId();

                        TextView date = findViewById(R.id.edit_appointment_time_text);
                        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM/YYYY");
                        date.setText(sdf.format(appointment.date ));
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

        findViewById(R.id.client_edit_appointment_progress_bar).setVisibility(View.VISIBLE);
        findViewById(R.id.edit_appointment_save_button).setClickable(false);
        if (changed) {
            String doer;
            if (FirebaseAuth.getInstance().getUid()
                    .equals(appointment.business_id)) {
                doer = "business";
            } else {
                doer = "client";
            }


            if ((((TextView) findViewById(R.id.edit_appointment_time_text)).getText().equals("Choose a Date")) ||
                    ((TextView) findViewById(R.id.edit_appointment_time_text)).getText().equals("") ||
                    ((TextView) findViewById(R.id.edit_appointment_time_text)).getText() == null ||
                    (!didChooseType())) {
                findViewById(R.id.client_edit_appointment_progress_bar).setVisibility(View.GONE);
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Please Choose a date and Appointment Type")
                        .setMessage("")
                        .setPositiveButton("Ok", null)
                        .setNegativeButton("Cancel", null)
                        .show();

            } else {
                //appointment.date = new Timestamp(((TextView) findViewById(R.id.edit_appointment_time_text)).getText().toString());
                if (userType == UserType.BusinessOwner) {
                    appointment.notes = ((com.google.android.material.textfield.TextInputEditText) findViewById(R.id.edit_appointment_notes_text_edit_text)).getText().toString();
                }

                if (!appointment.id.equals("")) {// reschedule  appointment
                    appointment.type = radioButton_id;
                    String oldAppointmentId = appointment.id;
                    appointment.id = "";// avoid adding old appointmet_id to  firestore
                    FirebaseFirestore.getInstance()
                            .collection(APPOINTMENTS_COLLECTION)
                            .document()
                            .set(appointment).addOnCompleteListener(task ->
                            FirebaseFirestore.getInstance().collection(APPOINTMENTS_COLLECTION)
                                    .document(oldAppointmentId).delete().addOnSuccessListener(result -> { // deleting old appointment
                                findViewById(R.id.client_edit_appointment_progress_bar).setVisibility(View.GONE);
                                Toast.makeText(getApplicationContext(), "Appointment Rescheduled Successfully ", Toast.LENGTH_LONG).show();
                                // --

                                finish();
                                //--
                            }));

                      /*
                      additions by Ophir on 6/1
                       */

                    // Awful everything D:
                    FirebaseFirestore.getInstance()
                            .collection(CLIENTS_COLLECTION)
                            .document(appointment.client_id)
                            .get()
                            .addOnSuccessListener(client  ->
                                    FirebaseFirestore.getInstance()
                                    .collection(BUSINESSES_COLLECTION)
                                    .document(appointment.business_id)
                                    .collection(TYPES_COLLECTION)
                                    .document(appointment.type)
                                    .get()
                                    .addOnSuccessListener(type -> {
                                        String notes;
                                        if (appointment.notes.equals("No notes yet.")) {
                                            notes = type.getString("notes");
                                        } else {
                                            notes = appointment.notes;
                                        }
                                        FirebaseFirestore.getInstance()
                                                .collection(BUSINESSES_COLLECTION)
                                                .document(appointment.business_id)
                                                .collection(TYPES_COLLECTION)
                                                .document(old_appointment_type)
                                                .get()
                                                .addOnSuccessListener(old_type -> {
                                                    Business.AppointmentAction aa =
                                                            new Business.AppointmentAction(
                                                                    "rescheduling",
                                                                    client.getString("name"),
                                                                    new Date(),
                                                                    old_appointment_date,
                                                                    appointment.date,
                                                                    type.getString("name"),
                                                                    old_type.getString("name"),
                                                                    doer,
                                                                    notes
                                                            );


                                                    FirebaseFirestore.getInstance()
                                                            .collection(BUSINESSES_COLLECTION)
                                                            .document(appointment.business_id)
                                                            .collection(APPOINTMENT_ACTIONS_COLLECTION)
                                                            .document()
                                                            .set(aa);
                                                });

                                    }));
                } else {                                // new appointment
                    appointment.type = radioButton_id;

                   // if (appointment.notes !=null) {  appointment.notes="No notes yet." ;}

                    FirebaseFirestore.getInstance()
                            .collection(APPOINTMENTS_COLLECTION)
                            .document()
                            .set(appointment).addOnCompleteListener(task -> {

                        Toast.makeText(getApplicationContext(), "Appointment scheduled Successfully ", Toast.LENGTH_LONG).show();
                        findViewById(R.id.client_edit_appointment_progress_bar).setVisibility(View.GONE);
                        finish();
                    });

                /*
                additions by Ophir on 8/1
                */

                    FirebaseFirestore.getInstance()
                            .collection(CLIENTS_COLLECTION)
                            .document(appointment.client_id)
                            .get()
                            .addOnSuccessListener(client  ->
                                    FirebaseFirestore.getInstance()
                                            .collection(BUSINESSES_COLLECTION)
                                            .document(appointment.business_id)
                                            .collection(TYPES_COLLECTION)
                                            .document(appointment.type)
                                            .get()
                                            .addOnSuccessListener(type -> {
                                                String notes;

                                                if ((appointment.notes == null) || appointment.notes.equals("No notes yet.")) {

                                                    notes = type.getString("notes");
                                                } else {
                                                    notes = appointment.notes;
                                                }
                                                Business.AppointmentAction aa =
                                                        new Business.AppointmentAction(
                                                                "scheduling",
                                                                client.getString("name"),
                                                                new Date(),
                                                                appointment.date,
                                                                appointment.date,
                                                                type.getString("name"),
                                                                type.getString("name"),
                                                                doer,
                                                                notes
                                                        );


                                                FirebaseFirestore.getInstance()
                                                        .collection(BUSINESSES_COLLECTION)
                                                        .document(appointment.business_id)
                                                        .collection(APPOINTMENT_ACTIONS_COLLECTION)
                                                        .document()
                                                        .set(aa);


                                                FirebaseFirestore.getInstance()
                                                        .collection(BUSINESSES_COLLECTION)
                                                        .document(appointment.business_id)
                                                        .collection(CLIENTELE_COLLECTION)
                                                        .whereEqualTo("client_id", appointment.client_id)
                                                        .get()
                                                        .addOnSuccessListener(ds -> {
                                                            if (ds.isEmpty())
                                                                FirebaseFirestore.getInstance()
                                                                        .collection(BUSINESSES_COLLECTION)
                                                                        .document(appointment.business_id)
                                                                        .collection(CLIENTELE_COLLECTION)
                                                                        .document()
                                                                        .set(new Business.ClienteleMember(appointment.client_id, client.getString("name")));
                                                        });

                                            }));
                }

            }
        } else {
            finish();
        }
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "close appointment reminder";
            String description = "reminder for an appointment 24 hours from now";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("0", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @SuppressLint("ResourceType")
    public Boolean didChooseType() {
        ChipGroup radioGroup = findViewById(R.id.edit_appoinment_radiogroup);
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
                .document(appointment.type)
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

        findViewById(R.id.client_edit_appointment_progress_bar).setVisibility(View.VISIBLE);
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
                        if (currentDayOpenHours != "") {
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
                                onUnavailableAppointment(oldDate, old, "Open hours in this day are " + currentDayOpenHours);
                            } else {
                                db.collection(APPOINTMENTS_COLLECTION)
                                        .whereEqualTo("business_id", appointment.business_id)
                                        .orderBy("date")
                                        .get().addOnCompleteListener(l -> {
                                    findViewById(R.id.client_edit_appointment_progress_bar).setVisibility(View.GONE);
                                    if (l.getResult().getDocuments().isEmpty())
                                        changed = true;
                                    for (DocumentSnapshot document : l.getResult().getDocuments()) {
                                        if ((document.exists()) && (document.getId() != appointment.id)) {
                                            Date appointmentDate = ((Timestamp) document.get("date")).toDate();
                                            int duration = atm.get(document.getString("type"));
                                            Calendar c2 = Calendar.getInstance();
                                            c2.setTime(appointmentDate);
                                            c2.add(Calendar.MINUTE, duration);

                                    /*
                                    cases where scheduling is impossible:
                                    1) appointment starts in the middle of another appointment
                                    2) appointment ends in the middle of another appointment
                                    3) appointment is scheduled when the business is closed (checked above)
                                     */
                                            if ((start.getTime() > appointmentDate.getTime()
                                                    && start.getTime() < c2.getTimeInMillis()) ||
                                                    ((end.getTime() < c2.getTimeInMillis())
                                                            && end.getTime() > appointmentDate.getTime())) {
                                                onUnavailableAppointment(oldDate, old, "Choose another time. Open hour in this day are " + currentDayOpenHours);

                                                break;
                                            } else {
                                                changed = true;
                                            }
                                        }
                                    }
                                });
                            }

                        } else {

                            onUnavailableAppointment(oldDate, old, "Business not open.  ");


                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                });
//
//        Tasks.whenAllSuccess(t).addOnSuccessListener(sl ->
//               );
    }

    private void onUnavailableAppointment(String oldDate, Date old,String message) {
        // return to old date in textview
        ((TextView) findViewById(R.id.edit_appointment_time_text))
                .setText(oldDate);

        appointment.date = old;
        //--

        new MaterialAlertDialogBuilder(this)
                .setTitle("Date is not available ")
                .setMessage(message)
                .setPositiveButton("Ok", (dialog, id) -> {
                    ClientChooseDateFragment newFragment =
                            new ClientChooseDateFragment();
                    newFragment.show(getSupportFragmentManager(),
                            "datePicker");
                })
                .setNegativeButton("Cancel", null)
                .show();
        findViewById(R.id.client_edit_appointment_progress_bar).setVisibility(View.GONE);
        return;

    }


    public void abortAppointment(View view) {
        findViewById(R.id.delete_icon).setClickable(false);
        String doer;
        if (FirebaseAuth.getInstance().getUid()
                .equals(appointment.business_id)) {
            doer = "business";
        } else {
            doer = "client";
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle("Cancel this appointment")
                .setMessage("Cancellation is Irreversible. ")
                .setPositiveButton("Yes", (dialog, id) -> {

                    FirebaseFirestore.getInstance().collection(APPOINTMENTS_COLLECTION)
                            .document(appointment.id).delete().addOnSuccessListener(result -> {
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
                                                sdf.parse(appointment.date.toString()),
                                                sdf.parse(appointment.date.toString()),
                                                appointment.type,
                                                appointment.type,
                                                doer,
                                                appointment.id
                                        );
                                        FirebaseFirestore.getInstance()
                                                .collection(BUSINESSES_COLLECTION)
                                                .document(appointment.business_id)
                                                .collection(APPOINTMENT_ACTIONS_COLLECTION)
                                                .document()
                                                .set(aa);
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                });

                        Toast.makeText(getApplicationContext(), "Appointment canceled  Successfully ", Toast.LENGTH_LONG).show();
                        finish();
                    });
                })
                .setNegativeButton("No",null)
                .show();
    }
}
