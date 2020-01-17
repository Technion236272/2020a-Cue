package com.technion.cue.BusinessFeatures;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.technion.cue.Credits;
import com.technion.cue.R;
import com.technion.cue.SignInActivity;
import com.technion.cue.annotations.ModuleAuthor;
import com.technion.cue.data_classes.Business;

import java.util.HashMap;
import java.util.Map;

import static android.text.TextUtils.isDigitsOnly;
import static com.technion.cue.FirebaseCollections.BUSINESSES_COLLECTION;
import static com.technion.cue.FirebaseCollections.TYPES_COLLECTION;
import static com.technion.cue.data_classes.Business.*;

@ModuleAuthor("Topaz")
public class BusinessSettings extends AppCompatActivity {
    private FirebaseUser currentUser;
    RecyclerView types;
    AlertDialog.Builder builder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_setting, new MySettingsFragment())
                .commit();


    }
    /*
    The following class creates the main Settings Fragment -
    the one we see when we press settings on the menu.
     */


    public static class MySettingsFragment extends PreferenceFragmentCompat {
        public FirebaseFirestore db;
        public FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preference, rootKey);

        }
        /*
        the following class if for the two dialogs in the settings -
        1. time frame dialog - where the business owner choose how much time before a meeting
        the client can't make any changes at the meeting.
        2. remind time dialog - where the business owner can choose how much time before a meeting
        the client will recieve a reminder about to meeting.

        this class is for dealing with the save button click event on the dialog -
        check if everything is legal and then save the changes in the firebase.
         */
        class CustomListener implements View.OnClickListener {
            private final Dialog dialog;
            private final String title;

            public CustomListener(Dialog dialog, String title) {
                this.dialog = dialog;
                this.title = title;
            }

            @Override
            public void onClick(View v) {
                String business_id = FirebaseAuth.getInstance().getUid();
                Map<String, Object> map = new HashMap<>();
                // put your code here
                // take the value from selected radio button
                RadioGroup radioGroup = dialog.findViewById(R.id.radio_group);
                int selectedId = radioGroup.getCheckedRadioButtonId();

                // find the radiobutton by returned id
                RadioButton radioButton = dialog.findViewById(selectedId);

                // get edittext value
                EditText edit_text = dialog.findViewById(R.id.num_input);
                String edit_text_value = edit_text.getText().toString();

                if (edit_text_value.isEmpty() || radioButton == null) {
                    Toast.makeText(MySettingsFragment.super.getContext(),
                            "Please enter a number and choose", Toast.LENGTH_LONG).show();
                } else {
                    FirebaseFirestore.getInstance().collection(BUSINESSES_COLLECTION)
                            .document(business_id)
                            .get()
                            .addOnSuccessListener(rt -> {
                                Business business = rt.toObject(Business.class);
                                if (title.equals("remind time")) {
                                    boolean is_contain = business.attributes.containsKey("remind time");
                                    if (is_contain) {
                                        business.attributes.remove("remind time");
                                        business.attributes.remove("remind time unit");
                                    }
                                    int remind_time_num = Integer.valueOf(edit_text_value);
                                    int choosen_rbutton_id = radioButton.getId();
                                    if (choosen_rbutton_id == R.id.minutes_button) {
                                        business.attributes.put("remind time", edit_text_value);
                                        business.attributes.put("remind time unit", "minutes");
                                    } else if (choosen_rbutton_id == R.id.hours_button) {
                                        int convert_hours_to_minutes = remind_time_num * 60;
                                        business.attributes.put("remind time", Integer.toString(convert_hours_to_minutes));
                                        business.attributes.put("remind time unit", "hours");
                                    } else if (choosen_rbutton_id == R.id.days_button) {
                                        int convert_days_to_minutes = remind_time_num * 24 * 60;
                                        business.attributes.put("remind time", Integer.toString(convert_days_to_minutes));
                                        business.attributes.put("remind time unit", "days");
                                    } else if (choosen_rbutton_id == R.id.weeks_button) {
                                        int convert_weeks_to_minutes = remind_time_num * 7 * 24 * 60;
                                        business.attributes.put("remind time", Integer.toString(convert_weeks_to_minutes));
                                        business.attributes.put("remind time unit", "weeks");
                                    }
                                } else if (title.equals("time frame")) {
                                    boolean is_contain = business.attributes.containsKey("time frame");
                                    if (is_contain) {
                                        business.attributes.remove("time frame");
                                        business.attributes.remove("time frame unit");
                                    }
                                    int remind_time_num = Integer.valueOf(edit_text_value);
                                    int choosen_rbutton_id = radioButton.getId();
                                    if (choosen_rbutton_id == R.id.minutes_button) {
                                        business.attributes.put("time frame", edit_text_value);
                                        business.attributes.put("time frame unit", "minutes");
                                    } else if (choosen_rbutton_id == R.id.hours_button) {
                                        int convert_hours_to_minutes = remind_time_num * 60;
                                        business.attributes.put("time frame", Integer.toString(convert_hours_to_minutes));
                                        business.attributes.put("time frame unit", "hours");
                                    } else if (choosen_rbutton_id == R.id.days_button) {
                                        int convert_days_to_minutes = remind_time_num * 24 * 60;
                                        business.attributes.put("time frame", Integer.toString(convert_days_to_minutes));
                                        business.attributes.put("time frame unit", "days");
                                    } else if (choosen_rbutton_id == R.id.weeks_button) {
                                        int convert_weeks_to_minutes = remind_time_num * 7 * 24 * 60;
                                        business.attributes.put("time frame", Integer.toString(convert_weeks_to_minutes));
                                        business.attributes.put("time frame unit", "weeks");
                                    }
                                }
                                map.put("attributes", business.attributes);
                                FirebaseFirestore.getInstance().collection(BUSINESSES_COLLECTION)
                                        .document(business_id)
                                        .update(map);
                                dialog.dismiss();
                            });
                }
            }
        }

        /*
        this function recognize a click on some preference on the prefernce menu,
        and define what to do with it.
         */
        @Override
        public boolean onPreferenceTreeClick(Preference p) {

            if (p.getKey().equals("appointment type")) {
                Fragment types = new typesFragment();
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_setting, types)
                        .addToBackStack(null)
                        .commit();

            }

            /*
            here if we choose the time frame setting,
            it searches in the firebase for the previous settings and upload it to the edit text,
            and to the correct radio button.
             */
            if (p.getKey().equals("time frame")) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                builder.setCancelable(false)
                        .setMessage("Please enter a number and choose")
                        .setView(R.layout.set_time)
                        .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) { }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();

                //upload the previous settings
                FirebaseFirestore.getInstance().collection(BUSINESSES_COLLECTION)
                        .document(FirebaseAuth.getInstance().getUid())
                        .get()
                        .addOnSuccessListener(rt->{
                            Business business = rt.toObject(Business.class);
                            if (business.attributes.containsKey("time frame")){
                                EditText number =alertDialog.findViewById(R.id.num_input);
                                String time_unit = business.attributes.get("time frame unit");
                                String time_in_minutes = business.attributes.get("time frame");
                                if(time_unit.equals("minutes")){
                                    number.setText(time_in_minutes);
                                    RadioButton radioButton = (RadioButton)alertDialog.findViewById(R.id.minutes_button);
                                    radioButton.setChecked(true);
                                } else if(time_unit.equals("hours")){
                                    int time_in_hours = Integer.valueOf(time_in_minutes)/60;
                                    number.setText(String.valueOf(time_in_hours));
                                    RadioButton radioButton = (RadioButton)alertDialog.findViewById(R.id.hours_button);
                                    radioButton.setChecked(true);
                                } else if(time_unit.equals("days")){
                                    int time_in_days = Integer.valueOf(time_in_minutes)/(60 * 24 );
                                    number.setText(String.valueOf(time_in_days));
                                    RadioButton radioButton = (RadioButton)alertDialog.findViewById(R.id.days_button);
                                    radioButton.setChecked(true);
                                } else if(time_unit.equals("weeks")){
                                    int time_in_days = Integer.valueOf(time_in_minutes)/(60 * 24 * 7 );
                                    number.setText(String.valueOf(time_in_days));
                                    RadioButton radioButton = (RadioButton)alertDialog.findViewById(R.id.weeks_button);
                                    radioButton.setChecked(true);
                                }
                            }
                        });
                Button save_button = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                Button cancel_button = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                save_button.setOnClickListener(new CustomListener(alertDialog,"time frame"));
                cancel_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                    }
                });
            }

            /*here if we choose the time frame setting,
            it searches in the firebase for the previous settings and upload it to the edit text,
            and to the correct radio button.
             */
            if (p.getKey().equals("remind time")) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                builder.setCancelable(false)
                        .setMessage("Please enter a number and choose")
                        .setView(R.layout.set_time)
                        .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) { }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();

                //upload the previous settings
                FirebaseFirestore.getInstance().collection(BUSINESSES_COLLECTION)
                        .document(FirebaseAuth.getInstance().getUid())
                        .get()
                        .addOnSuccessListener(rt->{
                            Business business = rt.toObject(Business.class);
                            if (business.attributes.containsKey("remind time")){
                                EditText number =alertDialog.findViewById(R.id.num_input);
                                String time_unit = business.attributes.get("remind time unit");
                                String time_in_minutes = business.attributes.get("remind time");
                                if(time_unit.equals("minutes")){
                                    number.setText(time_in_minutes);
                                    RadioButton radioButton = (RadioButton)alertDialog.findViewById(R.id.minutes_button);
                                    radioButton.setChecked(true);
                                } else if(time_unit.equals("hours")){
                                    int time_in_hours = Integer.valueOf(time_in_minutes)/60;
                                    number.setText(String.valueOf(time_in_hours));
                                    RadioButton radioButton = (RadioButton)alertDialog.findViewById(R.id.hours_button);
                                    radioButton.setChecked(true);
                                } else if(time_unit.equals("days")){
                                    int time_in_days = Integer.valueOf(time_in_minutes)/(60 * 24 );
                                    number.setText(String.valueOf(time_in_days));
                                    RadioButton radioButton = (RadioButton)alertDialog.findViewById(R.id.days_button);
                                    radioButton.setChecked(true);
                                } else if(time_unit.equals("weeks")){
                                    int time_in_days = Integer.valueOf(time_in_minutes)/(60 * 24 * 7 );
                                    number.setText(String.valueOf(time_in_days));
                                    RadioButton radioButton = (RadioButton)alertDialog.findViewById(R.id.weeks_button);
                                    radioButton.setChecked(true);
                                }
                            }
                        });
                Button save_button = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                Button cancel_button = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                save_button.setOnClickListener(new CustomListener(alertDialog,"remind time"));
            }

            if (p.getKey().equals("credits")) {

                final Intent intent = new Intent(MySettingsFragment.super.getContext(), Credits.class);
                startActivity(intent);


            }

            if (p.getKey().equals("logout")) {
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(getContext(), SignInActivity.class));
                    getActivity().finish();
                    return true;
            }

            return true;
        }
    }

    /*
    this class presents all the type appointments there is.
    they all in a list and when you press one of them -
    you go to newTypeFragment - another class fragment,
    and upload all the information about the type in the edit texts.
     */
    public static class typesFragment extends Fragment {
        AppointmentTypesListAdapter tAdapter;
        public FirebaseFirestore db;
        public FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.types_fragment, container, false);
        }

        @Override
        public void onStart() {
            super.onStart();
            tAdapter.startListening();
        }

        @Override
        public void onStop() {
            super.onStop();
            tAdapter.stopListening();
        }

        @Override
        public void onResume() {
            super.onResume();
            tAdapter.startListening();
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            Button add_button = view.findViewById(R.id.button_add_type);
            RecyclerView types = view.findViewById(R.id.types_list);
            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
            types.setLayoutManager(layoutManager);
            types.setHasFixedSize(true);

            Query query = FirebaseFirestore.getInstance()
                    .collection(BUSINESSES_COLLECTION + "/" + FirebaseAuth.getInstance().getUid()
                            + "/Types").orderBy("name");

            FirestoreRecyclerOptions<AppointmentType> options =
                    new FirestoreRecyclerOptions.Builder<AppointmentType>()
                            .setQuery(query, AppointmentType.class)
                            .build();

            tAdapter = new AppointmentTypesListAdapter(options, new AppointmentTypesListAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    View v = types.getLayoutManager().findViewByPosition(position);
                    TextView name = v.findViewById(R.id.businessType);
                    Fragment new_types = new newTypesFragment();
                    Bundle args = new Bundle();
                    args.putString("name", name.getText().toString());
                    new_types.setArguments(args);
                    FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.fragment_setting, new_types)
                            .addToBackStack(null)
                            .commit();
                }
            });
            types.setAdapter(tAdapter);


            add_button.setOnClickListener(v -> {
                Fragment new_types = new newTypesFragment();
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_setting, new_types)
                        .addToBackStack(null)
                        .commit();
            });
        }
    }
    /*
    this class is for creating new type or edit an existing one.
     */

    public static class newTypesFragment extends Fragment {
        public FirebaseFirestore db;
        public FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);


        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.new_appointment_type, container, false);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            String oname;
            if (getArguments() == null)
                oname = null;
            else
                oname = getArguments().getString("name");
            final String tname = oname;
            if (tname != null) { //if the type exists - we want to fill in the field.
                FirebaseFirestore.getInstance()
                        .collection(BUSINESSES_COLLECTION + "/" + FirebaseAuth.getInstance().getUid()
                                + "/Types").whereEqualTo("name", tname).limit(1).get().addOnSuccessListener(documentSnapshot -> {
                    final AppointmentType appointmentType = documentSnapshot.getDocuments().get(0).toObject(AppointmentType.class);

                    Switch isActive = (Switch) view.findViewById(R.id.active_type);
                    TextInputEditText type_name1 = view.findViewById(R.id.bTypeNameEdit);
                    TextInputEditText duration1 = view.findViewById(R.id.bTypeDurationEdit);
                    CheckBox check1 = (CheckBox) view.findViewById(R.id.type_sunday);
                    CheckBox check2 = (CheckBox) view.findViewById(R.id.type_monday);
                    CheckBox check3 = (CheckBox) view.findViewById(R.id.type_tuesday);
                    CheckBox check4 = (CheckBox) view.findViewById(R.id.type_wednesday);
                    CheckBox check5 = (CheckBox) view.findViewById(R.id.type_thursday);
                    CheckBox check6 = (CheckBox) view.findViewById(R.id.type_friday);
                    CheckBox check7 = (CheckBox) view.findViewById(R.id.type_saturday);
                    TextInputEditText notes = view.findViewById(R.id.bTypeNotesEdit);

                    //assign all check to false to save else statement later
                    isActive.setChecked(false);
                    check1.setChecked(false);
                    check2.setChecked(false);
                    check3.setChecked(false);
                    check4.setChecked(false);
                    check5.setChecked(false);
                    check6.setChecked(false);
                    check7.setChecked(false);

                    if (appointmentType.attributes.get("active").equals("true")) {
                        isActive.setChecked(true);
                    }
                    type_name1.setText(appointmentType.name);
                    duration1.setText(appointmentType.attributes.get("duration"));
                    notes.setText(appointmentType.attributes.get("notes"));
                    if (appointmentType.attributes.get("sunday").equals("true")) {
                        check1.setChecked(true);
                    }
                    if (appointmentType.attributes.get("monday").equals("true")) {
                        check2.setChecked(true);
                    }
                    if (appointmentType.attributes.get("tuesday").equals("true")) {
                        check3.setChecked(true);
                    }
                    if (appointmentType.attributes.get("wednesday").equals("true")) {
                        check4.setChecked(true);
                    }
                    if (appointmentType.attributes.get("thursday").equals("true")) {
                        check5.setChecked(true);
                    }
                    if (appointmentType.attributes.get("friday").equals("true")) {
                        check6.setChecked(true);
                    }
                    if (appointmentType.attributes.get("saturday").equals("true")) {
                        check7.setChecked(true);
                    }
                });
            } else { //default - new type in active
                Switch type_active = (Switch) view.findViewById(R.id.active_type);
                type_active.setChecked(true);
            }
            Button done = view.findViewById(R.id.button_done);
            done.setOnClickListener(l -> {
                boolean isActive = ((Switch) view.findViewById(R.id.active_type)).isChecked();
                TextInputEditText type_name = view.findViewById(R.id.bTypeNameEdit);
                TextInputEditText duration = view.findViewById(R.id.bTypeDurationEdit);
                boolean isChecked1 = ((CheckBox) view.findViewById(R.id.type_sunday)).isChecked();
                boolean isChecked2 = ((CheckBox) view.findViewById(R.id.type_monday)).isChecked();
                boolean isChecked3 = ((CheckBox) view.findViewById(R.id.type_tuesday)).isChecked();
                boolean isChecked4 = ((CheckBox) view.findViewById(R.id.type_wednesday)).isChecked();
                boolean isChecked5 = ((CheckBox) view.findViewById(R.id.type_thursday)).isChecked();
                boolean isChecked6 = ((CheckBox) view.findViewById(R.id.type_friday)).isChecked();
                boolean isChecked7 = ((CheckBox) view.findViewById(R.id.type_saturday)).isChecked();
                TextInputEditText notes = view.findViewById(R.id.bTypeNotesEdit);

                //check if input edit type in legal
                if (type_name.getText().toString().isEmpty()) {
                    Toast.makeText(this.getContext(),
                            "Please enter type name", Toast.LENGTH_LONG).show();
                } else if ((!isDigitsOnly(duration.getText().toString())) ||
                        duration.getText().toString().isEmpty()) {
                    Toast.makeText(this.getContext(),
                            "Duration needs to be a number, please choose a number of minutes",
                            Toast.LENGTH_LONG).show();
                } else if (!isChecked1 && !isChecked2 && !isChecked3 && !isChecked4 && !isChecked5
                        && !isChecked6 && !isChecked7) {
                    Toast.makeText(this.getContext(),
                            "Please choose at least one day where type will be available"
                            , Toast.LENGTH_LONG).show();
                } else if ((tname != null && !tname.equals(type_name.getText().toString())) ||
                        tname == null) { //need to check if type name already exist
                    FirebaseFirestore.getInstance()
                            .collection(BUSINESSES_COLLECTION + "/" + FirebaseAuth.getInstance().getUid()
                                    + "/Types").whereEqualTo("name", type_name.getText().toString())
                            .get().addOnSuccessListener(f -> {
                        if (f.isEmpty()) {
                            Map<String, String> attributes = new HashMap<>();
                            attributes.put("active", String.valueOf(isActive));
                            attributes.put("duration", duration.getText().toString());
                            attributes.put("sunday", String.valueOf(isChecked1));
                            attributes.put("monday", String.valueOf(isChecked2));
                            attributes.put("tuesday", String.valueOf(isChecked3));
                            attributes.put("wednesday", String.valueOf(isChecked4));
                            attributes.put("thursday", String.valueOf(isChecked5));
                            attributes.put("friday", String.valueOf(isChecked6));
                            attributes.put("saturday", String.valueOf(isChecked7));
                            if(notes.getText().toString().isEmpty()){
                                notes.setText("No special notes");
                            }
                            attributes.put("notes", notes.getText().toString());

                            AppointmentType type = new AppointmentType(type_name.getText().toString(), attributes);
                            Map<String, Object> map = new HashMap<>();
                            map.put("attributes", attributes);
                            map.put("name", type_name.getText().toString());


                            if (tname != null) {
                                FirebaseFirestore.getInstance()
                                        .collection(BUSINESSES_COLLECTION + "/" + FirebaseAuth.getInstance().getUid()
                                                + "/Types").whereEqualTo("name", tname).get().addOnSuccessListener(p -> {
                                    String document_id = p.getDocuments().get(0).getId();
                                    FirebaseFirestore.getInstance()
                                            .collection(BUSINESSES_COLLECTION + "/" + FirebaseAuth.getInstance().getUid()
                                                    + "/Types").document(document_id).update(map);
                                });
                                FirebaseFirestore.getInstance()
                                        .collection(BUSINESSES_COLLECTION + "/" + FirebaseAuth.getInstance().getUid()
                                                + "/Types").document().update(map);

                            } else {
                                FirebaseFirestore.getInstance()
                                        .collection(BUSINESSES_COLLECTION + "/" + FirebaseAuth.getInstance().getUid()
                                                + "/Types").document().set(type);
                            }
                            getFragmentManager().popBackStackImmediate();

                        } else {
                            Toast.makeText(this.getContext(),
                                    "Type name is taken. please choose different name", Toast.LENGTH_LONG).show();
                        }
                    });


                }
            });
        }
    }
}



