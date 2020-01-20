package com.technion.cue.BusinessFeatures;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.technion.cue.R;
import com.technion.cue.annotations.ModuleAuthor;

/**
 * This fragment displays a calendar, from which the business owner
 * can access all of his appointments
 */
@ModuleAuthor("Ophir Eyal")
public class BusinessScheduleMonth extends Fragment {

    private Context context;

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
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
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
            b.putBoolean("returnToTabs", false);
            Fragment bsd = new BusinessScheduleDay();
            bsd.setArguments(b);
            getActivity().findViewById(R.id.business_schedule_tabs).setVisibility(View.INVISIBLE);
            getActivity().findViewById(R.id.bottom_navigation).setVisibility(View.GONE);
            getActivity().findViewById(R.id.progress_bar).setVisibility(View.VISIBLE);
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .remove(this)
                    .replace(R.id.business_schedule, bsd)
                    .addToBackStack(null)
                    .commit();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().findViewById(R.id.business_schedule_tabs).setVisibility(View.VISIBLE);
        getActivity().findViewById(R.id.bottom_navigation).setVisibility(View.VISIBLE);
    }
}

