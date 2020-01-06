package com.technion.cue.BusinessFeatures;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.technion.cue.R;
import com.technion.cue.annotations.ModuleAuthor;

@ModuleAuthor("Topaz")
public class BusinessSignUp2 extends Fragment {

    private static final int GET_LOGO = 0;
    private final BusinessSignUpContainer businessSignUpContainer;
    private View view;

    BusinessSignUp2(BusinessSignUpContainer businessSignUpContainer) {
        this.businessSignUpContainer = businessSignUpContainer;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.second_business_sign_up_fragment,
                container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        businessSignUpContainer.name = view.findViewById(R.id.businessNameEditText);
        businessSignUpContainer.description = view.findViewById(R.id.businessDescriptionEditText);
        businessSignUpContainer.phone = view.findViewById(R.id.businessPhoneEditText);
        businessSignUpContainer.state = view.findViewById(R.id.businessStateEditText);
        businessSignUpContainer.city = view.findViewById(R.id.businessCityEditText);
        businessSignUpContainer.address = view.findViewById(R.id.businessAddressEditText);

        view.findViewById(R.id.businessLogoEdit).setOnClickListener(cl -> {
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("image/*");
            if (photoPickerIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                startActivityForResult(
                        Intent.createChooser(photoPickerIntent, "Select File"),
                        GET_LOGO
                );
            }
        });
    }

    /**
     * Set the displayed logo to the one chosen from the image gallery
     * @param reqCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        if (reqCode == GET_LOGO && data != null) {
            businessSignUpContainer.logoData = data.getData();
            CircularImageView logo = view.findViewById(R.id.businessLogoEdit);
            Glide.with(logo.getContext())
                    .load(businessSignUpContainer.logoData)
                    .error(R.drawable.person_icon)
                    .into(logo);
        }
    }


}