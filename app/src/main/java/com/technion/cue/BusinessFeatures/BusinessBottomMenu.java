package com.technion.cue.BusinessFeatures;

import android.view.MenuItem;

/**
 * Operation of bottom menu for business owners.
 */
interface BusinessBottomMenu {

    /**
     * opening the business schedule activity
     * @param item the bottom menu that is connecting the activities that are shown to the BO
     */
    void openBusinessSchedule(MenuItem item);

    /**
     * opening the business homepage activity
     * @param item the bottom menu that is connecting the activities that are shown to the BO
     */
    void openBusinessHomepage(MenuItem item);

    /**
     * opening the business clientele list activity
     * @param item the bottom menu that is connecting the activities that are shown to the BO
     */
    void openBusinessClientele(MenuItem item);

}
