package com.technion.cue.BusinessFeatures;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;
import com.technion.cue.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * top activity for viewing the schedule of the business
 */
public class BusinessSchedule extends FragmentActivity {

    private ViewPager pager;
    private PagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_schedule);

        pager = findViewById(R.id.business_schedule_pager);
        pagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        pager.setAdapter(pagerAdapter);

        TabLayout tabs = findViewById(R.id.business_schedule_tabs);
        tabs.setupWithViewPager(pager);
    }


    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {

        List<String> titleList = new ArrayList<>(
                Arrays.asList("Today", "This Week", "This Month", "Recent Changes")
        );

        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @Override
        public Fragment getItem(int position) {
            switch (titleList.get(position)) {
                case "Today":
                    Bundle b = new Bundle();
                    Calendar c = Calendar.getInstance();
                    b.putInt("year", c.get(Calendar.YEAR));
                    b.putInt("month", c.get(Calendar.MONTH));
                    b.putInt("day", c.get(Calendar.DAY_OF_MONTH));
                    Fragment bsd = new BusinessScheduleDay();
                    bsd.setArguments(b);
                    return bsd;
                case "This Week":
                    return new BusinessScheduleWeek();
                case "This Month":
                    return new BusinessScheduleMonth();
                case "Recent Changes":
                    return new BusinessScheduleFragment();
                default:
                    return new BusinessScheduleFragment();
            }
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titleList.get(position);
        }
    }
}
