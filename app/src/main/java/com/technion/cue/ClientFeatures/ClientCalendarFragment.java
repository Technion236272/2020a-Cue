package com.technion.cue.ClientFeatures;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;


import com.google.firebase.auth.FirebaseAuth;
import com.technion.cue.R;

import java.util.Date;

/**
 * ClientCalendarFragment - Fragemnt which show
 * calendar and relacent appointments
 *
 * */
public class ClientCalendarFragment extends Fragment {


    FirebaseAuth mAuth;
    CalendarView c;


    public ClientCalendarFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        // Set the date to now


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        super.onCreate(savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_client_calendar, container, false);
        return v;

    }

    /**
     * onViewCreated - showing calendar view
     * by loading into container
     *
     * */

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        c = view.findViewById(R.id.calendar_page_date_picker);
        c.setMinDate((new Date().getTime()));
        c.setVisibility(View.VISIBLE);
        c.setOnDateChangeListener((c_view, year, month, day) -> {

            c.animate()
                    .translationY(-c.getHeight())
                    .setDuration(300)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                           c.setVisibility(View.GONE);

                            Bundle b = new Bundle();
                            b.putInt("year", year);
                            b.putInt("month", month);
                            b.putInt("day", day);
                            Fragment bsd = new ClientAppointmentsPerDayFragment();
                            bsd.setArguments(b);

                            getChildFragmentManager()
                                    .beginTransaction()
                                    .setCustomAnimations(R.anim.animation_slidein_replace_fragment, 0)
                                    .replace(R.id.client_calendar_fragment_container, bsd)
                                    .addToBackStack(null)
                                    .commit();
                        }
                    });



        });
    }



    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public void onStart() {
        super.onStart();

    }
}
