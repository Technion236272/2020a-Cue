package com.technion.cue.BusinessFeatures;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.technion.cue.R;
import com.technion.cue.data_classes.Appointment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import static com.technion.cue.FirebaseCollections.APPOINTMENTS_COLLECTION;

/**
 * this activity will display a list of appointments for a given day
 * for the business owner
 */
public class BusinessScheduleDay extends Fragment {

    private Date currentDay;
    private Date nextDay;
    private RecyclerView appointments_list;
    private DailyAppointmentListAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_business_schedule_day,
                container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle b = getArguments();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            currentDay = sdf.parse(b.getInt("day") + "/" +
                    (b.getInt("month") + 1) + "/" + b.getInt("year"));
            Calendar c  = Calendar.getInstance();
            c.setTime(currentDay);
            c.add(Calendar.DATE, 1);
            nextDay = c.getTime();
        } catch (ParseException e) {
            Log.d(this.toString(), "wrong format!");
        }

        appointments_list = getActivity().findViewById(R.id.daily_appointments_list);

        layoutManager = new LinearLayoutManager(getContext());
        appointments_list.setLayoutManager(layoutManager);

        Query query = FirebaseFirestore.getInstance()
                .collection(APPOINTMENTS_COLLECTION)
                .whereEqualTo("business_id",
                        FirebaseAuth.getInstance().getCurrentUser().getUid())
                .whereGreaterThanOrEqualTo("date", currentDay)
                .whereLessThan("date", nextDay)
                .orderBy("date");

        FirestoreRecyclerOptions<Appointment> options =
                new FirestoreRecyclerOptions.Builder<Appointment>()
                        .setQuery(query, Appointment.class)
                        .build();
        mAdapter = new DailyAppointmentListAdapter((ViewGroup)view, getContext(), options);
        appointments_list.setAdapter(mAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        mAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        mAdapter.stopListening();
        Objects.requireNonNull(getActivity())
                .findViewById(R.id.business_schedule_tabs)
                .setVisibility(View.VISIBLE);
    }
}

