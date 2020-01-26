package com.technion.cue.BusinessFeatures;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.technion.cue.R;


/**
 * this is a dummy fragment class for all business schedule views that have not been implemented as of yet.
 * once all of them have been implemented, this fragment class, as well as it's accompanying layout file will be terminated
 */
public class BusinessScheduleFragmentPlaceholder extends Fragment {


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            ViewGroup rootView = (ViewGroup) inflater.inflate(
                    R.layout.business_schedule_fragment, container, false);

            return rootView;
        }
}
