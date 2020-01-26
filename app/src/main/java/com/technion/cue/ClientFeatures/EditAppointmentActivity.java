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
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.dialog.MaterialDialogs;
import com.google.android.material.radiobutton.MaterialRadioButton;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.technion.cue.FirebaseCollections.APPOINTMENTS_COLLECTION;
import static com.technion.cue.FirebaseCollections.APPOINTMENT_ACTIONS_COLLECTION;
import static com.technion.cue.FirebaseCollections.BUSINESSES_COLLECTION;
import static com.technion.cue.FirebaseCollections.CLIENTELE_COLLECTION;
import static com.technion.cue.FirebaseCollections.CLIENTS_COLLECTION;
import static com.technion.cue.FirebaseCollections.REVIEWS_COLLECTION;
import static com.technion.cue.FirebaseCollections.TYPES_COLLECTION;



enum UserType {
    BusinessOwner,
    Client
}
public class EditAppointmentActivity extends AppCompatActivity
        implements DatePickerDialog.OnDateSetListener {

    FirebaseFirestore db;
    Business business;
    Intent intent;
    Calendar calendar;
    Appointment appointment;
    FirebaseAuth mAuth;
    String radioButton_id;
    Map<Integer, String> typesIdAndNotes = new HashMap<>();
    UserType userType;
    Boolean firstEdit = false;// needed to change
    Boolean editAppointment = false;
    Boolean setDate = false;
    Boolean setChip = false;
    Map<String, Integer> atm; // Contain type + duration
    long oneMinInMilisec = 60000;
    private Date old_appointment_date;
    private String old_appointment_type;

    /**
     * Business Owner that want to create an NEW appointment*
     * should add bundle with "business_id" and "client_name"*
     * Business Owner that want to create an EDIT appointment
     * should add bundle with "notes" and "business_id"*
     * <p>
     * Client  that want to create an NEW appointment*
     * should add bundle with "business_id" *
     * Client that want to create an EDIT appointment
     * should add bundle with "notes" and "business_id"*
     **/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_appointment);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        appointment = new Appointment();
        radioButton_id = "";
        firstEdit = true;
        intent = getIntent();


        Bundle extras = intent.getExtras();
        if (extras != null) {
            if (extras.containsKey("appointment_id") && extras.containsKey("business_id")) { // you are in edit appointment
                appointment.id = intent.getExtras().getString("appointment_id");
                appointment.business_id = intent.getExtras().getString("business_id");
                setDate = true;
                editAppointment = true;
                loadAppointmentDetails();
            } else if (extras.containsKey("business_id") && extras.containsKey("client_name")) { // newAppointment as business owner
                editAppointment = false;
                userType = UserType.BusinessOwner;
                appointment.id = "";
                appointment.business_id = intent.getExtras().getString("business_id");
                appointment.type = "";
                appointment.client_id = intent.getExtras().getString("client_name");
                ((RelativeLayout) findViewById(R.id.delete_button_bottom)).setVisibility(View.GONE);

                ((TextView) findViewById(R.id.noteTitle)).setVisibility(View.GONE);
                ((TextView) findViewById(R.id.edit_appointment_notes_text)).setVisibility(View.INVISIBLE);
                findViewById(R.id.edit_appointment_note_laylout).setVisibility(View.VISIBLE);
                loadNewAppointment();
            } else if (extras.containsKey("business_id")) { // newAppointment as client
                editAppointment = false;
                userType = UserType.Client;
                appointment.id = "";
                appointment.type = "";
                appointment.business_id = intent.getExtras().getString("business_id");
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

    /**
     * loadAvailableAppointment - Mange possible Appointments times
     * by the type the user choose and the day of month(calendar).
     * and show a list of radio buttons which user can choose from.
     */

    public void loadAvailableAppointment(Calendar calendar) {
        // Loading optional appointments time
        // Depends on appointment type chosen(each type has different duration)
        final ArrayList<Long> possibleAppointments = new ArrayList<>(); // EVERY APPOINTMENT DATE WILL BE IN MILLISEC;

        final Calendar opening_c = Calendar.getInstance();
        final Calendar closing_c = Calendar.getInstance();

        int duration;
        // First we fix duration - if 21 then it will be 25 , if it 26 then it will be 30
        if (atm.get(appointment.type) % 10 != 0) {
            duration = (atm.get(appointment.type) % 10 > 5 ? atm.get(appointment.type) + (10 - atm.get(appointment.type) % 10) : atm.get(appointment.type) + (5 - atm.get(appointment.type) % 10));
        } else {
            duration = atm.get(appointment.type);
        }

        String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};

        Task buildArrayOfPossibleAppointments = db.collection(BUSINESSES_COLLECTION)
                .document(business.id)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                    Calendar c = calendar;
                    Map<String, String> open_hours = (Map<String, String>) documentSnapshot.get("open_hours");
                    String currentDayOpenHours = open_hours.get(days[c.get(Calendar.DAY_OF_WEEK) - 1]);
                    try {       // Get openning and closing time in this specific day
                        if (currentDayOpenHours != "") {
                            Date openingHour = sdf.parse(currentDayOpenHours.split("-")[0]);
                            opening_c.setTime(openingHour);
                            opening_c.set(c.get(Calendar.YEAR),
                                    c.get(Calendar.MONTH),
                                    c.get(Calendar.DAY_OF_MONTH),
                                    opening_c.get(Calendar.HOUR_OF_DAY),
                                    opening_c.get(Calendar.MINUTE));
                            Date closingHour = sdf.parse(currentDayOpenHours.split("-")[1]);
                            closing_c.setTime(closingHour);
                            closing_c.set(c.get(Calendar.YEAR),
                                    c.get(Calendar.MONTH),
                                    c.get(Calendar.DAY_OF_MONTH),
                                    closing_c.get(Calendar.HOUR_OF_DAY),
                                    closing_c.get(Calendar.MINUTE));

                            Calendar todayDate = Calendar.getInstance();
                            Calendar openTimeTmp = Calendar.getInstance();
                            openTimeTmp.setTime((opening_c.getTime()));
                            // build data structure that will contain all possible appointments in this day
                            // First, Check if the date is today - if so we will allow only future time
                            if ((todayDate.get(Calendar.MONTH) == opening_c.get(Calendar.MONTH)) &&
                                    (todayDate.get(Calendar.DAY_OF_MONTH) == opening_c.get(Calendar.DAY_OF_MONTH)) &&
                                    (todayDate.get(Calendar.YEAR) == opening_c.get(Calendar.YEAR))) {
                                while (openTimeTmp.getTimeInMillis() < closing_c.getTimeInMillis()) {
                                    if (calendar.getTimeInMillis() < openTimeTmp.getTimeInMillis()) {
                                        possibleAppointments.add(openTimeTmp.getTimeInMillis());
                                    }
                                    openTimeTmp.add(Calendar.MINUTE, duration);
                                }

                            } else {
                                while (openTimeTmp.getTimeInMillis() < closing_c.getTimeInMillis()) {
                                    possibleAppointments.add(openTimeTmp.getTimeInMillis());
                                    openTimeTmp.add(Calendar.MINUTE, duration);
                                }
                            }

                        } else {
                            setDate = false;
//                                errorDialog("Sorry, Business Close. Choose another day.");
                            return;
                        }
                    } catch (ParseException e) {
                        errorDialog("Parsing Failed! Relanuch app.");
                        return;
                    }
                });

        // now that i have a list of all possible appointment time, let remove
        // the ones that already been schedual
        // Remove any appointment from possibleAppointments that is not possible
        Tasks.whenAll(buildArrayOfPossibleAppointments).addOnSuccessListener(l -> {
            db.collection(APPOINTMENTS_COLLECTION)
                    .whereEqualTo("business_id", business.id)
                    .whereLessThanOrEqualTo("date", new Timestamp(closing_c.getTime()))
                    .whereGreaterThanOrEqualTo("date", new Timestamp(opening_c.getTime()))
                    .get().addOnSuccessListener(data -> {
                // We will use "atm" to find each type duration
                for (DocumentSnapshot document : data.getDocuments()) {
                    Appointment app = document.toObject(Appointment.class);
                    // duration in minutes
                    int appDuration;
                    if (atm.get(app.type) % 10 != 0) {
                        appDuration = (atm.get(app.type) % 10 > 5 ? atm.get(app.type) + (10 - atm.get(app.type) % 10) : atm.get(app.type) + (5 - atm.get(app.type) % 10));
                    } else {
                        appDuration = atm.get(app.type);
                    }
                    // Remove Zeros in Sec and mili
                    Calendar appDateNoSec = Calendar.getInstance();
                    appDateNoSec.setTime(app.date);
                    appDateNoSec.set(Calendar.MILLISECOND, 0);
                    appDateNoSec.set(Calendar.SECOND, 0);

                    app.date = appDateNoSec.getTime();
                    Long appStart = app.date.getTime();
                    Date appEndDate = app.date;
                    appEndDate.setTime(appStart + appDuration * oneMinInMilisec); // 1 min = 60k milisec

                    Long appEnd = appEndDate.getTime();

                    int index = 0;

                    // mark each impossible  meeting time in 0L - long zero
                    for (Long possibleApp : possibleAppointments) { // possibleApp - time im milisec
                        // Var duration is the choosen duration time

                        if (((possibleApp < appEnd) && (possibleApp >= appStart)) ||
                                ((possibleApp + duration * oneMinInMilisec <= appEnd) && (possibleApp + duration * oneMinInMilisec > appStart)) ||
                                ((possibleApp < appStart) && (possibleApp + duration * oneMinInMilisec > appEnd))) {
                            possibleAppointments.set(index, 0L);
                        }
                        index++;
                    }
                    // Removing all impossible meetings time
                    for (int i = index - 1; i >= 0; i--) {
                        if (possibleAppointments.get(i) == 0L) {
                            possibleAppointments.remove(i);

                        }
                    }

                }


                findViewById(R.id.client_edit_appointment_progress_bar).setVisibility(View.GONE);

                // now only possible appointment are Available

                // setting dialog custom design
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM/YYYY");
                SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm");
                final View dialogView = getLayoutInflater().inflate(R.layout.choose_appointment_time_layout, null);
                MaterialAlertDialogBuilder alertDialog = new MaterialAlertDialogBuilder(this);

                RadioGroup rg = dialogView.findViewById(R.id.radioGroup);
                if (possibleAppointments.size() > 0) {
                    MaterialRadioButton rb[] = new MaterialRadioButton[possibleAppointments.size()];
                    rg.setOrientation(RadioGroup.VERTICAL);
                    for (int i = 0; i < possibleAppointments.size(); i++) {
                        Date radioDate = new Date(possibleAppointments.get(i));
                        rb[i] = new MaterialRadioButton(this);
                        rb[i].setText("  " + sdfTime.format(radioDate));
                        rb[i].setTextSize(22);
                        rb[i].setTag(radioDate); // Save date value in tag
                        rb[i].setTextColor(getResources().getColor(R.color.dimTextOnBackground));
                        rg.addView(rb[i]);
                    }
                } else {
                    rg.setVisibility(View.GONE);
                    dialogView.findViewById(R.id.no_appointments_possible).setVisibility(View.VISIBLE);
                }

                final TextView title = dialogView.findViewById(R.id.choose_time_dialog_title);
                title.setText("Choose Time");

                rg.setOnCheckedChangeListener((group, checkedId) -> {

                    if (checkedId != -1) {
                        for (int i = 0; i < group.getChildCount(); i++) {
                            if (group.getChildAt(i).getId() == checkedId) {
                                appointment.date = (Date) group.getChildAt(i).getTag();
                                break;
                            }
                        }
                    } else {
                        appointment.date = null;
                    }

                });

                SimpleDateFormat sdfDate = new SimpleDateFormat("dd.MM.YY");
                final TextView dateTitle = dialogView.findViewById(R.id.choose_time_dialog_date);
                dateTitle.setText(sdfDate.format(calendar.getTime()));

                alertDialog.setPositiveButton("Done", (t, t2) -> {
                    if (rg.getVisibility() == View.VISIBLE) {
                        setDate = true;
                        ((TextView) findViewById(R.id.edit_appointment_time_text)).setText(sdf.format(appointment.date));
                    } else {
                        setDate = false;
                    }

                });

                alertDialog.setNegativeButton("Cancel", (t, t2) -> {
                    // update "askedForReview to TRUE
                    setDate = false;
                });


                alertDialog.setView(dialogView);
                alertDialog.show();


            });

        });


    }

    /**
     * loadBusinessData  - Load Business Data and update UI
     */
    private void loadBusinessData() {
        db.collection(BUSINESSES_COLLECTION)
                .document(appointment.business_id)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    business = documentSnapshot.toObject(Business.class);
                    if (business != null) {
                        TextView locationView = findViewById(R.id.edit_appointment_address_text);
                        locationView.setText(
                                business.location.get("address") +
                                        ", " + business.location.get("city") +
                                        ", " + business.location.get("state")
                        );
                    }
                });
    }

    /**
     * loadTypes  - Load Business Types and update UI
     */
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
                            appointment.type = l.getDocuments().get(checkedId).getId();
                            if ((firstEdit == true) && (editAppointment == false)) {
                                firstEdit = false;
                                appointment.notes = typesIdAndNotes.get(checkedId);
                                ((TextView) findViewById(R.id.edit_appointment_time_text)).setText("Choose a Date");
                                changeDate(findViewById(R.id.edit_appointment_time_text));
                            } else if (firstEdit == false) {
                                appointment.notes = typesIdAndNotes.get(checkedId);
                                ((TextView) findViewById(R.id.edit_appointment_time_text)).setText("Choose a Date");
                                changeDate(findViewById(R.id.edit_appointment_time_text));
                            } else {
                                firstEdit = false;
                            }

                            if (userType == UserType.BusinessOwner) {
                                ((com.google.android.material.textfield.TextInputEditText) (findViewById(R.id.edit_appointment_notes_text_edit_text))).setText(appointment.notes);
                            } else {
                                ((TextView) findViewById(R.id.edit_appointment_notes_text)).setText(appointment.notes);
                                firstEdit = false;
                            }
                            radioButton_id = l.getDocuments().get(checkedId).getId();
                            setChip = true;
                        } else {
                            radioButton_id = "";
                            appointment.type = "";
                            setChip = false;
                        }

                    });
                    int i = 0;
                    for (DocumentSnapshot document : l.getDocuments()) {

                        Chip[] rb = new Chip[l.getDocuments().size()];
                        rb[i] = (Chip) getLayoutInflater().inflate(R.layout.layout_chip_choice, rg, false);
                        rb[i].setText(document.getString("name"));
                        rg.addView(rb[i]);
                        // Get types notes :
                        typesIdAndNotes.put(i, (((Map<String, String>) document.get("attributes")).get("notes" +
                                "") != null ? ((Map<String, String>) document.get("attributes")).get("notes") : "No notes yet."));
                        rb[i].setId(i);
                        if (document.getId().equals(appointment.type)) {
                            rg.check(rb[i].getId());
                            radioButton_id = document.getId();
                        }
                        i++;
                    }
                    findViewById(R.id.client_edit_appointment_progress_bar).setVisibility(View.GONE);
                });
    }

    /**
     * loadAppointmentData  - Load Appointment data when in EDIT MODE
     * end update UI.
     */

    private void loadAppointmentData() {

        db.collection(APPOINTMENTS_COLLECTION)
                .document(appointment.id)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    appointment = documentSnapshot.toObject(Appointment.class);
                    if (appointment != null) {
                        old_appointment_date = appointment.date;
                        old_appointment_type = appointment.type;
                        if (appointment.business_id.equals(mAuth.getUid())) {
                            userType = UserType.BusinessOwner;

                            findViewById(R.id.noteTitle).setVisibility(View.GONE);
                            findViewById(R.id.edit_appointment_notes_text).setVisibility(View.INVISIBLE);
                            findViewById(R.id.edit_appointment_note_laylout).setVisibility(View.VISIBLE);
                            ((com.google.android.material.textfield.TextInputEditText) (findViewById(R.id.edit_appointment_notes_text_edit_text))).setText(appointment.notes);

                        } else {
                            userType = UserType.Client;
                            ((TextView) findViewById(R.id.edit_appointment_notes_text)).setText(appointment.notes);
                        }

                        appointment.id = documentSnapshot.getId();

                        TextView date = findViewById(R.id.edit_appointment_time_text);
                        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM/YYYY");
                        date.setText(sdf.format(appointment.date));
                        loadTypes(); // load appointments type
                    }


                });
    }

    public void loadNewAppointment() {
        // load business details
        loadBusinessData();
        loadTypes();
        ((TextView) findViewById(R.id.edit_appointment_time_text)).setText("Choose a time");
    }

    /**
     * changeDate  - When user wanna change date
     * First we check that he chose a type and then
     * allow him to chose a date.
     */
    public void changeDate(View view) {
        //setDate=false;
        if ((appointment.type != null) && (!appointment.type.equals(""))) {
            ClientChooseDateFragment newFragment = new ClientChooseDateFragment();
            newFragment.show(getSupportFragmentManager(), "datePicker");
        } else {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("First Choose Appointment Type.")
                    .setMessage("")
                    .setPositiveButton("Ok", null)
                    .show();
        }
    }

    /**
     * exitEditAppointment
     * Do NOT change or create any appointment.
     */

    public void exitEditAppointment(View view) {
        // exit and dont save
        finish();
    }

    /**
     * saveChanges
     * Update appointment notes/date/type if in EDIT APPOINTMENT MODE
     * Create appointment if in NEW APPOINTMENT MODE
     */

    public void saveChanges(View view) {

        findViewById(R.id.client_edit_appointment_progress_bar).setVisibility(View.VISIBLE);
        findViewById(R.id.edit_appointment_save_button).setClickable(false);
        if ((setDate) && (setChip)) {
            String doer;
            if (FirebaseAuth.getInstance().getUid().equals(appointment.business_id)) {
                doer = "business";
            } else {
                doer = "client";
            }

            //appointment.date = new Timestamp(((TextView) findViewById(R.id.edit_appointment_time_text)).getText().toString());
            if (userType == UserType.BusinessOwner) {
                appointment.notes = ((com.google.android.material.textfield.TextInputEditText) findViewById(R.id.edit_appointment_notes_text_edit_text)).getText().toString();
            } else {
                appointment.notes = ((TextView) findViewById(R.id.edit_appointment_notes_text)).getText().toString();
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
                        .addOnSuccessListener(client ->
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
                // appointment.date = calendar.getTime();
                FirebaseFirestore.getInstance()
                        .collection(APPOINTMENTS_COLLECTION)
                        .document()
                        .set(appointment).addOnCompleteListener(task -> {

                    Toast.makeText(getApplicationContext(), "Appointment Scheduled Successfully ", Toast.LENGTH_LONG).show();
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
                        .addOnSuccessListener(client ->
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


        } else {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Please Choose Date and Appointment Type.")
                    .setMessage("")
                    .setPositiveButton("Ok", null)
                    .setNegativeButton("Cancel", null)
                    .show();
            findViewById(R.id.edit_appointment_save_button).setClickable(true);
            findViewById(R.id.client_edit_appointment_progress_bar).setVisibility(View.GONE);

        }

    }

    /**
     * createNotificationChannel
     * Mange notifications
     */

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
        return !radioButton_id.equals("");
    }

    /**
     * onDateSet
     * Show pick date dialog for user to chose a day.
     */
    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        // store the values selected into a Calendar instance
        calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, monthOfYear);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        findViewById(R.id.client_edit_appointment_progress_bar).setVisibility(View.VISIBLE);
        atm = new HashMap<>();
        db.collection(BUSINESSES_COLLECTION)
                .document(business.id)
                .collection(TYPES_COLLECTION)
                .get()
                .addOnSuccessListener(qs -> {
                    for (DocumentSnapshot d : qs.getDocuments()) {
                        atm.put(d.getId(), Integer.valueOf(
                                ((Map<String, String>) d.get("attributes")).get("duration"))

                        );
                    }
                    loadAvailableAppointment(calendar);
                });

    }


    /**
     * errorDialog
     * make a ui - using dialog to show users errors
     */

    private void errorDialog(String message) {


        new MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog)

                .setTitle("Please Follow ")
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

    /**
     * abortAppointment
     * cancel an appointment
     */
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
                .setPositiveButton("Yes", (dialog, id) ->
                        FirebaseFirestore.getInstance().collection(APPOINTMENTS_COLLECTION)
                                .document(appointment.id).delete().addOnSuccessListener(result ->
                                FirebaseFirestore.getInstance()
                                        .collection(CLIENTS_COLLECTION)
                                        .document(appointment.client_id)
                                        .get()
                                        .addOnSuccessListener(ds ->
                                                FirebaseFirestore.getInstance()
                                                .collection(BUSINESSES_COLLECTION)
                                                .document(appointment.business_id)
                                                .collection(TYPES_COLLECTION)
                                                .document(appointment.type)
                                                .get()
                                                .addOnSuccessListener(ds2 -> {
                                                    Business.AppointmentAction aa = new Business.AppointmentAction(
                                                            "cancellation",
                                                            ds.getString("name"),
                                                            new Date(),
                                                            appointment.date,
                                                            appointment.date,
                                                            ds2.getString("name"),
                                                            ds2.getString("name"),
                                                            doer,
                                                            appointment.id
                                                    );
                                                    FirebaseFirestore.getInstance()
                                                            .collection(BUSINESSES_COLLECTION)
                                                            .document(appointment.business_id)
                                                            .collection(APPOINTMENT_ACTIONS_COLLECTION)
                                                            .document()
                                                            .set(aa)
                                                            .addOnSuccessListener(sl -> {
                                                                Toast.makeText(getApplicationContext(),
                                                                        "Appointment canceled  Successfully ",
                                                                        Toast.LENGTH_LONG).show();
                                                                finish();
                                                            });
                                                }))))
                .setNegativeButton("No", null)
                .show();
    }
}
