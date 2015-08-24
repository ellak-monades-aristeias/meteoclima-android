package gr.qpc.meteoclimaandroid;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
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

                    if (c.getString(MeteoclimaMainFragment.TAG_ID).equals(selected_id)) {

                        // Storing each json item in variable
                        String id = c.getString(MeteoclimaMainFragment.TAG_ID);
                        String yy = c.getString(MeteoclimaMainFragment.TAG_YEAR);
                        String mm = c.getString(MeteoclimaMainFragment.TAG_MONTH);
                        String dd = c.getString(MeteoclimaMainFragment.TAG_DAY);
                        String hh = c.getString(MeteoclimaMainFragment.TAG_HOUR);
                        String lat = c.getString(MeteoclimaMainFragment.TAG_LAT);
                        String lon = c.getString(MeteoclimaMainFragment.TAG_LON);
                        String mslp = c.getString(MeteoclimaMainFragment.TAG_MSLP);
                        String temp = c.getString(MeteoclimaMainFragment.TAG_TEMP);
                        String rain = c.getString(MeteoclimaMainFragment.TAG_RAIN);
                        String snow = c.getString(MeteoclimaMainFragment.TAG_SNOW);
                        String windsp = c.getString(MeteoclimaMainFragment.TAG_WINDSP);
                        String winddir = c.getString(MeteoclimaMainFragment.TAG_WINDDIR);
                        String relhum = c.getString(MeteoclimaMainFragment.TAG_RELHUM);
                        String lcloud = c.getString(MeteoclimaMainFragment.TAG_LCOUD);
                        String mcloud = c.getString(MeteoclimaMainFragment.TAG_MCLOUD);
                        String hcloud = c.getString(MeteoclimaMainFragment.TAG_HCLOUD);
                        String weatherImage = c.getString(MeteoclimaMainFragment.TAG_WEATHER_IMAGE);
                        String windWaveImage = c.getString(MeteoclimaMainFragment.TAG_WIND_WAVE_IMAGE);

                        // adding each child node to HashMap key => value
                        map.put(MeteoclimaMainFragment.TAG_ID, id);
                        map.put(MeteoclimaMainFragment.TAG_YEAR, yy);
                        map.put(MeteoclimaMainFragment.TAG_MONTH, mm);
                        map.put(MeteoclimaMainFragment.TAG_DAY, dd);
                        map.put(MeteoclimaMainFragment.TAG_HOUR, hh);
                        map.put(MeteoclimaMainFragment.TAG_LAT, lat);
                        map.put(MeteoclimaMainFragment.TAG_LON, lon);
                        map.put(MeteoclimaMainFragment.TAG_MSLP, mslp);
                        map.put(MeteoclimaMainFragment.TAG_TEMP, temp);
                        map.put(MeteoclimaMainFragment.TAG_RAIN, rain);
                        map.put(MeteoclimaMainFragment.TAG_SNOW, snow);
                        map.put(MeteoclimaMainFragment.TAG_WINDSP, windsp);
                        map.put(MeteoclimaMainFragment.TAG_WINDDIR, winddir);
                        map.put(MeteoclimaMainFragment.TAG_RELHUM, relhum);
                        map.put(MeteoclimaMainFragment.TAG_LCOUD, lcloud);
                        map.put(MeteoclimaMainFragment.TAG_MCLOUD, mcloud);
                        map.put(MeteoclimaMainFragment.TAG_HCLOUD, hcloud);
                        map.put(MeteoclimaMainFragment.TAG_WEATHER_IMAGE, weatherImage);
                        map.put(MeteoclimaMainFragment.TAG_WIND_WAVE_IMAGE, windWaveImage);

                        //parse date and convert it from UTC to local time
                        String dateStr = yy + " " + mm + " " + dd + " " + hh;
                        SimpleDateFormat readFormat = new SimpleDateFormat("yyyy MM dd HH");
                        readFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                        Date date = readFormat.parse(dateStr);
                        String printFormatTemplate = "";
                        if (fragment.equals("daily")) {
                            printFormatTemplate = "EEE, dd MMM yyyy HH:mm";
                        } else if (fragment.equals("hourly")) {
                            printFormatTemplate = "HH:mm";
                        }
                        SimpleDateFormat printFormat = new SimpleDateFormat(printFormatTemplate, Locale.ENGLISH);
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
        Drawable drawable = res.getDrawable(helper.returnDrawableId(Integer.parseInt(map.get(MeteoclimaMainFragment.TAG_WEATHER_IMAGE))));
        imageView.setImageDrawable(drawable);
        TextView basicWeatherDescriptionTextView = (TextView) findViewById(R.id.basicWeatherDetails);
        basicWeatherDescriptionTextView.setText(helper.returnBasicWeatherDescription(Integer.parseInt(map.get(MeteoclimaMainFragment.TAG_WEATHER_IMAGE))));
        basicWeatherDescriptionTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);

        String[] forecastDescriptions = helper.getForecastDescriptions();
        String[] tagNames = new String[]{"mslp","temp","rain","snow","windsp","winddir","relhum","lcloud","mcloud","hcloud","landOrSea"};
        GridView gridView = (GridView) findViewById(R.id.gridviewDetails);

        // create the grid item mapping
        String[] from = new String[] {"forecast_name", "value"};
        int[] to = new int[] { R.id.item1, R.id.item2};

        // prepare the list of all records
        List<HashMap<String, String>> fillMaps = new ArrayList<HashMap<String, String>>();
        for(int i = 0; i < 10; i++){
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
