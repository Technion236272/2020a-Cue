package com.technion.cue.ClientFeatures;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ActionBarContextView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.technion.cue.R;
import com.technion.cue.data_classes.Business;
import java.util.Date;


import static com.technion.cue.FirebaseCollections.BUSINESSES_COLLECTION;
/**
 * ClientBusinessList - Fragemnt which show
 * list of businesses list
 *
 * */
public class ClientBusinessList   extends Fragment {

    private Date currentDay;
    private Date nextDay;
    private RecyclerView business_list;
    private BusinessesListAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {






        return inflater.inflate(R.layout.client_businesses_fragment,
                container, false);



    }




    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        business_list = getActivity().findViewById(R.id.business_list);

        layoutManager = new LinearLayoutManager(getContext());
        business_list.setLayoutManager(layoutManager);


        // actionbar





        // a query to get all the appointments which occur today, for the current business
        Query query = FirebaseFirestore.getInstance()
                .collection(BUSINESSES_COLLECTION);

        FirestoreRecyclerOptions<Business> options =
                new FirestoreRecyclerOptions.Builder<Business>()
                        .setQuery(query, Business.class)
                        .build();

        ((AppCompatActivity)getActivity()).getSupportActionBar().setCustomView(null);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Businesses List");

        mAdapter = new BusinessesListAdapter(options,(ViewGroup)view);
        business_list.setAdapter(mAdapter);

    }



    @Override
    public void onStart() {
        super.onStart();
        mAdapter.startListening();

    }

    @Override
    public void onStop() {
        super.onStop();
        mAdapter.stopListening();
    }





}



