package com.technion.cue.ClientFeatures;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.technion.cue.R;
import com.technion.cue.data_classes.Appointment;
import com.technion.cue.data_classes.Client;

import static com.technion.cue.FirebaseCollections.APPOINTMENTS_COLLECTION;


public class ClientHomePage extends AppCompatActivity {

    private MyAppointmentListAdapter appointmentAdapter;
    private MyFavoriteListAdapter favoriteAdapter;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_home_page);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        setUpRecyclerFavoriteView();
        setUpRecycleAppointmentAView();
    }

    private void setUpRecycleAppointmentAView() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        Query query = db.collection(APPOINTMENTS_COLLECTION)
                .whereEqualTo("client_id", currentUser.getUid())
                .orderBy("date");
        FirestoreRecyclerOptions<Appointment> options =
                new FirestoreRecyclerOptions.Builder<Appointment>()
                        .setQuery(query, Appointment.class)
                        .build();
        appointmentAdapter = new MyAppointmentListAdapter(options);
        RecyclerView recyclerView = findViewById(R.id.myAppointmentList);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(appointmentAdapter);
    }

    private void setUpRecyclerFavoriteView() {

        FirebaseUser currentUser = mAuth.getCurrentUser();
        Query query = db.collection("Clients")
                .document(currentUser.getUid())
                .collection("Favorites");

        FirestoreRecyclerOptions<Client.Favorite> options =
                new FirestoreRecyclerOptions.Builder<Client.Favorite>()
                .setQuery(query, Client.Favorite.class)
                .build();
        favoriteAdapter = new MyFavoriteListAdapter(options);
        RecyclerView recyclerView = findViewById(R.id.myFavoriteList);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager
                (new LinearLayoutManager(
                        this, LinearLayoutManager.HORIZONTAL,false));
        recyclerView.setAdapter(favoriteAdapter);
    }


    protected void onStart() {
        super.onStart();
        appointmentAdapter.startListening();
        favoriteAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        appointmentAdapter.stopListening();
        favoriteAdapter.stopListening();
    }

    public void goToBusiness(View view) {
        String business = "IfEInm3cpkcfYe9JQXZgvXWKJ5B2";
        Bundle b = new Bundle();
        b.putString("business", business);
        Intent intent = new Intent(getBaseContext(), ClientBusinessHomepage.class);
        intent.putExtras(b);
        startActivity(intent);
    }
}