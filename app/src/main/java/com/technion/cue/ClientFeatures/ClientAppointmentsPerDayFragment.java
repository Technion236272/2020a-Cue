
package com.technion.cue.ClientFeatures;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import android.content.Intent;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import com.google.firebase.firestore.Query;

import com.google.firebase.auth.FirebaseUser;
import com.technion.cue.R;
import com.technion.cue.data_classes.Appointment;

import static com.technion.cue.FirebaseCollections.APPOINTMENTS_COLLECTION;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * ClientAppointmentsPerDayFragment - on client homepage
 * present daily appointmet (per day)
 * */
public class ClientAppointmentsPerDayFragment extends Fragment {

    private MyAppointmentListAdapter appointmentAdapter;


    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    Bundle bundle;
    TextView dateText;
    TextView dateMonth;
    ImageButton backButton;

    public ClientAppointmentsPerDayFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        bundle = getArguments();


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_client_appointmets_per_day, container, false);

        if (getArguments() != null && getArguments().containsKey("useBack") && !getArguments().getBoolean("useBack")) {
            view.findViewById(R.id.client_calendar_fragment_appointment_top_back).setVisibility(View.GONE);
        }

        dateText = view.findViewById(R.id.client_calendar_fragment_appointment_top_date);
        dateMonth = view.findViewById(R.id.client_calendar_fragment_appointment_top_date_month);

        // use calendar to get month bo_name
        Calendar cal =Calendar.getInstance();
        cal.set(bundle.getInt("year"),bundle.getInt("month"),bundle.getInt("day"));
        String monthName = (new SimpleDateFormat("MMM").format(cal.getTime()));
        // --
        dateText.setText(bundle.getInt("day")+"");
        dateMonth.setText(monthName);

        backButton = view.findViewById(R.id.client_calendar_fragment_appointment_top_back);
        backButton.setOnClickListener(l->
                view.animate()
                .translationY(view.getHeight())
                .setDuration(300)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        view.setVisibility(View.GONE);

                        Bundle b = new Bundle();

                        Fragment bsd = new ClientCalendarFragment();
                        bsd.setArguments(b);

                       getParentFragment().getChildFragmentManager()
                               .beginTransaction()
                               .setCustomAnimations(R.anim.animation_slideout_replace_fragment, 0)
                               .replace(R.id.client_calendar_fragment_container, bsd)
                               .commit();

                    }
                }));


        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpRecycleAppointmentAView();
    }

    /**
     * setUpRecycleAppointmentAView - set recycleView for
     * appointments in that day
     * */
    private void setUpRecycleAppointmentAView() {

        FirebaseUser currentUser = mAuth.getCurrentUser();
        Calendar c = Calendar.getInstance();

        int month = (bundle.getInt("month"));
        c.set(bundle.getInt("year"),month , bundle.getInt("day"), 0, 0);

        Calendar end = Calendar.getInstance();
        end.setTime(c.getTime());

        end.add(Calendar.DATE,1);

        Timestamp time = new Timestamp(c.getTime());
        Timestamp endTime = new Timestamp(end.getTime());
        Query query = db.collection(APPOINTMENTS_COLLECTION)
                .whereEqualTo("client_id", currentUser.getUid())
                .whereGreaterThanOrEqualTo("date", time)
                .whereLessThanOrEqualTo("date",endTime)
                .orderBy("date", Query.Direction.ASCENDING);
        FirestoreRecyclerOptions<Appointment> options =
                new FirestoreRecyclerOptions.Builder<Appointment>()
                        .setQuery(query, Appointment.class)
                        .build();
        appointmentAdapter = new MyAppointmentListAdapter(options);


        RecyclerView recyclerView = getActivity().findViewById(R.id.client_calendar_fragment_appointment_list);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        recyclerView.setAdapter(appointmentAdapter);

    }



    @Override
    public void onStart() {
        super.onStart();
        appointmentAdapter.startListening();
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpRecycleAppointmentAView();
        appointmentAdapter.startListening();

    }


    @Override
    public void onStop() {
        super.onStop();
        RecyclerView recyclerView = getActivity().findViewById(R.id.client_calendar_fragment_appointment_list);
        recyclerView.getRecycledViewPool().clear();
        appointmentAdapter.stopListening();

    }





}
