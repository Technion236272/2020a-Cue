package com.technion.cue.BusinessFeatures;


import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import androidx.annotation.NonNull;

import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.technion.cue.R;
import com.technion.cue.data_classes.Business.AppointmentType;

import static com.technion.cue.FirebaseCollections.BUSINESSES_COLLECTION;
import static com.technion.cue.FirebaseCollections.TYPES_COLLECTION;



import com.technion.cue.R;
import com.technion.cue.data_classes.Business.AppointmentType;

public class AppointmentTypesListAdapter
            extends FirestoreRecyclerAdapter<AppointmentType, AppointmentTypesListAdapter.typeHolder> {

    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        public void onItemClick(View view, int position);
    }

    AppointmentTypesListAdapter(@NonNull FirestoreRecyclerOptions<AppointmentType> options, OnItemClickListener onItemClickListener) {
        super(options);
        mOnItemClickListener = onItemClickListener;
    }

    @Override
    protected void onBindViewHolder(@NonNull typeHolder holder, int position, @NonNull AppointmentType appointmentType) {
        holder.type_text.setText(appointmentType.name);
    }


    @NonNull
    @Override
    public typeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.settings_type_holder
                , parent, false);
        typeHolder holder = new typeHolder(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnItemClickListener.onItemClick(v, holder.getAdapterPosition());
            }
        });
        return holder;
    }


    class typeHolder extends RecyclerView.ViewHolder {
        TextView type_text;

        typeHolder(@NonNull View itemView) {
            super(itemView);
            type_text = itemView.findViewById(R.id.businessType);
        }
    }
}