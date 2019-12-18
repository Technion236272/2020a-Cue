package com.technion.cue.ClientFeatures;

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
import com.technion.cue.R;
import com.technion.cue.data_classes.Appointment;

import java.text.SimpleDateFormat;

import static com.technion.cue.FirebaseCollections.BUSINESSES_COLLECTION;
import static com.technion.cue.FirebaseCollections.CLIENTS_COLLECTION;
import static com.technion.cue.FirebaseCollections.TYPES_COLLECTION;


public class MyAppointmentListAdapter extends
        FirestoreRecyclerAdapter<Appointment, MyAppointmentListAdapter.itemHolder>  {


    public MyAppointmentListAdapter(@NonNull FirestoreRecyclerOptions<Appointment> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull itemHolder holder, int position, @NonNull Appointment appointment) {
            // TODO: the texts should be the business & type names. currently, their document ids will be displayed


        holder.business.setTag(appointment.business_id);

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM/YYYY");
        holder.date.setText(sdf.format(appointment.date));
        FirebaseFirestore.getInstance()
                .collection(BUSINESSES_COLLECTION)
                .document(appointment.business_id)
                .get()
                .addOnSuccessListener(l -> {
                    holder.business.setText(l.getString("business_name"));
                });
        FirebaseFirestore.getInstance()
                .collection(BUSINESSES_COLLECTION)
                .document(appointment.business_id)
                .collection(TYPES_COLLECTION)
                .document(appointment.type)
                .get()
                .addOnSuccessListener(l -> {
                    holder.type.setText(l.getString("name"));
                });
            holder.notes.setText(appointment.notes);

    }

    @NonNull
    @Override
    public itemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.my_appointment_list_adapter, parent,false);
        return new itemHolder(v);
    }

    class itemHolder extends RecyclerView.ViewHolder {
        TextView business;
        TextView date;
        TextView notes;
        TextView type;

        public itemHolder(@NonNull View itemView) {
            super(itemView);
            business = itemView.findViewById(R.id.business);
            date = itemView.findViewById(R.id.date);
            notes = itemView.findViewById(R.id.notes);
            type = itemView.findViewById(R.id.type);

        }
    }
}
