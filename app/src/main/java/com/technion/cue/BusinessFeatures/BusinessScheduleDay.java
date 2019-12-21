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
import android.widget.CalendarView;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
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

import static com.technion.cue.FirebaseCollections.APPOINTMENTS_COLLECTION;
import static com.technion.cue.FirebaseCollections.BUSINESSES_COLLECTION;
import static com.technion.cue.FirebaseCollections.CLIENTS_COLLECTION;
import static com.technion.cue.FirebaseCollections.TYPES_COLLECTION;

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

        Query query = FirebaseFirestore.getInstance().collection(APPOINTMENTS_COLLECTION)
                .whereEqualTo("business_id",
                        FirebaseAuth.getInstance().getCurrentUser().getUid())
                .whereGreaterThanOrEqualTo("date", currentDay)
                .whereLessThan("date", nextDay)
                .orderBy("date");
        FirestoreRecyclerOptions<Appointment> options =
                new FirestoreRecyclerOptions.Builder<Appointment>()
                        .setQuery(query, Appointment.class)
                        .build();
        mAdapter = new DailyAppointmentListAdapter(options);
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
        getActivity().findViewById(R.id.business_schedule_tabs).setVisibility(View.VISIBLE);
    }

    static class DailyAppointmentListAdapter extends
            FirestoreRecyclerAdapter<Appointment, DailyAppointmentListAdapter.itemHolder> {


        DailyAppointmentListAdapter(@NonNull FirestoreRecyclerOptions<Appointment> options) {
            super(options);
        }

        @Override
        protected void onBindViewHolder(@NonNull itemHolder holder,
                                        int position,
                                        @NonNull Appointment appointment) {

            // TODO: change date display format

            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            holder.date.setText(sdf.format(appointment.date));
            FirebaseFirestore.getInstance()
                    .collection(BUSINESSES_COLLECTION)
                    .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .collection(TYPES_COLLECTION)
                    .document(appointment.type)
                    .get()
                    .addOnSuccessListener(l -> {
                        holder.type.setText(l.getString("name"));
                    });
            FirebaseFirestore.getInstance()
                    .collection(CLIENTS_COLLECTION)
                    .document(appointment.client_id)
                    .get()
                    .addOnSuccessListener(l -> {
                        holder.client.setText(l.getString("name"));
                    });
        }

        @NonNull
        @Override
        public itemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.business_appointment_row, parent,false);
            return new itemHolder(v);
        }

        class itemHolder extends RecyclerView.ViewHolder {
            TextView client;
            TextView date;
            TextView type;

            public itemHolder(@NonNull View itemView) {
                super(itemView);
                client = itemView.findViewById(R.id.BO_list_client_name);
                date = itemView.findViewById(R.id.BO_list_date);
                type = itemView.findViewById(R.id.BO_list_appointment_type);
            }
        }
    }
}

