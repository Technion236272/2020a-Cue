package com.technion.cue.BusinessFeatures;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.technion.cue.R;
import com.technion.cue.annotations.ModuleAuthor;

@ModuleAuthor("Topaz & Ophir")
public class BusinessSignUp1 extends Fragment {

    private final BusinessSignUpContainer businessSignUpContainer;

    BusinessSignUp1(BusinessSignUpContainer businessSignUpContainer) {
        this.businessSignUpContainer = businessSignUpContainer;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.first_business_sign_up_fragment,
                container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        businessSignUpContainer.email = view.findViewById(R.id.businessEmailEditText);
        businessSignUpContainer.password = view.findViewById(R.id.businessPasswordEditText);
        businessSignUpContainer.bo_name = view.findViewById(R.id.businessFullNameEditText);

        for (View vw : new View[]
                { businessSignUpContainer.email, businessSignUpContainer.password, businessSignUpContainer.bo_name })
            vw.setOnFocusChangeListener((v, isFocused) -> {
                if (!isFocused) businessSignUpContainer.hideKeyboard(v);
            });
    }

}
