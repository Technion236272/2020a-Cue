package com.technion.cue.ClientFeatures;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.technion.cue.BusinessFeatures.BusinessInfoFragment;
import com.technion.cue.R;
import com.technion.cue.annotations.ModuleAuthor;

/*
        Still need to set all up
 */

@ModuleAuthor("Ben")
public class ClientBusinessHomepage extends AppCompatActivity {
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_business_homepage);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Favorite");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        // getting the b_id from client home page

        findViewById(R.id.switch_to_date_time_fragments).setVisibility(View.VISIBLE);
        Bundle b = getIntent().getExtras();
        Fragment f = new BusinessInfoFragment();
        f.setArguments(b);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_holder_business_client, f)
                .commit();


        findViewById(R.id.switch_to_date_time_fragments).setOnClickListener(l -> {
            Intent intent = new Intent(this, EditAppointmentActivity.class);
            intent.putExtras(b);
            startActivity(intent);
            findViewById(R.id.switch_to_date_time_fragments).setVisibility(View.VISIBLE);
        });



    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }



}
