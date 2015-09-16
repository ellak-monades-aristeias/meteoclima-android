package gr.qpc.meteoclimaandroid;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
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
                    JSONObject c = retrievedForecasts.getJSONObject(i);

                    if (c.has("error")) {
                        System.out.println(c.getString("error"));
                    }

                    if (c.getString(Helper.TAG_ID).equals(selected_id)) {

                        // Storing each json item in variable
                        String id = c.getString(Helper.TAG_ID);
                        String yy = c.getString(Helper.TAG_YEAR);
                        String mm = c.getString(Helper.TAG_MONTH);
                        String dd = c.getString(Helper.TAG_DAY);
                        String hh = c.getString(Helper.TAG_HOUR);
                        String lat = c.getString(Helper.TAG_LAT);
                        String lon = c.getString(Helper.TAG_LON);
                        String mslp = c.getString(Helper.TAG_MSLP);
                        String temp = c.getString(Helper.TAG_TEMP);
                        String rain = c.getString(Helper.TAG_RAIN);
                        String snow = c.getString(Helper.TAG_SNOW);
                        String windsp = c.getString(Helper.TAG_WINDSP);
                        String winddir = c.getString(Helper.TAG_WINDDIR);
                        String windDirSym = c.getString(Helper.TAG_WINDDIR_SYM);
                        String relhum = c.getString(Helper.TAG_RELHUM);
                        String weatherImage = c.getString(Helper.TAG_WEATHER_IMAGE);
                        String windBeaufort = c.getString(Helper.TAG_WIND_BEAUFORT);
                        String distance = c.getString(Helper.TAG_DISTANCE);
                        String heatIndex = c.getString(Helper.TAG_HEAT_INDEX);

                        // adding each child node to HashMap key => value
                        map.put(Helper.TAG_ID, id);
                        map.put(Helper.TAG_YEAR, yy);
                        map.put(Helper.TAG_MONTH, mm);
                        map.put(Helper.TAG_DAY, dd);
                        map.put(Helper.TAG_HOUR, hh);
                        map.put(Helper.TAG_LAT, lat);
                        map.put(Helper.TAG_LON, lon);
                        map.put(Helper.TAG_MSLP, mslp);
                        map.put(Helper.TAG_TEMP, temp);
                        map.put(Helper.TAG_RAIN, rain);
                        map.put(Helper.TAG_SNOW, snow);
                        map.put(Helper.TAG_WINDSP, windsp);
                        map.put(Helper.TAG_WINDDIR, winddir);
                        map.put(Helper.TAG_WINDDIR_SYM, windDirSym);
                        map.put(Helper.TAG_RELHUM, relhum);
                        map.put(Helper.TAG_WEATHER_IMAGE, weatherImage);
                        map.put(Helper.TAG_WIND_BEAUFORT, windBeaufort);
                        map.put(Helper.TAG_DISTANCE, distance);
                        map.put(Helper.TAG_HEAT_INDEX, heatIndex);

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
        Drawable drawable = res.getDrawable(helper.returnDrawableId(Integer.parseInt(map.get(Helper.TAG_WEATHER_IMAGE))));
        imageView.setImageDrawable(drawable);

        //update temperature
        TextView temperatureTextView = (TextView) findViewById(R.id.temperatureDetails);
        temperatureTextView.setText(helper.formatTemperature(map.get(Helper.TAG_TEMP)));
        temperatureTextView.setGravity(Gravity.CENTER_VERTICAL);

        TextView basicWeatherDescriptionTextView = (TextView) findViewById(R.id.basicWeatherDetails);
        basicWeatherDescriptionTextView.setText(helper.returnBasicWeatherDescription(Integer.parseInt(map.get(Helper.TAG_WEATHER_IMAGE))));
        basicWeatherDescriptionTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);

        String[] forecastDescriptions = helper.getForecastDescriptions();
        String[] tagNames = helper.getTagNames();
        GridView gridView = (GridView) findViewById(R.id.gridviewDetails);

        // create the grid item mapping
        String[] from = new String[] {"forecast_name", "value"};
        int[] to = new int[] { R.id.item1, R.id.item2};

        // prepare the list of all records
        List<HashMap<String, String>> fillMaps = new ArrayList<HashMap<String, String>>();

        //first add the wind separately
        HashMap<String, String> windToFill = new HashMap<String, String>();
        windToFill.put("forecast_name", "Wind speed/direction");
        windToFill.put("value", map.get(Helper.TAG_WIND_BEAUFORT) + " Bf / " + map.get(Helper.TAG_WINDDIR_SYM));
        fillMaps.add(windToFill);

        for(int i = 0; i < forecastDescriptions.length; i++){
            HashMap<String, String> mapToFill = new HashMap<String, String>();
            mapToFill.put("forecast_name", forecastDescriptions[i]);
            mapToFill.put("value", map.get(tagNames[i]));
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
