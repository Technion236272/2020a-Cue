package com.technion.cue.BusinessFeatures;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.technion.cue.R;

public class BusinessScheduleMonth extends Fragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_business_schedule_month,
                container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        CalendarView c = view.findViewById(R.id.month_business_schedule);
        c.setOnDateChangeListener((c_view, year, month, day) -> {
            Bundle b = new Bundle();
            b.putInt("year", year);
            b.putInt("month", month);
            b.putInt("day", day);
            Fragment bsd = new BusinessScheduleDay();
            bsd.setArguments(b);
//            Intent intent = new Intent(getActivity().getBaseContext(),
//                    BusinessSchedule.class);
//            intent.putExtras(b);
//            getActivity().startActivity(intent);
//            getActivity().getSupportFragmentManager()
//                    .beginTransaction()
//                    .replace(R.id.business_schedule, bsd)
//                    .addToBackStack(null)
//                    .commit();
        });
    }
}

