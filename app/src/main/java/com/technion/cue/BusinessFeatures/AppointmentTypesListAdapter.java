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

public class AppointmentTypesListAdapter
            extends FirestoreRecyclerAdapter<AppointmentType, AppointmentTypesListAdapter.TypeHolder> {

    private ViewGroup parentView;

    AppointmentTypesListAdapter(ViewGroup view, Context context,
                             FirestoreRecyclerOptions<AppointmentType> options) {
        super(options);
        this.parentView = view;
    }

        @NonNull
        @Override
        public AppointmentTypesListAdapter.TypeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.settings_type_holder
                    ,parent, false);
            TypeHolder holder = new TypeHolder(view);



            return holder;
        }

    public AppointmentTypesListAdapter(@NonNull FirestoreRecyclerOptions<AppointmentType> options) {
        super(options);
    }

        @Override
        public void onBindViewHolder(@NonNull TypeHolder holder, int position, @NonNull AppointmentType appointmentType) {
//            FirebaseFirestore.getInstance()
//                    .collection(BUSINESSES_COLLECTION)
//                    .document(FirebaseAuth.getInstance().getUid())
//                    .collection(TYPES_COLLECTION)
//                    .get()
//                    .addOnSuccessListener(l -> {
//                        for (DocumentSnapshot document : l.getDocuments())  {
//                            holder.type_text.setText(document.get("name").toString());
//                        }
//                    });

            holder.type_text.setText(appointmentType.name);
        }



        class TypeHolder extends RecyclerView.ViewHolder {
            TextView duration;
            TextView type_text;

            TypeHolder(@NonNull View itemView) {
                super(itemView);
                type_text = itemView.findViewById(R.id.businessType);
                duration = itemView.findViewById(R.id.businessTypeDuration);
            }
        }
    }

