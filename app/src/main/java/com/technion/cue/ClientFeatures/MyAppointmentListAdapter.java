package com.technion.cue.ClientFeatures;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.technion.cue.R;
import com.technion.cue.data_classes.Appointment;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.technion.cue.FirebaseCollections.APPOINTMENTS_COLLECTION;
import static com.technion.cue.FirebaseCollections.BUSINESSES_COLLECTION;
import static com.technion.cue.FirebaseCollections.TYPES_COLLECTION;

/**
 * MyAppointmentListAdapter - adapter for appointment list
 * which load information from firebase firestore into cell using
 * object type(Appointment) that we set before.
 * */
public class MyAppointmentListAdapter extends
        FirestoreRecyclerAdapter<Appointment, MyAppointmentListAdapter.itemHolder>  {

    private ViewGroup parentView;

    MyAppointmentListAdapter(ViewGroup view, Context context,
                                FirestoreRecyclerOptions<Appointment> options) {
        super(options);
        this.parentView = view;

    }

    public MyAppointmentListAdapter(@NonNull FirestoreRecyclerOptions<Appointment> options) {
        super(options);

    }


    /**
     * onBindViewHolder - bring from firebase information
     * about this appointment(using appointment id)
     * */
    @Override
    protected void onBindViewHolder(@NonNull itemHolder holder, int position, @NonNull Appointment appointment) {
            // TODO: the texts should be the business & type names. currently, their document ids will be displayed
        holder.business.setTag(R.id.business_info,appointment.business_id);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM/YYYY");
        holder.date.setText(sdf.format(appointment.date));
        FirebaseFirestore.getInstance()
                .collection(BUSINESSES_COLLECTION)
                .document(appointment.business_id)
                .get()
                .addOnSuccessListener(l -> {
                    holder.business.setText(l.getString("business_name"));
                    holder.business_id = (appointment.business_id);
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
        FirebaseFirestore.getInstance()
                .collection(APPOINTMENTS_COLLECTION)
                .whereEqualTo("client_id",appointment.client_id)
                .whereEqualTo("business_id",appointment.business_id)
                .whereEqualTo("date", new Timestamp(appointment.date))
                .limit(1)
                .get()
                .addOnSuccessListener(l -> {
                    if (l.getDocuments().size() > 0 ) {
                        holder.appointment_id = l.getDocuments().get(0).getId();
                    }
//                    holder.business.setTag(R.id.myAppointmentList,holder.notes); // need to change - ben
                });

            holder.notes.setText(appointment.notes == null ? "No note yet." : appointment.notes);
            holder.dateType = appointment.date;



    }
    /**
     * onCreateViewHolder - add a bundle for appointment page with
     * all needed information(optimise) like :
     * business name , appointment date,type'notes..
     * */
    @NonNull
    @Override
    public itemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.my_appointment_list_adapter, parent,false);
        itemHolder holder = new itemHolder(v);

        v.setOnClickListener(v1 -> {
            Intent getIntentBOPage = new Intent(parent.getContext(), ClientAppointmentPage.class);
            // TODO: start using appointmet object !!! - refactoring later.
            getIntentBOPage.putExtra("business_name",holder.business.getText());
            getIntentBOPage.putExtra("appointment_type",holder.type.getText());
            getIntentBOPage.putExtra("appointment_date",holder.date.getText());
            getIntentBOPage.putExtra("appointment_date_type",holder.dateType);
            getIntentBOPage.putExtra("appointment_notes",holder.notes.getText());
            getIntentBOPage.putExtra("business_id",holder.business_id);
            getIntentBOPage.putExtra("appointment_id",holder.appointment_id);

            parent.getContext().startActivity(getIntentBOPage);

        });
        return holder;
    }

    class itemHolder extends RecyclerView.ViewHolder {
        TextView business;
        TextView date;
        TextView notes;
        TextView type;
        String appointment_id;
        String business_id;
        Date dateType;

        public itemHolder(@NonNull View itemView) {
            super(itemView);
            business = itemView.findViewById(R.id.appointment_box_business_name);
            date = itemView.findViewById(R.id.appointment_box_business_date);
            notes = itemView.findViewById(R.id.notes);
            type = itemView.findViewById(R.id.type);
            appointment_id ="";
            business_id ="";
            dateType=null;

        }
    }
    @Override
    public void onDataChanged() {
        super.onDataChanged();
        if ( parentView != null) {
            if (getItemCount() == 0) {
                parentView.findViewById(R.id.client_no_appointments_message_y).setVisibility(View.VISIBLE);
            } else {
                parentView.findViewById(R.id.client_no_appointments_message_y).setVisibility(View.GONE);
            }
        }
    }


}
