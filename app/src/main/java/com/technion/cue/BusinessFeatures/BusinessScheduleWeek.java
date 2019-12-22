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
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.technion.cue.R;
import com.technion.cue.data_classes.Appointment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import static com.technion.cue.FirebaseCollections.APPOINTMENTS_COLLECTION;

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

        List<Task> task_list = new ArrayList<>();
        List<Query> query_list = new ArrayList<>();
        AtomicInteger size = new AtomicInteger();
        for (Date date : week_days) {
            c.setTime(date);
            c.set(Calendar.HOUR_OF_DAY, 0);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
            Date today = c.getTime();
            c.add(Calendar.DATE, 1);
            Date nextDay = c.getTime();

            Query query = FirebaseFirestore.getInstance().collection(APPOINTMENTS_COLLECTION)
                    .whereEqualTo("business_id",
                            FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .whereGreaterThanOrEqualTo("date", today)
                    .whereLessThan("date", nextDay)
                    .orderBy("date");
            query_list.add(query);
            task_list.add(query.get().addOnSuccessListener(l -> {
                if (!l.isEmpty()) {
                    size.getAndIncrement();
                }
            }));
            FirestoreRecyclerOptions<Appointment> options =
                    new FirestoreRecyclerOptions.Builder<Appointment>()
                            .setQuery(query, Appointment.class)
                            .build();
            DailyAppointmentListAdapter daily_appointments_adapter =
                    new DailyAppointmentListAdapter(getContext(), options, false);
            daily_adapters.put(date, daily_appointments_adapter);
        }

        AtomicInteger completed = new AtomicInteger();
        for (Task t : task_list) {
            Tasks.whenAll(t).addOnSuccessListener(l -> {
                completed.getAndIncrement();
                if (completed.intValue() == NUMBER_OF_DAYS_IN_WEEK) {
                    WeeklyAppointmentListAdapter mAdapter =
                            new WeeklyAppointmentListAdapter(query_list, size.intValue());
                    week_days_list.setAdapter(mAdapter);
                }
            });
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

        private final List<Query> queryList;
        private List<Task> waitingTasks = new ArrayList<>();
        int size;

        class ItemHolder extends RecyclerView.ViewHolder {
            TextView day_of_the_week;
            View item_view;
            ItemHolder(@NonNull View itemView) {
                super(itemView);
                this.day_of_the_week = itemView.findViewById(R.id.day_of_the_week);
                this.item_view = itemView;
            }
        }

        WeeklyAppointmentListAdapter(List<Query> queryList, int size) {
            super();
            this.size = size;
            this.queryList = queryList;
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

            queryList.get(position)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                            Date position_date = week_days.get(position);
                            holder.day_of_the_week.setText(sdf.format(position_date));
                            if (DateUtils.isToday(position_date.getTime())) {
                                holder.day_of_the_week.setTextColor(getResources()
                                        .getColor(R.color.ColorSecondaryLight));
                            }

                            RecyclerView appointments_list =
                                    holder.item_view.findViewById(R.id.appointment_list_for_day);
                            layoutManager = new LinearLayoutManager(getContext());
                            appointments_list.setLayoutManager(layoutManager);
                            appointments_list.setAdapter(daily_adapters.get(position_date));
                            if (queryDocumentSnapshots.isEmpty()) {
                                holder.item_view
                                        .findViewById(R.id.week_day_divider)
                                        .setVisibility(View.GONE);
                            }
                        });
        }

        @Override
        public int getItemCount() {
            return size;
        }
    }
}

