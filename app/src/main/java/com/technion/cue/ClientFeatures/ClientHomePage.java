package com.technion.cue.ClientFeatures;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

<<<<<<< HEAD
import android.content.res.ColorStateList;
import android.graphics.Color;
=======
import android.content.Intent;
>>>>>>> 09a1c1c91242141adb5a5a269f7860c8a48b1356
import android.os.Bundle;
import android.view.View;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.technion.cue.R;
import com.technion.cue.data_classes.Appointment;
import com.technion.cue.data_classes.Client;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.content.Intent;
import android.widget.Toast;

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

    //     Menu listener
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_recents:
                        Toast.makeText(ClientHomePage.this, "Recents", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.action_favorites:
                        Toast.makeText(ClientHomePage.this, "Favorites", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.action_nearby:
                        Toast.makeText(ClientHomePage.this, "Nearby", Toast.LENGTH_SHORT).show();
                        break;
                }
                return true;
            }
        });
        // --
        BottomNavigationItemView currentButton = findViewById(R.id.action_recents);
        currentButton.setClickable(false);
        currentButton.setItemBackground(R.color.ColorSecondaryDark);



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
        favoriteAdapter.startListening();
        appointmentAdapter.startListening();

    }

    @Override
    protected void onStop() {
        super.onStop();
        appointmentAdapter.stopListening();
        favoriteAdapter.stopListening();
    }


    public void moveToBOPage(View view) {
        Intent getIntentBOPage = new Intent(this, ClientBusinessHomepage.class);
        getIntentBOPage.putExtra("business_id",(String)view.findViewById(R.id.businessName).getTag());
        startActivity(getIntentBOPage);
    }

    public void appoitmentEdit(View view) {
        Intent getIntentBOPage = new Intent(this, ClientAppointmentPage.class);
        // TODO: CHECK IF getTag is null
        getIntentBOPage.putExtra("appointment_id",(String)view.findViewById(R.id.business).getTag());
        startActivity(getIntentBOPage);
    }

    // - menu - ben 17.12
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.client_main_menu, menu);
        return true;
    }
//=======
//    public void goToBusiness(View view) {
//        String business = "IfEInm3cpkcfYe9JQXZgvXWKJ5B2";
//        Bundle b = new Bundle();
//        b.putString("business", business);
//        Intent intent = new Intent(getBaseContext(), ClientBusinessHomepage.class);
//        intent.putExtras(b);
//        startActivity(intent);
//>>>>>>> 09a1c1c91242141adb5a5a269f7860c8a48b1356
//    }
}