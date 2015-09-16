package gr.qpc.meteoclimaandroid;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

import gr.qpc.meteoclimaandroid.adapters.MyArrayAdapter;

/**
 * Created by spyros on 8/18/15.
 */
public class MeteoclimaDailyFragment extends Fragment {

    private View rootView;
    private LinearLayout spinnerDaily;
    private Helper helper;
    private JSONArray retrievedForecasts;
    private ArrayList<HashMap<String, String>> list;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);

        rootView = inflater.inflate(R.layout.fragment_daily, container, false);

        spinnerDaily = (LinearLayout) rootView.findViewById(R.id.spinner_daily);

        helper = new Helper(getActivity());

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        getFragmentManager().executePendingTransactions();
        if (isVisible()) {
            populateList();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (isVisible()) {
            populateList();
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            populateList();
        }
    }

    private void populateList() {
        if (Helper.isGotForecasts()) {
            retrievedForecasts = helper.getRetrievedForecasts();

            list = new ArrayList<HashMap<String, String>>();

            // looping through all locations
            try {
                for (int i = 0; i < retrievedForecasts.length(); i++) {
                    JSONObject c = retrievedForecasts.getJSONObject(i);

                    if (c.has("error")) {
                        Log.d(Helper.LOG_TAG, c.getString("error"));
                    }

                    // Storing each json item in variable
                    String id = c.getString(Helper.TAG_ID);
                    String lat = c.getString(Helper.TAG_LAT);
                    String lon = c.getString(Helper.TAG_LON);
                    String yy = c.getString(Helper.TAG_YEAR);
                    String mm = c.getString(Helper.TAG_MONTH);
                    String dd = c.getString(Helper.TAG_DAY);
                    String hh = c.getString(Helper.TAG_HOUR);
                    String temp = c.getString(Helper.TAG_TEMP);
                    String weatherImage = c.getString(Helper.TAG_WEATHER_IMAGE);
                    String mslp = c.getString(Helper.TAG_MSLP);
                    String rain = c.getString(Helper.TAG_RAIN);
                    String snow = c.getString(Helper.TAG_SNOW);
                    String windBeaufort = c.getString(Helper.TAG_WIND_BEAUFORT);
                    String winddir = c.getString(Helper.TAG_WINDDIR);
                    String windDirSym = c.getString(Helper.TAG_WINDDIR_SYM);
                    String relhum = c.getString(Helper.TAG_RELHUM);
                    String heatIndex = c.getString(Helper.TAG_HEAT_INDEX);

                    //parse date and convert it from UTC to local time
                    String dateStr = yy + " " + mm + " " + dd + " " + hh;
                    SimpleDateFormat readFormat = new SimpleDateFormat("yyyy MM dd HH");
                    readFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                    Date date = readFormat.parse(dateStr);

                    SimpleDateFormat printFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm", Locale.ENGLISH);
                    printFormat.setTimeZone(TimeZone.getDefault());
                    String formattedDate = printFormat.format(date);

                    //parse the current forecasts date from helper to compare it
                    Date currentForecastDate = readFormat.parse(helper.getCurrentForecastDateTime());

                    Calendar cal1 = Calendar.getInstance();
                    cal1.setTime(date); //forecast's in the loop cal

                    //calculate tomorrow's date
                    Calendar cal3 = Calendar.getInstance();
                    cal3.setTime(currentForecastDate); //current forecast's cal
                    cal3.add(Calendar.DATE, 1); //current forecast's cal plus one day

                    if (cal1.get(Calendar.DAY_OF_YEAR) >= cal3.get(Calendar.DAY_OF_YEAR)) {
                        //put everything in hashmaps and then in an ArrayList to populate the listView
                        HashMap<String,String> map = new HashMap<String,String>();
                        map.put("id", id);
                        map.put("formattedDate", formattedDate);
                        map.put(Helper.TAG_TEMP, temp);
                        map.put(Helper.TAG_WEATHER_IMAGE, weatherImage);
                        map.put(Helper.TAG_MSLP, mslp);
                        map.put(Helper.TAG_RAIN, rain);
                        map.put(Helper.TAG_SNOW, snow);
                        map.put(Helper.TAG_WIND_BEAUFORT, windBeaufort);
                        map.put(Helper.TAG_WINDDIR, winddir);
                        map.put(Helper.TAG_WINDDIR_SYM, windDirSym);
                        map.put(Helper.TAG_RELHUM, relhum);
                        map.put(Helper.TAG_HEAT_INDEX, heatIndex);
                        list.add(map);
                    }


                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            final MyArrayAdapter adapter = new MyArrayAdapter(getActivity(), list);
            spinnerDaily.setVisibility(View.GONE);
            ListView listView = (ListView) rootView.findViewById(R.id.listview);
            listView.setAdapter(adapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
              @Override
              public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                  Intent intent = new Intent(getActivity(),MeteoclimaDetailsActivity.class);
                  intent.putExtra("id",adapter.getId(position));
                  intent.putExtra("fragment","daily");
                  startActivity(intent);
              }
            });
        }
    }
}
