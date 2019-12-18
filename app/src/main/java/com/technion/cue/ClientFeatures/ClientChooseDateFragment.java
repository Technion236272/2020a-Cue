package com.technion.cue.ClientFeatures;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.DatePicker;

import androidx.fragment.app.Fragment;

import com.technion.cue.R;

public class ClientChooseDateFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.client_choose_date_fragment, container, false);
        getActivity().findViewById(R.id.switch_to_date_time_fragments).setVisibility(View.INVISIBLE);
        Bundle b = getArguments();
        ((CalendarView)rootView.findViewById(R.id.date_picker))
                .setOnDateChangeListener( (view, year, month, day) -> {
                    Fragment f = new ClientChooseTimeFragment();
                    b.putInt("year", year);
                    b.putInt("month", month);
                    b.putInt("day", day);
                    f.setArguments(b);
                    getActivity()
                            .getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_holder_business_client, f)
                            .commit();
        });
        return rootView;
    }
}
