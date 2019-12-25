package com.technion.cue.BusinessFeatures;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.technion.cue.R;
import com.technion.cue.data_classes.Appointment;
import com.technion.cue.data_classes.Business;

import static com.technion.cue.FirebaseCollections.BUSINESSES_COLLECTION;
import static com.technion.cue.FirebaseCollections.CLIENTELE_COLLECTION;

public class ClienteleList extends AppCompatActivity {


    ClienteleListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clientele_list);

        RecyclerView clientele = findViewById(R.id.clientele_list);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        clientele.setLayoutManager(layoutManager);

        Query query = FirebaseFirestore.getInstance()
                .collection(BUSINESSES_COLLECTION)
                .document(FirebaseAuth.getInstance().getUid())
                .collection(CLIENTELE_COLLECTION)
                .orderBy("name");

        FirestoreRecyclerOptions<Business.ClienteleMember> options =
                new FirestoreRecyclerOptions.Builder<Business.ClienteleMember>()
                        .setQuery(query, Business.ClienteleMember.class)
                        .build();

        mAdapter = new ClienteleListAdapter(this, options);
        clientele.setAdapter(mAdapter);
    }


    class ClienteleListAdapter extends
            FirestoreRecyclerAdapter<Business.ClienteleMember, ClienteleListAdapter.itemHolder> {

        private final Context context;
        private ViewGroup parentView = null;
        private boolean useDivider = true;

        ClienteleListAdapter(Context context,
                             FirestoreRecyclerOptions<Business.ClienteleMember> options) {
            super(options);
            this.context = context;
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
         *
         * @param holder:   the holder class inside the recycler view
         * @param position: position of the item inside the list
         * @param cm:       the clientele member represented by the item
         */
        @Override
        protected void onBindViewHolder(@NonNull ClienteleListAdapter.itemHolder holder,
                                        int position,
                                        @NonNull Business.ClienteleMember cm) {

        }

        @NonNull
        @Override
        public ClienteleListAdapter.itemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.clientele_row, parent, false);
            return new ClienteleListAdapter.itemHolder(v);
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
}
