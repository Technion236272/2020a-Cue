package com.technion.cue.ClientFeatures;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.technion.cue.R;
import com.technion.cue.data_classes.Appointment;
import com.technion.cue.data_classes.Client;

import static com.technion.cue.FirebaseCollections.APPOINTMENTS_COLLECTION;
/**
 * ClientHomeFragment - contains 2 recycled views .
 * First, Favorite - horizontal list
 * Sec, Appointments - vertical list
 *
 * */

public class ClientHomeFragment extends Fragment {

    private MyAppointmentListAdapter appointmentAdapter;
    private MyFavoriteListAdapter favoriteAdapter;

    Query queryAppointment;
    Query queryFavorite;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;


    public ClientHomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
         View view = inflater.inflate(R.layout.fragment_client_home, container, false);


        return view;
    }
    /**
     * onViewCreated - set  2 recycled views adapters.
     * First, Favorite - horizontal list
     * Sec, Appointments - vertical list
     *
     * */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        setUpRecycleAppointmentAView();
        setUpRecyclerFavoriteView();
    }



    /**
     * setUpRecycleAppointmentAView - set  Appointments recycled views adapter.
     * and limit it to 5 in main homepage fragment
     * if not appointment it will show "No Appointments yet"
     * */
    private void setUpRecycleAppointmentAView() {
        queryAppointment =FirebaseFirestore.getInstance().collection(APPOINTMENTS_COLLECTION)
                .whereEqualTo("client_id", FirebaseAuth.getInstance().getUid())
                .whereGreaterThanOrEqualTo("date", Timestamp.now())
                .orderBy("date", Query.Direction.ASCENDING)
                .limit(5);
        FirestoreRecyclerOptions<Appointment> options =
                new FirestoreRecyclerOptions.Builder<Appointment>()
                        .setQuery(queryAppointment, Appointment.class)
                        .build();
        appointmentAdapter = new MyAppointmentListAdapter((ViewGroup)getView(),getContext(),options);

        RecyclerView recyclerView = getActivity().findViewById(R.id.myAppointmentList);

        recyclerView.setAdapter(appointmentAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

    }

    /**
     * setUpRecyclerFavoriteView - set  Favorites recycled views adapter.
     *
     * */
    private void setUpRecyclerFavoriteView() {

        FirebaseUser currentUser = mAuth.getCurrentUser();
        queryFavorite = db.collection("Clients")
                .document(currentUser.getUid())
                .collection("Favorites");

        FirestoreRecyclerOptions<Client.Favorite> options =
                new FirestoreRecyclerOptions.Builder<Client.Favorite>()
                .setQuery(queryFavorite, Client.Favorite.class)
                .build();
        favoriteAdapter = new MyFavoriteListAdapter(options,(ViewGroup)getView());

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
