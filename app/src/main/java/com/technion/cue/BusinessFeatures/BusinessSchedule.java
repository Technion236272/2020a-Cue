package com.technion.cue.BusinessFeatures;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.technion.cue.R;
import com.technion.cue.annotations.ModuleAuthor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * top activity for viewing the schedule of the business
 */
@ModuleAuthor("Ophir Eyal")
public class BusinessSchedule extends AppCompatActivity implements BusinessBottomMenu {

    private ViewPager pager;
    private PagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_schedule);

        pager = findViewById(R.id.business_schedule_pager);
        pagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        pager.setAdapter(pagerAdapter);

        // disable swiping when in the monthly schedule view (calendar view)
        pager.setOnTouchListener((v, event) -> {
            pager.setCurrentItem(pager.getCurrentItem());
            return pager.getCurrentItem() == 2;
        });

        TabLayout tabs = findViewById(R.id.business_schedule_tabs);
        tabs.setupWithViewPager(pager);

        BottomNavigationView bnv = findViewById(R.id.bottom_navigation);
        bnv.getMenu().getItem(0).setChecked(true);
    }

    public void openBusinessSchedule(MenuItem item) { }

    public void openBusinessHomepage(MenuItem item) {
        Intent intent = new Intent(getBaseContext(), BOBusinessHomePage.class);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }

    public void openBusinessClientele(MenuItem item) {
        Intent intent = new Intent(this, ClienteleList.class);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {

        List<String> titleList = new ArrayList<>(
                Arrays.asList("Today", "This Week", "ALL TIMES", "Recent Changes")
        );

        ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        /**
         * open appropriate fragment when a tab is clicked
         * @param position the position of the tab inside the Viewpager
         * @return the fragment that should be opened
         */
        @Override
        public Fragment getItem(int position) {
            switch (titleList.get(position)) {
                case "Today":
                    Bundle b;
                    if (getIntent().hasExtra("year")) {
                        b = getIntent().getExtras();
                        findViewById(R.id.business_schedule_tabs).setVisibility(View.GONE);
                    }
                    else {
                        b = new Bundle();
                        Calendar c = Calendar.getInstance();
                        b.putInt("year", c.get(Calendar.YEAR));
                        b.putInt("month", c.get(Calendar.MONTH));
                        b.putInt("day", c.get(Calendar.DAY_OF_MONTH));
                    }
                    Fragment bsd = new BusinessScheduleDay();
                    bsd.setArguments(b);
                    return bsd;
                case "This Week":
                    return new BusinessScheduleWeek();
                case "ALL TIMES":
                    return new BusinessScheduleMonth();
                case "Recent Changes":
                    return new BusinessScheduleRecentChanges();
                default:
                    return new BusinessScheduleFragmentPlaceholder();
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
