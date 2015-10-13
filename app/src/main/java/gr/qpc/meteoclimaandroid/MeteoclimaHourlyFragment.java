package gr.qpc.meteoclimaandroid;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
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
public class MeteoclimaHourlyFragment extends Fragment {

    private View rootView;
    private LinearLayout spinnerHourly;
    private Button chartButton;
    private Helper helper;
    private JSONArray retrievedForecasts;
    private ArrayList<HashMap<String, String>> list;
    private ArrayList<String> chartHourPoints;
    private ArrayList<String> chartTempPoints;
    private ArrayList<String> chartWindPoints;
    private Boolean chartCompleted;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        rootView = inflater.inflate(R.layout.fragment_hourly, container,
                false);

        spinnerHourly = (LinearLayout) rootView.findViewById(R.id.spinner_hourly);

        helper = new Helper(getActivity());

        chartHourPoints = new ArrayList<String>();
        chartTempPoints = new ArrayList<String>();
        chartWindPoints = new ArrayList<String>();
        chartCompleted = false;

        chartButton = (Button) rootView.findViewById(R.id.chart_button_hourly);
        chartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), MeteoclimaChartActivity.class);
                i.putStringArrayListExtra("hours", chartHourPoints);
                i.putStringArrayListExtra("temp_points", chartTempPoints);
                i.putStringArrayListExtra("wind_points", chartWindPoints);
                startActivity(i);
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isAdded()) {
            if (isVisible()) {
                populateList();
            }
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

    private void addDataToChart(String time, String temp, String wind) {
        if (!chartCompleted) {
            chartHourPoints.add(time);
            chartTempPoints.add(temp);
            chartWindPoints.add(wind);
        }
    }

    private void populateList() {
        if (Helper.isGotForecasts()) {
            retrievedForecasts = helper.getRetrievedForecasts();

            list = new ArrayList<HashMap<String, String>>();

            int forecastCounter = 0; //counter to get forecasts for 24 hours (8 forecasts)

            // looping through all forecasts
            try {
                for (int i = 0; i < retrievedForecasts.length(); i++) {
                    JSONObject jlist = retrievedForecasts.getJSONObject(i);

                    // Storing each json item in variable
                    String id = jlist.getString(Helper.TAG_ID);
                    String date_hour = jlist.getString(Helper.TAG_DATE_HOUR);

                    //split date for backwards compatibility
                    String[] date_parts = date_hour.split("-");
                    String yy = date_parts[0];
                    String mm = date_parts[1];
                    String[] date_parts2 = date_parts[2].split(" ");
                    String dd =  date_parts2[0];
                    String[] date_parts3 = date_parts2[1].split(":");
                    String hh = date_parts3[0];

                    //get main inside list
                    JSONObject main = jlist.getJSONObject(Helper.TAG_MAIN);
                    String temp = main.getString(Helper.TAG_TEMP);
                    String mslp = main.getString(Helper.TAG_MSLP);
                    String rain = "0.0";
                    if (jlist.has(Helper.TAG_RAIN)) {
                        JSONObject rainJson = jlist.getJSONObject(Helper.TAG_RAIN);
                        if (rainJson.has(Helper.TAG_RAIN_3H)) {
                            rain = rainJson.getString(Helper.TAG_RAIN_3H);
                        }
                    }
                    String snow = "0.0";
                    if (jlist.has(Helper.TAG_SNOW)) {
                        JSONObject snowJson = jlist.getJSONObject(Helper.TAG_SNOW);
                        if (snowJson.has(Helper.TAG_SNOW_3H)) {
                            snow = snowJson.getString(Helper.TAG_SNOW_3H);
                        }
                    }
                    String relhum = main.getString(Helper.TAG_RELHUM);

                    //get wind main inside list
                    JSONObject wind = jlist.getJSONObject(Helper.TAG_WIND);
                    String windsp = wind.getString(Helper.TAG_WINDSP);
                    String winddir = wind.getString(Helper.TAG_WINDDIR);

                    JSONArray weather_jarray = jlist.getJSONArray(Helper.TAG_WEATHER);

                    JSONObject weather = weather_jarray.getJSONObject(0);
                    String weatherImage = weather.getString(Helper.TAG_WEATHER_IMAGE);
                    String weatherDescription = weather.getString(Helper.TAG_WEATHER_DESCRIPTION);

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
                    Calendar cal2 = Calendar.getInstance();
                    cal1.setTime(date); //forecast's in the loop cal
                    cal2.setTime(currentForecastDate); //current forecast's cal

                    boolean hourNext = cal1.getTimeInMillis() > cal2.getTimeInMillis();

                    boolean sameDay = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);

                    //calculate tomorrow's date
                    Calendar cal3 = Calendar.getInstance();
                    cal3.setTime(currentForecastDate);
                    cal3.add(Calendar.DATE, 1);

                    boolean nextDay = cal1.get(Calendar.DAY_OF_YEAR) == cal3.get(Calendar.DAY_OF_YEAR);

                    //keep only 8 forecasts (24 hours) starting from the current forecast
                    if (forecastCounter < 8) {
                        if ((sameDay && hourNext) || nextDay) {
                            //put everything in hashmaps and then in an ArrayList to populate the listView
                            HashMap<String, String> map = new HashMap<String, String>();
                            map.put("id", id);
                            map.put("formattedDate", formattedDate);
                            map.put(Helper.TAG_TEMP, temp);
                            map.put(Helper.TAG_WEATHER_DESCRIPTION,weatherDescription);
                            map.put(Helper.TAG_WEATHER_IMAGE, weatherImage);
                            map.put(Helper.TAG_MSLP, mslp);
                            map.put(Helper.TAG_RAIN, rain);
                            map.put(Helper.TAG_SNOW, snow);
                            map.put(Helper.TAG_WINDSP, windsp);
                            map.put(Helper.TAG_WINDDIR, winddir);
                            map.put(Helper.TAG_RELHUM, relhum);
                            list.add(map);
                            //increment forecastCounter
                            forecastCounter++;
                            //add point to chart
                            addDataToChart(printFormat.format(date),temp, windsp);
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            chartCompleted = true;
            final MyArrayAdapter adapter = new MyArrayAdapter(getActivity(), list);
            spinnerHourly.setVisibility(View.GONE);
            if (rootView == null) {
                rootView = getView();
            }
            ListView listView = (ListView) rootView.findViewById(R.id.listview_hourly);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Intent intent = new Intent(getActivity(), MeteoclimaDetailsActivity.class);
                            intent.putExtra("id", adapter.getId(position));
                            intent.putExtra("fragment", "hourly");
                            startActivity(intent);
                        }
                    });
        }
    }
}
