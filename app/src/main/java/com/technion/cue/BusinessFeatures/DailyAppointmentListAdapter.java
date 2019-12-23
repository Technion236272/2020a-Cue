package com.technion.cue.BusinessFeatures;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.technion.cue.R;
import com.technion.cue.data_classes.Appointment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import static com.technion.cue.FirebaseCollections.APPOINTMENTS_COLLECTION;
import static com.technion.cue.FirebaseCollections.BUSINESSES_COLLECTION;
import static com.technion.cue.FirebaseCollections.CLIENTS_COLLECTION;
import static com.technion.cue.FirebaseCollections.TYPES_COLLECTION;

public class DailyAppointmentListAdapter extends
        FirestoreRecyclerAdapter<Appointment, DailyAppointmentListAdapter.itemHolder> {

    private final Context context;
    private ViewGroup parentView = null;
    private boolean useDivider = true;

    DailyAppointmentListAdapter(ViewGroup view, Context context,
                                FirestoreRecyclerOptions<Appointment> options) {
        super(options);
        this.parentView = view;
        this.context = context;
    }

    DailyAppointmentListAdapter(Context context,
                                @NonNull FirestoreRecyclerOptions<Appointment> options,
                                boolean useDivider) {
        super(options);
        this.context = context;
        this.useDivider = useDivider;
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
                                    .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .collection(TYPES_COLLECTION)
                                    .document(appointment.type)
                                    .get()
                                    .addOnSuccessListener(documentSnapshot -> {
                                        Map<String, String> attributes =
                                                (Map<String, String>)documentSnapshot
                                                        .get("attributes");
                                        Calendar c = Calendar.getInstance();
                                        c.setTime(a.date);
                                        c.add(Calendar.MINUTE,
                                                Integer.valueOf(attributes.get("duration")));
                                        if (c.getTime().getTime() >= currentTime.getTime())
                                            holder.flag.setVisibility(View.VISIBLE);
                                        else {
                                            holder.type.setTextColor(context.getResources()
                                                    .getColor(R.color.TextOnBackgroundTransparent));
                                            holder.client.setTextColor(context.getResources()
                                                    .getColor(R.color.TextOnBackgroundTransparent));
                                            holder.date.setTextColor(context.getResources()
                                                    .getColor(R.color.TextOnBackgroundTransparent));
                                        }
                                    });
                        } else if (appointment.date.getTime() < currentTime.getTime()) {
                            holder.type.setTextColor(context.getResources()
                                    .getColor(R.color.TextOnBackgroundTransparent));
                            holder.client.setTextColor(context.getResources()
                                    .getColor(R.color.TextOnBackgroundTransparent));
                            holder.date.setTextColor(context.getResources()
                                    .getColor(R.color.TextOnBackgroundTransparent));
                        }
                    }
                });
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
}