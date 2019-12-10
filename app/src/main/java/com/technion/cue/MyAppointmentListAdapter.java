package com.technion.cue;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;


public class MyAppointmentListAdapter extends FirestoreRecyclerAdapter<CAppointmentListItem, MyAppointmentListAdapter.itemHolder>  {

    private FirebaseFirestore db;

    public MyAppointmentListAdapter(@NonNull FirestoreRecyclerOptions<CAppointmentListItem> options) {
        super(options);
    }
    @Override
    protected void onBindViewHolder(@NonNull itemHolder holder, int position, @NonNull CAppointmentListItem model) {


//        DocumentReference appointmentRef = db.document(model.getAppointment_business_ref());
////
//        if (model.getContent_item().toString().length() > 0) {
            holder.appointmentName.setText(((Character) model.getAppointment_business_ref().charAt(0)).toString());
            holder.appointmentTime.setText(model.getAppointment_business_ref());
//        }
    }

    @NonNull
    @Override
    public itemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.my_appointment_list_adapter,parent,false);
        return new itemHolder(v);
    }

    class itemHolder extends RecyclerView.ViewHolder {
        TextView appointmentName;
        TextView appointmentTime;


        public itemHolder(@NonNull View itemView) {
            super(itemView);
            appointmentName = itemView.findViewById(R.id.appointmentName);
            appointmentTime = itemView.findViewById(R.id.appointmentTime);

        }
    }
}
