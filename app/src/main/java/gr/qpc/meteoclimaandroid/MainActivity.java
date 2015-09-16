package gr.qpc.meteoclimaandroid;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import gr.qpc.meteoclimaandroid.adapters.TabsPagerAdapter;

/**
 * Created by spyros on 8/18/15.
 */

public class MainActivity extends ActionBarActivity implements
        ActionBar.TabListener {

    private MyViewPager viewPager;
    private TabsPagerAdapter mAdapter;
    private ActionBar actionBar;
    private Helper helper;

    // Tab titles
    private String[] tabs = { "Now", "24 Hours", "Next Days" };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initilization
        // block widget service from running simultaneously with the main app
        helper = new Helper(this);
        helper.setBlockWidgetService(true);

        //stop widget service if it is running
        if (helper.isWidgetServiceRunning(MeteoclimaWidgetService.class)) {
            stopService(new Intent(this,MeteoclimaWidgetService.class));
        }


        viewPager = (MyViewPager) findViewById(R.id.pager);
        actionBar = getSupportActionBar();
        mAdapter = new TabsPagerAdapter(getSupportFragmentManager());

        viewPager.setAdapter(mAdapter);
        actionBar.setHomeButtonEnabled(false);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.mipmap.ic_launcher);

        //hide tabs until location is ready and data from server is fetched
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

        // Adding TabsgetActionBar()
        for (String tab_name : tabs) {
            actionBar.addTab(actionBar.newTab().setText(tab_name)
                    .setTabListener(this));
        }

        //set the selectedTabIndex in case of screen orientation change
        if(savedInstanceState != null ) {
            showTabs();
            Log.d(Helper.LOG_TAG, "savedInstanceStatesavedInstance tab int: " + String.valueOf(savedInstanceState.getInt("selectedTabIndex")));
            if (0 < savedInstanceState.getInt("selectedTabIndex") && savedInstanceState.getInt("selectedTabIndex") < 3) {
                actionBar.setSelectedNavigationItem(savedInstanceState.getInt("selectedTabIndex"));
            }
        }

        /**
         * on swiping the viewpager make respective tab selected
         * */
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                // on changing the page
                // make respected tab selected
                actionBar.setSelectedNavigationItem(position);
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("selectedTabIndex", actionBar.getSelectedNavigationIndex());
    }

    @Override
    public void onTabReselected(Tab tab, FragmentTransaction ft) {
    }

    @Override
    public void onTabSelected(Tab tab, FragmentTransaction ft) {
        // on tab selected
        // show respected fragment view
        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_meteoclima_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent i = new Intent(this,MeteoclimaSettingsActivity.class);
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void showTabs() {
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        viewPager.setPagingEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        helper.setBlockWidgetService(true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        helper.setBlockWidgetService(false);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        helper.setBlockWidgetService(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        helper.setBlockWidgetService(false);
    }

    @Override
    protected void onPause() {
        super.onPause();
        helper.setBlockWidgetService(false);
    }
}
