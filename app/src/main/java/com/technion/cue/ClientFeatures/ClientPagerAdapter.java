package com.technion.cue.ClientFeatures;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
/**
 * ClientPagerAdapter - Adapter for tabs
 * which make all the magic - client can
 * move from one framgnet to another easliy
 *
 * */
public class ClientPagerAdapter extends FragmentStatePagerAdapter {


    int numOfTabs;

    public ClientPagerAdapter(FragmentManager fm , int numOfTabs) {
        super(fm);
        this.numOfTabs = numOfTabs;

    }

    /**
     * getItem - check which tab was clicked.
     *
     * */
    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                ClientHomeFragment tab1 = new ClientHomeFragment();
                return tab1;
            case 1:
                ClientCalendarFragment tab2 = new ClientCalendarFragment();
                return tab2;
                default:
                    return null;
        }

    }

    @Override
    public int getCount() {
        return numOfTabs;
    }
}
