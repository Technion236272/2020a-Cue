package com.technion.cue;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;



public class ClientHomePage extends AppCompatActivity {

    private MyAppointmentListAdapter MAadapter;
    private MyFavoriteListAdapter MFadapter;

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
        Query query = db.collection("Appointments").whereEqualTo("client", currentUser.getUid());
        FirestoreRecyclerOptions<CAppointmentListItem> options = new FirestoreRecyclerOptions.Builder<CAppointmentListItem>()
                .setQuery(query, CAppointmentListItem.class)
                .build();
        MAadapter = new MyAppointmentListAdapter(options);
        RecyclerView recyclerView = findViewById(R.id.myAppointmentList);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(MAadapter);
    }

    private void setUpRecyclerFavoriteView() {

        FirebaseUser currentUser = mAuth.getCurrentUser();
        Query query = db.collection("Favorites").whereEqualTo("client_id", currentUser.getUid());

        FirestoreRecyclerOptions<BFavoriteListItem> options = new FirestoreRecyclerOptions.Builder<BFavoriteListItem>()
                .setQuery(query, BFavoriteListItem.class)
                .build();
        MFadapter = new MyFavoriteListAdapter(options);
        RecyclerView recyclerView = findViewById(R.id.myFavoriteList);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));
        recyclerView.setAdapter(MFadapter);
    }


    protected void onStart() {
        super.onStart();
        MAadapter.startListening();
        MFadapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        MAadapter.stopListening();
        MFadapter.stopListening();
    }
}