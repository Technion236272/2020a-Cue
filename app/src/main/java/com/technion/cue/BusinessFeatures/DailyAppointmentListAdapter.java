package com.technion.cue.BusinessFeatures;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.technion.cue.ClientFeatures.EditAppointmentActivity;
import com.technion.cue.R;
import com.technion.cue.annotations.ModuleAuthor;
import com.technion.cue.data_classes.Appointment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import static com.technion.cue.FirebaseCollections.APPOINTMENTS_COLLECTION;
import static com.technion.cue.FirebaseCollections.BUSINESSES_COLLECTION;
import static com.technion.cue.FirebaseCollections.CLIENTS_COLLECTION;
import static com.technion.cue.FirebaseCollections.TYPES_COLLECTION;

/**
 * A FirestoreRecyclerAdapter for displaying all appointments today.
 * This is used by the fragment which display the daily and weekly appointments
 */
@ModuleAuthor("Ophir Eyal")
public class DailyAppointmentListAdapter extends
        FirestoreRecyclerAdapter<Appointment, DailyAppointmentListAdapter.itemHolder> {

    private final Context context;
    private final FragmentActivity activity;
    private ViewGroup parentView = null;
    private boolean useDivider = true;

    DailyAppointmentListAdapter(FragmentActivity activity, ViewGroup view,
                                Context context,
                                FirestoreRecyclerOptions<Appointment> options) {
        super(options);
        this.parentView = view;
        this.context = context;
        this.activity = activity;
    }

    DailyAppointmentListAdapter(FragmentActivity activity, Context context,
                                @NonNull FirestoreRecyclerOptions<Appointment> options,
                                boolean useDivider) {
        super(options);
        this.context = context;
        this.useDivider = useDivider;
        this.activity = activity;
    }

    class itemHolder extends RecyclerView.ViewHolder {
        View flag;
        TextView client;
        TextView date;
        TextView type;

        itemHolder(@NonNull View itemView) {
            super(itemView);
            client = itemView.findViewById(R.id.BO_list_client_name);
            date = itemView.findViewById(R.id.BO_list_date);
            type = itemView.findViewById(R.id.BO_list_appointment_type);
            flag = itemView.findViewById(R.id.current_appointment_flag);
            // if there is no more than one item, or if it was stated explicitly,
            // a divider will not be used
            if (getItemCount() <= 1 || !useDivider) {
                itemView.findViewById(R.id.divider).setVisibility(View.GONE);
            }
        }
    }

    /**
     * finds appointment date, type & client from FireStore database and displays them
     * @param holder: the holder class inside the recycler view
     * @param position: position of the item inside the list
     * @param appointment: the appointment represented in the item
     */
    @Override
    protected void onBindViewHolder(@NonNull itemHolder holder,
                                    int position,
                                    @NonNull Appointment appointment) {

        holder.itemView.findViewById(R.id.appointment_item).setOnClickListener(cl -> {
            Bundle bundle = new Bundle();
            bundle.putString("business_id", FirebaseAuth.getInstance().getUid());
            bundle.putString("notes", appointment.id);
            Intent intent = new Intent(context, EditAppointmentActivity.class);
            intent.putExtras(bundle);
            activity.startActivity(intent);
        });

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        holder.date.setText(sdf.format(appointment.date));
        FirebaseFirestore.getInstance()
                .collection(BUSINESSES_COLLECTION)
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection(TYPES_COLLECTION)
                .document(appointment.type)
                .get()
                .addOnSuccessListener(l -> holder.type.setText(l.getString("name")));
        FirebaseFirestore.getInstance()
                .collection(CLIENTS_COLLECTION)
                .document(appointment.client_id)
                .get()
                .addOnSuccessListener(l -> holder.client.setText(l.getString("name")));

        Date currentTime = new Date(System.currentTimeMillis());
        FirebaseFirestore.getInstance()
                .collection(APPOINTMENTS_COLLECTION)
                .whereLessThanOrEqualTo("date", currentTime)
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(l -> {
                    if (!l.isEmpty()) {
                        Appointment a = l.getDocuments().get(0).toObject(Appointment.class);
                        if (a.equals(appointment)) {
                            FirebaseFirestore.getInstance()
                                    .collection(BUSINESSES_COLLECTION)
                                    .document(FirebaseAuth.getInstance()
                                            .getCurrentUser().getUid())
                                    .collection(TYPES_COLLECTION)
                                    .document(appointment.type)
                                    .get()
                                    .addOnSuccessListener(documentSnapshot -> {
                                        Map<String, String> attributes =
                                                (Map<String, String>) documentSnapshot
                                                        .get("attributes");
                                        Calendar c = Calendar.getInstance();
                                        c.setTime(a.date);
                                        c.add(Calendar.MINUTE,
                                                Integer.valueOf(attributes.get("duration")));
                                        // checks whether the bound appointment is the
                                        // current appointment as well.
                                        // If so, display a flag icon next to it
                                        if (c.getTime().getTime() >= currentTime.getTime()){
                                            holder.flag.setVisibility(View.VISIBLE);
                                            holder.type.setTextColor(context.getResources()
                                                    .getColor(R.color.secondaryTextColor));
                                            holder.client.setTextColor(context.getResources()
                                                    .getColor(R.color.secondaryTextColor));
                                            holder.date.setTextColor(context.getResources()
                                                    .getColor(R.color.secondaryTextColor));
                                        }
                                        else {
                                            holder.type.setTextColor(context.getResources()
                                                    .getColor(R.color.transparentTextOnBackground));
                                            holder.client.setTextColor(context.getResources()
                                                    .getColor(R.color.transparentTextOnBackground));
                                            holder.date.setTextColor(context.getResources()
                                                    .getColor(R.color.transparentTextOnBackground));
                                        }
                                    });
                            // if the appointment's time has passed, display it
                            // with a gray color
                        } else if (appointment.date.getTime() < currentTime.getTime()) {
                            holder.type.setTextColor(context.getResources()
                                    .getColor(R.color.transparentTextOnBackground));
                            holder.client.setTextColor(context.getResources()
                                    .getColor(R.color.transparentTextOnBackground));
                            holder.date.setTextColor(context.getResources()
                                    .getColor(R.color.transparentTextOnBackground));
                            // if th appointment has not happened yet, disallow marking it as "NO-SHOW"
                        } else {
                            holder.itemView.findViewById(R.id.no_show_mark).setClickable(false);
                        }
                    }
                });

        RadioButton no_show = holder.itemView.findViewById(R.id.no_show_mark);
        no_show.setChecked(appointment.no_show);

        no_show.setOnCheckedChangeListener((buttonView, isChecked) ->
                FirebaseFirestore.getInstance()
                        .collection(APPOINTMENTS_COLLECTION)
                        .document(appointment.id)
                        .update("no_show", isChecked)
        );

        noShowClarify(holder, appointment.client_id);

    }

    @NonNull
    @Override
    public itemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.business_appointment_row, parent,false);
        return new itemHolder(v);
    }

    /**
     * checks whether there are items in the list.
     * If there are none, display the "no_appointments_message"
     * If there are at least one, remove the message from the view
     */
    @Override
    public void onDataChanged() {
        super.onDataChanged();
        if (parentView != null) {
            if (getItemCount() == 0) {
                parentView.findViewById(R.id.no_appointments_message).setVisibility(View.VISIBLE);
            } else {
                parentView.findViewById(R.id.no_appointments_message).setVisibility(View.GONE);
                // TODO: check if the flag needs to be moved
                //  to a different meeting / assigned to a meeting
                // TODO: check if need to change color of text inside items
            }
        }
    }

    private void noShowClarify(itemHolder holder, String client_id) {
        FirebaseFirestore.getInstance()
                .collection(APPOINTMENTS_COLLECTION)
                .whereEqualTo("client_id", client_id)
                .get()
                .addOnSuccessListener(documentSnapshots -> {
                    double size = documentSnapshots.size();
                    double no_show_num = 0;
                    for (DocumentSnapshot ds : documentSnapshots) {
                        if (ds.getString("business_id").equals(FirebaseAuth.getInstance().getUid())
                                && ds.contains("no_show")
                                && ds.getBoolean("no_show")) {
                            no_show_num++;
                        }
                    }

                    if (no_show_num >= ((1.0/3.0) * size)) {
                        holder.client.setTextColor(Color.RED);
                    }
                });
    }
}