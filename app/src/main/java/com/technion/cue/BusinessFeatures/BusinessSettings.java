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

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.text.TextUtils;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.technion.cue.Credits;
import com.technion.cue.R;
import com.technion.cue.annotations.ModuleAuthor;
import com.technion.cue.data_classes.Appointment;
import com.technion.cue.data_classes.Business;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

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
    public FirebaseFirestore db;
    public FirebaseStorage storage;
    StorageReference storageRef;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_setting, new MySettingsFragment())
                .commit();


    }


    public static class MySettingsFragment extends PreferenceFragmentCompat {
        public FirebaseFirestore db;
        public FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preference, rootKey);

        }

        @Override
        public boolean onPreferenceTreeClick(Preference p) {

            if (p.getKey().equals("appointment type")) {
                Fragment types = new typesFragment();
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_setting, types)
                        .addToBackStack(null)
                        .commit();

            }

            if (p.getKey().equals("time frame")) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                LayoutInflater inflater = requireActivity().getLayoutInflater();

                View dialogView = inflater.inflate(R.layout.set_time, null);

                builder.setView(dialogView).
                        setPositiveButton(R.string.save_settings, (dialog, which) -> {

                            // take the value from selected radio button
                            RadioGroup radioGroup = (RadioGroup) dialogView.findViewById(R.id.radio_group);
                            int selectedId = radioGroup.getCheckedRadioButtonId();

                            // find the radiobutton by returned id
                            RadioButton radioButton = (RadioButton) dialogView.findViewById(selectedId);

                            // get edittext value
                            EditText edit_text = (EditText) dialogView.findViewById(R.id.num_input);
                            String edit_text_value = edit_text.getText().toString();

                            if (edit_text_value.isEmpty() || radioButton == null) {
                                Toast t = Toast.makeText(MySettingsFragment.super.getContext(), "Please enter a number and choose", Toast.LENGTH_LONG);
                                t.show();
                                //Todo: how to block people from not choose and press save?
                            } else {
                                //Todo: put the values inside the settings map.
//                                    db = FirebaseFirestore.getInstance();
//                                    StorageReference ref = storageRef.child("Businesses").child("attributes");


                            }

                            //Todo: old settings (number and units) need to be performed
                        })
                        .setNegativeButton(R.string.cancel_settings, (dialog, which) -> dialog.cancel());
                AlertDialog dialog = builder.create();
                dialog.show();
            }

            if (p.getKey().equals("remind time")) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                LayoutInflater inflater = requireActivity().getLayoutInflater();

                View dialogView = inflater.inflate(R.layout.set_time, null);

                builder.setView(dialogView).
                        setPositiveButton(R.string.save_settings, (dialog, which) -> {


                            // take the value from selected radio button
                            RadioGroup radioGroup = dialogView.findViewById(R.id.radio_group);
                            int selectedId = radioGroup.getCheckedRadioButtonId();

                            // find the radiobutton by returned id
                            RadioButton radioButton = dialogView.findViewById(selectedId);

                            // get edittext value
                            EditText edit_text = dialogView.findViewById(R.id.num_input);
                            String edit_text_value = edit_text.getText().toString();

                            if (edit_text_value.isEmpty() || radioButton == null) {

                                Toast t = Toast.makeText(MySettingsFragment.super.getContext(),
                                        "Please enter a number and choose", Toast.LENGTH_LONG);
                                t.show();
                                //Todo: how to block people from not choose and press save?
                            } else {
                                //Todo: put the values inside the settings map.

//


                            }

                            //Todo: old settings (number and units) need to be performed
                        })
                        .setNegativeButton(R.string.cancel_settings, (dialog, which) -> dialog.cancel());
                AlertDialog dialog = builder.create();
                dialog.show();
            }

            if (p.getKey().equals("credits")) {

                final Intent intent = new Intent(MySettingsFragment.super.getContext(), Credits.class);
                startActivity(intent);


            }
            return true;
        }
    }

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
                    .collection(BUSINESSES_COLLECTION+"/"+FirebaseAuth.getInstance().getUid()
                            +"/Types").orderBy("name");

            FirestoreRecyclerOptions<AppointmentType> options =
                    new FirestoreRecyclerOptions.Builder<AppointmentType>()
                            .setQuery(query, AppointmentType.class)
                            .build();

            tAdapter = new AppointmentTypesListAdapter(options, new AppointmentTypesListAdapter.OnItemClickListener(){
                @Override
                public void onItemClick (View view, int position){
                    View v = types.getLayoutManager().findViewByPosition(position);
                    TextView name = v.findViewById(R.id.businessType);
                    Fragment new_types = new newTypesFragment();
                    Bundle args = new Bundle();
                    args.putString("name",name.getText().toString());
                    new_types.setArguments(args);
                    FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.fragment_setting, new_types)
                            .addToBackStack(null)
                            .commit();


                }
            });
            types.setAdapter(tAdapter);


            add_button.setOnClickListener(v->{
                Fragment new_types = new newTypesFragment();
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_setting, new_types)
                        .addToBackStack(null)
                        .commit();
                    });
        }
    }

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
            String tname = getArguments().getString("name");
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
                    boolean name_avail = FirebaseFirestore.getInstance()
                            .collection(BUSINESSES_COLLECTION + "/" + FirebaseAuth.getInstance().getUid()
                                    + "/Types").whereEqualTo("name", type_name.getText().toString())
                            .get().getResult().isEmpty();
                    if (!name_avail) {
                        Toast.makeText(this.getContext(),
                                "Type name is taken. please choose different name", Toast.LENGTH_LONG).show();
                    }
                } else {
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
                    getFragmentManager().beginTransaction().remove(newTypesFragment.this).commit();

                }
            });
        }
    }
}


