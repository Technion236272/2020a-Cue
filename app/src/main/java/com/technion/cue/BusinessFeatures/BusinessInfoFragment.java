package com.technion.cue.BusinessFeatures;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.technion.cue.R;

public class BusinessInfoFragment extends Fragment {

    BusinessInfoLoader loader;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.business_info_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String business_to_fetch;
        if (getArguments() != null) {
            business_to_fetch = getArguments().getString("business_id");
        } else {
            business_to_fetch = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
        loader = new BusinessInfoLoader(view,
                FirebaseFirestore.getInstance(),
                business_to_fetch);
        loader.loadDataFromFB();
    }
}
