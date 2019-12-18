package com.technion.cue.ClientFeatures;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.technion.cue.R;
import com.technion.cue.data_classes.Appointment;


public class MyAppointmentListAdapter extends
        FirestoreRecyclerAdapter<Appointment, MyAppointmentListAdapter.itemHolder>  {


    public MyAppointmentListAdapter(@NonNull FirestoreRecyclerOptions<Appointment> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull itemHolder holder, int position, @NonNull Appointment model) {
            // TODO: the texts should be the business & type names. currently, their document ids will be displayed
            holder.business.setText(model.business_id);
            holder.date.setText(model.date.toString());
            holder.notes.setText(model.notes);
            holder.type.setText(model.type);
            holder.business.setTag(model.business_id); // -- ben - 18.12
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
