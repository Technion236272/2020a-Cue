package com.technion.cue.ClientFeatures;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;




import android.content.Intent;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import com.google.firebase.firestore.Query;

import com.google.firebase.auth.FirebaseUser;
import com.technion.cue.R;
import com.technion.cue.data_classes.Appointment;
import com.technion.cue.data_classes.Client;

import android.view.Menu;
import android.view.MenuInflater;

import android.widget.TextView;

import static com.technion.cue.FirebaseCollections.APPOINTMENTS_COLLECTION;






import com.technion.cue.R;


public class ClientHomeFragment extends Fragment {

    private MyAppointmentListAdapter appointmentAdapter;
    private MyFavoriteListAdapter favoriteAdapter;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;


    public ClientHomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
         View view = inflater.inflate(R.layout.fragment_client_home, container, false);


        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpRecycleAppointmentAView();
        setUpRecyclerFavoriteView();
    }




    private void setUpRecycleAppointmentAView() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        Query query = db.collection(APPOINTMENTS_COLLECTION)
                .whereEqualTo("client_name", currentUser.getUid())
                .whereGreaterThanOrEqualTo("date", Timestamp.now())
                .orderBy("date", Query.Direction.ASCENDING)
                .limit(3);
        FirestoreRecyclerOptions<Appointment> options =
                new FirestoreRecyclerOptions.Builder<Appointment>()
                        .setQuery(query, Appointment.class)
                        .build();
        appointmentAdapter = new MyAppointmentListAdapter(options);

        RecyclerView recyclerView = getActivity().findViewById(R.id.myAppointmentList);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

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

        RecyclerView recyclerView = getActivity().findViewById(R.id.myFavoriteList);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager
                (new LinearLayoutManager(
                        getContext(), LinearLayoutManager.HORIZONTAL,false));

        recyclerView.setAdapter(favoriteAdapter);



    }


    @Override
    public void onStart() {
        super.onStart();
        appointmentAdapter.startListening();
        favoriteAdapter.startListening();
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpRecycleAppointmentAView();
        setUpRecyclerFavoriteView();
        appointmentAdapter.startListening();
        favoriteAdapter.startListening();
    }


    @Override
    public void onStop() {
        super.onStop();
        RecyclerView recyclerView = getActivity().findViewById(R.id.myAppointmentList);
        recyclerView.getRecycledViewPool().clear();
        appointmentAdapter.stopListening();
        favoriteAdapter.stopListening();
    }




}
