package com.technion.cue.BusinessFeatures;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.technion.cue.BusinessFeatures.BusinessScheduleDay.DailyAppointmentListAdapter;
import com.technion.cue.ClientFeatures.ClientChooseTimeFragment;
import com.technion.cue.R;
import com.technion.cue.data_classes.Appointment;

import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.technion.cue.FirebaseCollections.APPOINTMENTS_COLLECTION;
import static com.technion.cue.FirebaseCollections.BUSINESSES_COLLECTION;
import static com.technion.cue.FirebaseCollections.CLIENTS_COLLECTION;
import static com.technion.cue.FirebaseCollections.TYPES_COLLECTION;

/**
 * this activity will display a list of appointments for a given day
 * for the business owner
 */
public class BusinessScheduleWeek extends Fragment {

    private static int NUMBER_OF_DAYS_IN_WEEK = 7;

    private RecyclerView week_days_list;
    private RecyclerView.LayoutManager layoutManager;

    private List<Date> week_days = new ArrayList<>();
    private Map<Date, DailyAppointmentListAdapter> daily_adapters = new HashMap<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_business_schedule_week,
                container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        week_days_list = getActivity().findViewById(R.id.weekly_appointments_list);

        layoutManager = new LinearLayoutManager(getContext());
        week_days_list.setLayoutManager(layoutManager);
        week_days_list.setHasFixedSize(true);

        Calendar c = Calendar.getInstance();
        // TODO: allow business owners to choose which day the week begins
        for (int i = 1 ; i <= 7 ; i++) {
            c.set(Calendar.DAY_OF_WEEK, i);
            week_days.add(c.getTime());
        }

        WeeklyAppointmentListAdapter mAdapter = new WeeklyAppointmentListAdapter();
        week_days_list.setAdapter(mAdapter);

        for (Date date : week_days) {
            c.setTime(date);
            c.add(Calendar.DATE, 1);
            Date nextDay = c.getTime();

            Query query = FirebaseFirestore.getInstance().collection(APPOINTMENTS_COLLECTION)
                    .whereEqualTo("business_id",
                            FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .whereGreaterThanOrEqualTo("date", date)
                    .whereLessThan("date", nextDay)
                    .orderBy("date");
            FirestoreRecyclerOptions<Appointment> options =
                    new FirestoreRecyclerOptions.Builder<Appointment>()
                            .setQuery(query, Appointment.class)
                            .build();
            DailyAppointmentListAdapter daily_appointments_adapter =
                    new DailyAppointmentListAdapter(options);
            daily_adapters.put(date, daily_appointments_adapter);
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        for (DailyAppointmentListAdapter adapter : daily_adapters.values()) {
            adapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        for (DailyAppointmentListAdapter adapter : daily_adapters.values()) {
            adapter.stopListening();
        }
    }

    private class WeeklyAppointmentListAdapter extends
            RecyclerView.Adapter<WeeklyAppointmentListAdapter.ItemHolder> {

        class ItemHolder extends RecyclerView.ViewHolder {
            TextView day_of_the_week;
            View item_view;

            public ItemHolder(@NonNull View itemView) {
                super(itemView);
                day_of_the_week = itemView.findViewById(R.id.day_of_the_week);
                item_view = itemView;
            }
        }

        @NonNull
        @Override
        public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.business_weekly_holder, parent,false);
            return new ItemHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ItemHolder holder, int position) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            Date position_date = week_days.get(position);
            holder.day_of_the_week.setText(sdf.format(position_date));
            if (DateUtils.isToday(position_date.getTime())) {
                holder.day_of_the_week
                        .setBackgroundColor(getResources().getColor(R.color.ColorSecondaryLight));
            }

            RecyclerView appointments_list =
                    holder.item_view.findViewById(R.id.appointment_list_for_day);
            layoutManager = new LinearLayoutManager(getContext());
            appointments_list.setLayoutManager(layoutManager);
            appointments_list.setAdapter(daily_adapters.get(position_date));
        }

        @Override
        public int getItemCount() {
            return NUMBER_OF_DAYS_IN_WEEK;
        }
    }
}

