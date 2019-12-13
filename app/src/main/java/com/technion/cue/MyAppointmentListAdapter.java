package com.technion.cue;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;


public class MyAppointmentListAdapter extends FirestoreRecyclerAdapter<CAppointmentListItem, MyAppointmentListAdapter.itemHolder >  {


    public MyAppointmentListAdapter(@NonNull FirestoreRecyclerOptions<CAppointmentListItem> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull itemHolder holder, int position, @NonNull CAppointmentListItem model) {
            holder.business.setText(model.getBusiness());
            holder.date.setText(model.getDate());
            holder.notes.setText(model.getNotes());
            holder.type.setText(model.getType());
    }

    @NonNull
    @Override
    public itemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.my_appointment_list_adapter,parent,false);
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
