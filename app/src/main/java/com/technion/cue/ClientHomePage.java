package com.technion.cue;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;


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
        setUpRecycleAppointmentAView();
//        setUpRecyclerFavoriteView();
    }

    private void setUpRecycleAppointmentAView() {

        FirebaseUser currentUser = mAuth.getCurrentUser();
        Query query = db.collection("Clients/"+currentUser.getUid()+"/appointments");

        FirestoreRecyclerOptions<CAppointmentListItem> options = new FirestoreRecyclerOptions.Builder<CAppointmentListItem>()
                .setQuery(query, CAppointmentListItem.class)
                .build();
        MyAppointmentListAdapter adapter = new MyAppointmentListAdapter(options);
        RecyclerView recyclerView = findViewById(R.id.myAppointmentList);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }


//    private void setUpRecyclerFavoriteView() {
//
//        FirebaseUser currentUser = mAuth.getCurrentUser();
//        Query query = db.collection(currentUser.getEmail()).orderBy("content_item");
//
//        FirestoreRecyclerOptions<MyFavoriteListAdapter> options = new FirestoreRecyclerOptions.Builder<MyFavoriteListAdapter>()
//                .setQuery(query, MyFavoriteListAdapter.class)
//                .build();
//        MyFavoriteListAdapter adapter = new MyFavoriteListAdapter(options);
//        RecyclerView recyclerView = findViewById(R.id.myFavoriteList);
//        recyclerView.setHasFixedSize(true);
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//        recyclerView.setAdapter(adapter);
//    }
}
