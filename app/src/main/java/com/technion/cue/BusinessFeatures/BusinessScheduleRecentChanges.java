package com.technion.cue.BusinessFeatures;

import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.technion.cue.R;
import com.technion.cue.annotations.ModuleAuthor;
import com.technion.cue.data_classes.Business;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.technion.cue.FirebaseCollections.APPOINTMENT_ACTIONS_COLLECTION;
import static com.technion.cue.FirebaseCollections.BUSINESSES_COLLECTION;

/**
 * this fragment will display some of the recent changes
 * in the business schedule, including newly scheduled appointments,
 * rescheduled appointments and cancellations
 */
@ModuleAuthor("Ophir")
public class BusinessScheduleRecentChanges extends Fragment {

    private RecyclerView recent_changes_list;
    private RecentChangesAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        getActivity().findViewById(R.id.progress_bar).setVisibility(View.VISIBLE);
        return inflater.inflate(R.layout.fragment_business_recent_changes,
                container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recent_changes_list = view.findViewById(R.id.recent_changes_list);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recent_changes_list.setLayoutManager(layoutManager);

        // a query to get all the 10 most recent changes in the business'es schedule
        Query query = FirebaseFirestore.getInstance()
                .collection(BUSINESSES_COLLECTION)
                .document(FirebaseAuth.getInstance().getUid())
                .collection(APPOINTMENT_ACTIONS_COLLECTION)
                .orderBy("action_date", Query.Direction.DESCENDING)
                .limit(10);

        FirestoreRecyclerOptions<Business.AppointmentAction> options =
                new FirestoreRecyclerOptions.Builder<Business.AppointmentAction>()
                        .setQuery(query, Business.AppointmentAction.class)
                        .build();

        mAdapter = new RecentChangesAdapter(options);
        recent_changes_list.setAdapter(mAdapter);
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
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().findViewById(R.id.progress_bar).setVisibility(View.GONE);
    }

    private class RecentChangesAdapter extends
            FirestoreRecyclerAdapter<Business.AppointmentAction, RecentChangesAdapter.itemHolder> {

        RecentChangesAdapter(@NonNull FirestoreRecyclerOptions<Business.AppointmentAction> options) {
            super(options);
        }

        @Override
        protected void onBindViewHolder(@NonNull itemHolder holder,
                                        int position,
                                        @NonNull Business.AppointmentAction model) {

            getActivity().findViewById(R.id.progress_bar).setVisibility(View.GONE);

            SimpleDateFormat sdf_date = new SimpleDateFormat("EEEE, dd MMMM yyyy");
            SimpleDateFormat sdf_time = new SimpleDateFormat("HH:mm");

            holder.actionDate.setText(sdf_date.format(model.action_date) + " at " +
                    sdf_time.format(model.action_date));

            String coloredText;

            switch (model.action_type) {
                case "scheduling":
                    coloredText = "<b><font color=\"#267838\">scheduled</font></b>";
                    holder.actionMessage.setText(
                            Html.fromHtml(model.client_name
                                            + " has "
                                            + coloredText
                                            + " an appointment of type "
                                            + model.appointment_type
                                            + " on "
                                            + sdf_date.format(model.appointment_date)
                                            + " at "
                                            + sdf_time.format(model.appointment_date)
                            )
                    );
                    break;
                case "rescheduling":
                    coloredText = "<b><font color=\"#312980\">rescheduled</font></b>";
                    holder.actionMessage.setText(
                            Html.fromHtml(model.client_name
                                            + " has "
                                            + coloredText
                                            + " from an appointment of type "
                                            + model.appointment_type + " in "
                                            + sdf_date.format(model.appointment_date)
                                            + ", at "
                                            + sdf_time.format(model.appointment_date)
                                            + " to an appointment of type "
                                            + model.new_appointment_type
                                            + " on "
                                            + sdf_date.format(model.new_appointment_date)
                                            + " at "
                                            + sdf_time.format(model.new_appointment_date)
                            )
                    );
                    break;
                case "cancellation":
                    coloredText = "<b><font color=\"#9C3A10\">cancelled</font></b>";
                    holder.actionMessage.setText(
                            Html.fromHtml(model.client_name
                                            + " has "
                                            + coloredText
                                            + " an appointment of type "
                                            + model.appointment_type
                                            + " that was due to happen on "
                                            + sdf_date.format(model.appointment_date)
                                            + " at "
                                            + sdf_time.format(model.appointment_date)
                            )
                    );
                    break;
            }

        }

        @NonNull
        @Override
        public itemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.recent_changes_row, parent,false);
            return new itemHolder(v);
        }

        private class itemHolder extends RecyclerView.ViewHolder {

            TextView actionDate;
            TextView actionMessage;

            itemHolder(@NonNull View itemView) {
                super(itemView);
                actionDate = itemView.findViewById(R.id.action_date);
                actionMessage = itemView.findViewById(R.id.action_message);
            }
        }
    }



}
