package com.technion.cue.BusinessFeatures;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.technion.cue.R;
import com.technion.cue.data_classes.Business;

import org.w3c.dom.Text;

import java.util.Calendar;
import java.util.Map;

import static com.technion.cue.FirebaseCollections.BUSINESSES_COLLECTION;

public class BusinessInfoFragment extends Fragment {

    BusinessLoader loader;

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
        loader = new BusinessLoader(view,
                FirebaseFirestore.getInstance(),
                business_to_fetch);
        loader.load();

        String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        Calendar c = Calendar.getInstance();
        String day = days[c.get(Calendar.DAY_OF_WEEK) - 1];
        switch (day) {
            case "Sunday":
                ((TextView) view.findViewById(R.id.sunday_text)).setTypeface(null, Typeface.BOLD);
                break;
            case "Monday":
                ((TextView) view.findViewById(R.id.monday_text)).setTypeface(null, Typeface.BOLD);
                break;
            case "Tuesday":
                ((TextView) view.findViewById(R.id.tuesday_text)).setTypeface(null, Typeface.BOLD);
                break;
            case "Wednesday":
                ((TextView) view.findViewById(R.id.wednesday_text)).setTypeface(null, Typeface.BOLD);
                break;
            case "Thursday":
                ((TextView) view.findViewById(R.id.thursday_text)).setTypeface(null, Typeface.BOLD);
                break;
            case "Friday":
                ((TextView) view.findViewById(R.id.friday_text)).setTypeface(null, Typeface.BOLD);
                break;
            case "Saturday":
                ((TextView) view.findViewById(R.id.saturday_text)).setTypeface(null, Typeface.BOLD);
                break;
            default:
                break;
        }

        TextView currentDayHours = view.findViewById(R.id.current_day_hours);
        LinearLayout openingHoursList = view.findViewById(R.id.open_hours_list);
        currentDayHours.setOnClickListener(cl -> {
            currentDayHours.setVisibility(View.GONE);
            openingHoursList.setVisibility(View.VISIBLE);
        });

        openingHoursList.setOnClickListener(cl -> {
            openingHoursList.setVisibility(View.GONE);
            currentDayHours.setVisibility(View.VISIBLE);
        });

        (view.findViewById(R.id.address)).setOnClickListener(cl -> {
            FirebaseFirestore.getInstance()
                    .collection(BUSINESSES_COLLECTION)
                    .document(FirebaseAuth.getInstance().getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        Map<String, String> location =
                                (Map<String, String>)documentSnapshot.get("location");
                        Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + location.get("address")
                                + ", " + location.get("city") + ", " + location.get("state"));
                        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                        mapIntent.setPackage("com.google.android.apps.maps");
                        startActivity(mapIntent);
                    });
        });
    }
}
