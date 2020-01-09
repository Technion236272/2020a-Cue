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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.Toast;

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
import com.technion.cue.data_classes.Business;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.HashMap;
import java.util.Map;

import static com.technion.cue.FirebaseCollections.BUSINESSES_COLLECTION;
import static com.technion.cue.FirebaseCollections.TYPES_COLLECTION;
import static com.technion.cue.data_classes.Business.*;

@ModuleAuthor("Topaz")
public class BusinessSettings extends AppCompatActivity {
    private RadioGroup radioGroup;

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

//                final Intent intent = new Intent(MySettingsFragment.super.getContext(),AppoitmentTypes.class);
//                startActivity(intent);
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

//                                    db = FirebaseFirestore.getInstance();
//                                    StorageReference ref = storageRef.child("Businesses").child("attributes");


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
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);


            Button add_button = view.findViewById(R.id.button_add_type);
            RecyclerView types = view.findViewById(R.id.types_list);
            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
            types.setLayoutManager(layoutManager);
            types.setHasFixedSize(true);

            Query query = FirebaseFirestore.getInstance()
                    .collection(BUSINESSES_COLLECTION)
                    .document(FirebaseAuth.getInstance().getUid())
                    .collection(TYPES_COLLECTION)
                    .orderBy("name");

            FirestoreRecyclerOptions<AppointmentType> options =
                    new FirestoreRecyclerOptions.Builder<AppointmentType>()
                            .setQuery(query, AppointmentType.class)
                            .build();

            AppointmentTypesListAdapter tAdapter = new AppointmentTypesListAdapter(options);
            types.setAdapter(tAdapter);

            add_button.setOnClickListener(v->{
                Fragment new_types = new newTypesFragment();
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.types_fragment, new_types)
                        .addToBackStack(null)
                        .commit();
                    });
        }
    }

    public static class newTypesFragment extends Fragment{
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
            Button done = view.findViewById(R.id.button_done);
            done.setOnClickListener(l->{
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


                Map<String, String> attributes = new HashMap<>();
                attributes.put("active", String.valueOf(isActive));
                attributes.put("duration", duration.getText().toString());
                attributes.put("sunday",String.valueOf(isChecked1));
                attributes.put("monday",String.valueOf(isChecked2));
                attributes.put("tuesday",String.valueOf(isChecked3));
                attributes.put("wednesday",String.valueOf(isChecked4));
                attributes.put("thursday",String.valueOf(isChecked5));
                attributes.put("friday",String.valueOf(isChecked6));
                attributes.put("saturday",String.valueOf(isChecked7));
                attributes.put("notes",notes.getText().toString());
                AppointmentType type = new AppointmentType(type_name.getText().toString(),attributes);

                FirebaseFirestore.getInstance()
                        .collection(BUSINESSES_COLLECTION+"/"+FirebaseAuth.getInstance().getUid()
                                +"/Types").document().set(type);
            });

        }
    }

    private boolean isInputValid(){

        return true;
    }

    private boolean isInputNotEmpty(String ... fields){
        for (String f : fields) {
            if (f.isEmpty())
                return false;
        }

        return true;
    }
}


