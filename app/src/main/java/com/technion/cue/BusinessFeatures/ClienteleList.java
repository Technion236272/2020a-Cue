package com.technion.cue.BusinessFeatures;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.technion.cue.ClientFeatures.EditAppointmentActivity;
import com.technion.cue.R;
import com.technion.cue.annotations.ModuleAuthor;
import com.technion.cue.data_classes.Business;

import static com.technion.cue.FirebaseCollections.BUSINESSES_COLLECTION;
import static com.technion.cue.FirebaseCollections.CLIENTELE_COLLECTION;

/**
 * This activity display a list filled with the business'es clientele
 * The client are sorted alphabetically (in an ascending order).
 * Clicking on each client item pops up a dialog with additional details about the client
 */
@ModuleAuthor("Ophir Eyal")
public class ClienteleList extends AppCompatActivity implements BusinessBottomMenu {


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
                .orderBy("bo_name");

        FirestoreRecyclerOptions<Business.ClienteleMember> options =
                new FirestoreRecyclerOptions.Builder<Business.ClienteleMember>()
                        .setQuery(query, Business.ClienteleMember.class)
                        .build();

        mAdapter = new ClienteleListAdapter(options);
        clientele.setAdapter(mAdapter);

        BottomNavigationView bnv = findViewById(R.id.bottom_navigation);
        bnv.getMenu().getItem(2).setChecked(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAdapter.stopListening();
    }

    class ClienteleListAdapter extends
            FirestoreRecyclerAdapter<Business.ClienteleMember, ClienteleListAdapter.itemHolder> {

        ClienteleListAdapter(FirestoreRecyclerOptions<Business.ClienteleMember> options) {
            super(options);
        }


        class itemHolder extends RecyclerView.ViewHolder {
            TextView client;
            TextView firstLetter;

            itemHolder(@NonNull View itemView) {
                super(itemView);
                client = itemView.findViewById(R.id.client_entry);
                firstLetter = itemView.findViewById(R.id.first_letter);

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

            char cflTemp = cm.name.charAt(0);
            char currentFirstLetter = cflTemp;
            cflTemp++;
            char nextFirstLetter = cflTemp;
            // If the client item is the first item (alphabetically) that begins
            // with a certain letter, display a large icon of that letter next to the item
            FirebaseFirestore.getInstance()
                    .collection(BUSINESSES_COLLECTION)
                    .document(FirebaseAuth.getInstance().getUid())
                    .collection(CLIENTELE_COLLECTION)
                    .whereGreaterThanOrEqualTo("bo_name", String.valueOf(currentFirstLetter))
                    .whereLessThan("bo_name", String.valueOf(nextFirstLetter))
                    .orderBy("bo_name")
                    .limit(1)
                    .get()
                    .addOnSuccessListener(documentSnapshots -> {
                        if (!documentSnapshots.isEmpty() &&
                                documentSnapshots.getDocuments()
                                        .get(0).getString("client_id").equals(cm.client_id)) {
                            holder.firstLetter.setText(String.valueOf(cm.name.charAt(0)));
                            holder.firstLetter.setVisibility(View.VISIBLE);
                        }
                        holder.client.setText(cm.name);
                    });

            holder.client.setOnClickListener(cl ->
                    new ClientInformationDialog(cm.client_id)
                            .show(getSupportFragmentManager(), null));
        }

        @NonNull
        @Override
        public ClienteleListAdapter.itemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.clientele_row, parent, false);
            return new ClienteleListAdapter.itemHolder(v);
        }
    }

    public void openBusinessSchedule(MenuItem item) {
        Intent intent = new Intent(getBaseContext(),BusinessSchedule.class);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }

    public void openBusinessHomepage(MenuItem item) {
        Intent intent = new Intent(getBaseContext(), BOBusinessHomePage.class);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }

    public void openBusinessClientele(MenuItem item) { }
}
