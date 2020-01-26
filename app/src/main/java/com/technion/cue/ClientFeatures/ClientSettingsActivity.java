package com.technion.cue.ClientFeatures;


import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;


import com.technion.cue.LauncherActivity;
import com.technion.cue.R;

/**
 * ClientSettingsActivity - Client Settings activity
 * Contain Logout button
 * and credit that load alert dialot with credit
 * */
public class ClientSettingsActivity extends AppCompatActivity  {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportActionBar().setTitle("");
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_setting, new ClientSettingsFragment())
                .commit();


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public static class ClientSettingsFragment extends PreferenceFragmentCompat {

        public FirebaseAuth mAuth;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.client_preference, rootKey);
            mAuth = FirebaseAuth.getInstance();

        }


        @Override
        public boolean onPreferenceTreeClick(Preference preference) {
            super.onPreferenceTreeClick(preference);

            if (preference.getKey().equals("logout") == true) {
                mAuth.signOut();
                Intent getIntentBOPage = new Intent(getActivity().getBaseContext(), LauncherActivity.class);
                startActivity(getIntentBOPage);
                getActivity().finish();
            } else {

                new MaterialAlertDialogBuilder(getContext())
                        .setTitle("Our App Credits ")
                        .setMessage("\nLOPEZ Mikhael\nIcon made by Freepik from www.flaticon.com")
                        .setPositiveButton("Ok", null)
                        .show();

            }
            return true;
        }
    }


}