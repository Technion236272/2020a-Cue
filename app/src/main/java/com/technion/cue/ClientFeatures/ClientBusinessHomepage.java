package com.technion.cue.ClientFeatures;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
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
        // getting the b_id from client home page

        findViewById(R.id.switch_to_date_time_fragments).setVisibility(View.VISIBLE);
        Bundle b = getIntent().getExtras();
        Fragment f = new BusinessInfoFragment();
        f.setArguments(b);
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_holder_business_client, f)
                .commit();

        Fragment cdf = new ClientChooseDateFragment();
        cdf.setArguments(b);
        findViewById(R.id.switch_to_date_time_fragments).setOnClickListener(l -> {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_holder_business_client, cdf)
                    .addToBackStack(null)
                    .commit();
            findViewById(R.id.switch_to_date_time_fragments).setVisibility(View.VISIBLE);
        });
    }


}
