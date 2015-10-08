package gr.qpc.meteoclimaandroid;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import gr.qpc.meteoclimaandroid.adapters.MySimpleAdapter;

public class MeteoclimaDetailsActivity extends AppCompatActivity {

    private Helper helper;
    private String selected_id;
    private String fragment;
    private TextView locationTextView;
    private JSONArray retrievedForecasts;
    private ActionBar actionbar;
    private ArrayList<HashMap<String, String>> list;
    private ImageView imageView;
    private TextView dateTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);

        selected_id = getIntent().getStringExtra("id");
        fragment = getIntent().getStringExtra("fragment");
        setContentView(R.layout.activity_details);
        helper = new Helper(this);

        // creating new HashMap
        HashMap<String, String> map = new HashMap<String, String>();

        if (Helper.isGotForecasts()) {
            retrievedForecasts = helper.getRetrievedForecasts();

            list = new ArrayList<HashMap<String, String>>();

            // looping through all locations
            try {
                for (int i = 0; i < retrievedForecasts.length(); i++) {
                    JSONObject list = retrievedForecasts.getJSONObject(i);

                    if (list.has("error")) {
                        System.out.println(list.getString("error"));
                    }

                    if (list.getString(Helper.TAG_ID).equals(selected_id)) {

                        // Storing each json item in variable
                        String id = list.getString(Helper.TAG_ID);
                        String date_hour = list.getString(Helper.TAG_DATE_HOUR);

                        //split date for backwards compatibility
                        String[] date_parts = date_hour.split("-");
                        String yy = date_parts[0];
                        String mm = date_parts[1];
                        String[] date_parts2 = date_parts[2].split(" ");
                        String dd =  date_parts2[0];
                        String[] date_parts3 = date_parts2[1].split(":");
                        String hh = date_parts3[0];
                        Log.d(Helper.LOG_TAG, "yy: " + yy + " mm: " + mm + " dd: " + dd + " hh " + hh);

                        //get main inside list
                        JSONObject main = list.getJSONObject(Helper.TAG_MAIN);
                        String temp = main.getString(Helper.TAG_TEMP);

                        String mslp = main.getString(Helper.TAG_MSLP);
                        String rain = "0.0";
                        if (main.has(Helper.TAG_RAIN)) {
                            rain = main.getString(Helper.TAG_RAIN);
                        }
                        String snow = "0.0";
                        if (main.has(Helper.TAG_SNOW)) {
                            snow = main.getString(Helper.TAG_SNOW);
                        }
                        String relhum = main.getString(Helper.TAG_RELHUM);

                        //gat wind main inside list
                        JSONObject wind = list.getJSONObject(Helper.TAG_WIND);
                        String windsp = wind.getString(Helper.TAG_WINDSP);
                        String winddir = wind.getString(Helper.TAG_WINDDIR);

                        JSONArray weather_jarray = list.getJSONArray(Helper.TAG_WEATHER);

                        JSONObject weather = weather_jarray.getJSONObject(0);
                        String weatherImage = weather.getString(Helper.TAG_WEATHER_IMAGE);
                        String weatherDescription = weather.getString(Helper.TAG_WEATHER_DESCRIPTION);

                        // adding each child node to HashMap key => value
                        map.put(Helper.TAG_ID, id);
                        map.put(Helper.TAG_YEAR, yy);
                        map.put(Helper.TAG_MONTH, mm);
                        map.put(Helper.TAG_DAY, dd);
                        map.put(Helper.TAG_HOUR, hh);
                        map.put(Helper.TAG_MSLP, mslp);
                        map.put(Helper.TAG_TEMP, temp);
                        map.put(Helper.TAG_RAIN, rain);
                        map.put(Helper.TAG_SNOW, snow);
                        map.put(Helper.TAG_WINDSP, windsp);
                        map.put(Helper.TAG_WINDDIR, winddir);
                        map.put(Helper.TAG_RELHUM, relhum);
                        map.put(Helper.TAG_WEATHER_IMAGE, weatherImage);
                        map.put(Helper.TAG_WEATHER_DESCRIPTION, weatherDescription);

                        //parse date and convert it from UTC to local time
                        String dateStr = yy + " " + mm + " " + dd + " " + hh;
                        SimpleDateFormat readFormat = new SimpleDateFormat("yyyy MM dd HH");
                        readFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                        Date date = readFormat.parse(dateStr);

                        SimpleDateFormat printFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm", Locale.ENGLISH);
                        printFormat.setTimeZone(TimeZone.getDefault());
                        String formattedDate = printFormat.format(date);

                        dateTime = (TextView) findViewById(R.id.dateTimeDetails);
                        dateTime.setText(formattedDate);
                    }


                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }
        
        locationTextView = (TextView) findViewById(R.id.locationDetails);
        locationTextView.setText(helper.getLastKnownLocation());

        imageView = (ImageView) findViewById(R.id.imageViewDetails);
        Resources res = getResources();
        int resourceId = res.getIdentifier("open" + map.get(Helper.TAG_WEATHER_IMAGE), "drawable", this.getPackageName());
        Drawable drawable = res.getDrawable(resourceId);
        imageView.setImageDrawable(drawable);

        //update temperature
        TextView temperatureTextView = (TextView) findViewById(R.id.temperatureDetails);
        temperatureTextView.setText(helper.formatTemperature(map.get(Helper.TAG_TEMP)));
        temperatureTextView.setGravity(Gravity.CENTER_VERTICAL);

        TextView basicWeatherDescriptionTextView = (TextView) findViewById(R.id.basicWeatherDetails);
        basicWeatherDescriptionTextView.setText(helper.capitalize(map.get(Helper.TAG_WEATHER_DESCRIPTION)));
        basicWeatherDescriptionTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);

        String[] forecastDescriptions = helper.getForecastDescriptions();
        String[] tagNames = helper.getTagNames();
        String[] tagUnits = helper.getForecastUnits();
        GridView gridView = (GridView) findViewById(R.id.gridviewDetails);

        // create the grid item mapping
        String[] from = new String[] {"forecast_name", "value"};
        int[] to = new int[] { R.id.item1, R.id.item2};

        // prepare the list of all records
        List<HashMap<String, String>> fillMaps = new ArrayList<HashMap<String, String>>();

        //first add the wind separately
        HashMap<String, String> windSpeedToFill = new HashMap<String, String>();
        windSpeedToFill.put("forecast_name", "Wind speed");
        windSpeedToFill.put("value", map.get(Helper.TAG_WINDSP) + " " + Helper.UNIT_WIND_SPEED);
        fillMaps.add(windSpeedToFill);
        HashMap<String, String> windDirToFill = new HashMap<String, String>();
        windDirToFill.put("forecast_name", "Wind direction");
        windDirToFill.put("value", helper.windDegreesToDirection(map.get(Helper.TAG_WINDDIR)));
        fillMaps.add(windDirToFill);

        for(int i = 0; i < forecastDescriptions.length; i++){
            HashMap<String, String> mapToFill = new HashMap<String, String>();
            mapToFill.put("forecast_name", forecastDescriptions[i]);
            mapToFill.put("value", map.get(tagNames[i]) + " " + tagUnits[i]);
            fillMaps.add(mapToFill);
        }

        // fill in the grid_item layout
        MySimpleAdapter adapter = new MySimpleAdapter(this, fillMaps, R.layout.grid_item, from, to);
        gridView.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; go home
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
