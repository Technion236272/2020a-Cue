package com.technion.cue;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Set;

public class Settings extends AppCompatActivity {
    private RadioGroup radioGroup;

    private FirebaseUser currentUser;


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
            final EditText input = new EditText(this.getContext());
            if (p.getKey().equals("appointment type")) {

                //for sprint2

                return true;
            }

            if (p.getKey().equals("time frame")) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                LayoutInflater inflater = requireActivity().getLayoutInflater();

                View dialogView = inflater.inflate(R.layout.set_time, null);

                builder.setView(dialogView).
                        setPositiveButton(R.string.save_settings, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {


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
                                    //Todo: how to block people from not choose and press save?9
                                } else {
                                    //Todo: put the values inside the settings map.

//                                    db = FirebaseFirestore.getInstance();
//                                    StorageReference ref = storageRef.child("Businesses").child("attributes");


                                }

                                //Todo: old settings (number and units) need to be performed
                            }
                        })
                        .setNegativeButton(R.string.cancel_settings, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
            }

            if (p.getKey().equals("remind time")) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                LayoutInflater inflater = requireActivity().getLayoutInflater();

                View dialogView = inflater.inflate(R.layout.set_time, null);

                builder.setView(dialogView).
                        setPositiveButton(R.string.save_settings, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {


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
                                    //Todo: how to block people from not choose and press save?9
                                } else {
                                    //Todo: put the values inside the settings map.

//                                    db = FirebaseFirestore.getInstance();
//                                    StorageReference ref = storageRef.child("Businesses").child("attributes");


                                }

                                //Todo: old settings (number and units) need to be performed
                            }
                        })
                        .setNegativeButton(R.string.cancel_settings, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
            return true;
        }
    }
}
