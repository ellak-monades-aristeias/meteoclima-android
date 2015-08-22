package gr.qpc.meteoclimaandroid;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

        rootView = inflater.inflate(R.layout.fragment_daily, container,
                false);

        spinnerDaily = (LinearLayout) rootView.findViewById(R.id.spinner_daily);

        helper = new Helper(getActivity());

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
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
                        System.out.println(c.getString("error"));
                    }

                    // Storing each json item in variable
                    String id = c.getString(MeteoclimaMainFragment.TAG_ID);
                    String yy = c.getString(MeteoclimaMainFragment.TAG_YEAR);
                    String mm = c.getString(MeteoclimaMainFragment.TAG_MONTH);
                    String dd = c.getString(MeteoclimaMainFragment.TAG_DAY);
                    String hh = c.getString(MeteoclimaMainFragment.TAG_HOUR);
                    String temp = c.getString(MeteoclimaMainFragment.TAG_TEMP);
                    String weatherImage = c.getString(MeteoclimaMainFragment.TAG_WEATHER_IMAGE);

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

                    if (date.compareTo(currentForecastDate) > 0) {
                        //put everything in hashmaps and then in an ArrayList to populate the listView
                        HashMap<String,String> map = new HashMap<String,String>();
                        map.put("1", formattedDate);
                        map.put("2", temp);
                        map.put("3", weatherImage);
                        list.add(map);
                    }


                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            MyArrayAdapter adapter = new MyArrayAdapter(getActivity(), list);
            spinnerDaily.setVisibility(View.GONE);
            ListView listView = (ListView) rootView.findViewById(R.id.listview);
            listView.setAdapter(adapter);
        }
    }
}
