package gr.qpc.meteoclimaandroid.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import gr.qpc.meteoclimaandroid.MeteoclimaDailyFragment;
import gr.qpc.meteoclimaandroid.MeteoclimaHourlyFragment;
import gr.qpc.meteoclimaandroid.MeteoclimaMainFragment;

/**
 * Created by spyros on 8/18/15.
 */
public class TabsPagerAdapter extends FragmentPagerAdapter {

    public TabsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int index) {

        switch (index) {
            case 0:
                // Meteoclima main (now) fragment activity
                return new MeteoclimaMainFragment();
            case 1:
                // Meteoclima hourly fragment activity
                return new MeteoclimaHourlyFragment();
            case 2:
                // Meteoclima daily fragment activity
                return new MeteoclimaDailyFragment();
        }

        return null;
    }

    @Override
    public int getCount() {
        // get item count - equal to number of tabs
        return 3;
    }

}