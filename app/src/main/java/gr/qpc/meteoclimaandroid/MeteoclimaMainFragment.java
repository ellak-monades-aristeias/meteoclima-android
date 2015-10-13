package gr.qpc.meteoclimaandroid;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import gr.qpc.meteoclimaandroid.adapters.MySimpleAdapter;

public class MeteoclimaMainFragment extends Fragment implements
        ConnectionCallbacks,
        OnConnectionFailedListener,
        LocationListener {

    private View rootView;
    private LocationClient mLocationClient;
    private TextView locationTextView;
    private TextView dateTime;
    private TextView firstLine;
    private TextView secondLine;
    private TextView thirdLine;
    private TextView forthLine;
    private ImageView imageView;
    private ImageView nextImageView;
    private LinearLayout homeNext;
    private LinearLayout spinner;
    private Helper helper;
    private ArrayList<HashMap<String, String>> retrievedLocationsList;
    private HashMap<String,String> results;
    private HashMap<String, String> mapToFillHomeNext;
    private DateFormat sdf;
    private Map<Date,String> dates;

    // Creating JSON Parser object
    private JSONParser jParser = new JSONParser();

    JSONArray retrievedLocations = null;

    private static final LocationRequest REQUEST = LocationRequest.create()
            .setInterval(180000) // 3 minutes
            .setFastestInterval(30000) // 30 seconds
            .setSmallestDisplacement(100) // 100 meters
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    private Location mLastLocation;

    private static boolean getLocationNameIsRunning = false;
    private static boolean connectToServerIsRunning = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        helper = new Helper(getActivity());

        // Hashmap for retrieved locations
        // (I am passing the right map from inside this one to AsyncTask's onPostExecute and then to updateForecastOnUi).
        retrievedLocationsList = new ArrayList<HashMap<String, String>>();

        //build the SimpleDateFormat here to use it everywhere as it is
        sdf = new SimpleDateFormat("yyyy MM dd HH z");
        sdf.setTimeZone(new SimpleTimeZone(SimpleTimeZone.UTC_TIME, "UTC"));

        //create a HashMap of date and ids (String) to compare dates and find the nearest date
        dates = new HashMap<Date,String>();


        //fix strict mode network exception
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        //get the current location
        setUpLocationClientIfNeeded();
        if (!mLocationClient.isConnected()) {
            mLocationClient.connect();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_main, container, false);
        }


        //put na image to imageView
        imageView = (ImageView) rootView.findViewById(R.id.imageView);
        Resources res = getResources();
        Drawable drawable = res.getDrawable(R.drawable.na);
        imageView.setImageDrawable(drawable);

        //first get the loading spinner
        spinner = (LinearLayout) rootView.findViewById(R.id.spinner);

        locationTextView = (TextView) rootView.findViewById(R.id.location);
        locationTextView.setText(helper.getLastKnownLocation());

        //hide homeNext until it's filled in
        homeNext = (LinearLayout) rootView.findViewById(R.id.homeNext);
        homeNext.setVisibility(View.GONE);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        //if forecast is already downloaded use this one
        if (Helper.isGotForecasts()) {
            updateUiFromStoredForecasts();
        } else {
            setUpLocationClientIfNeeded();
            if (!mLocationClient.isConnected()) {
                mLocationClient.connect();
            }
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            //if forecast is already downloaded use this one
            if (Helper.isGotForecasts()) {
                updateUiFromStoredForecasts();
            } else {
                setUpLocationClientIfNeeded();
                if (!mLocationClient.isConnected()) {
                    mLocationClient.connect();
                } else {
                    updateUi();
                }
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mLocationClient != null) {
            mLocationClient.disconnect();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //delete retrieved forecasts to get the newest when the app run again
        helper.storeForecasts(new JSONArray());
        Helper.setGotForecasts(false);
        if (mLocationClient != null) {
            mLocationClient.disconnect();
        }
    }

    private void setUpLocationClientIfNeeded() {
        if (mLocationClient == null) {
            mLocationClient = new LocationClient(
                    getActivity().getApplicationContext(),
                    this,  // ConnectionCallbacks
                    this); // OnConnectionFailedListener
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        /*setUpLocationClientIfNeeded();
        if (!mLocationClient.isConnected()) {
            mLocationClient.connect();
        }*/
        updateUi();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        mLocationClient.requestLocationUpdates(
                REQUEST,
                this);  // LocationListener
        if (getFragmentManager() != null) { //run only if app is active
            getFragmentManager().executePendingTransactions();
        }//to make sure isAdded returns the correct value
        if (isAdded()) { //prevent running if the fragment is not still attached to the activity
            if (Helper.isGotForecasts()) {
                updateUiFromStoredForecasts();
            } else {
                updateUi();
            }
        }
    }

    @Override
    public void onDisconnected() {
        // Do nothing
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Do nothing
    }

    public void updateUi() {
        //first update the location name
        if (!getLocationNameIsRunning) {
            new GetLocationName(getActivity()).execute();
        }
        //then get the forecast from server
        if (!connectToServerIsRunning) {
            new ConnectToServer(getActivity()).execute();
        }
    }

    public void updateForecastOnUi(HashMap<String,String> map) {
        if (rootView != null && map.size() > 2) {

            //update image
            Resources res = getResources();
            int resourceId = res.getIdentifier("open" + map.get(Helper.TAG_WEATHER_IMAGE), "drawable", getActivity().getApplicationContext().getPackageName());
            Drawable drawable = res.getDrawable(resourceId);
            imageView.setImageDrawable(drawable);

            //update description
            TextView basicWeatherDescriptionTextView = (TextView) rootView.findViewById(R.id.basicWeather);
            basicWeatherDescriptionTextView.setText(helper.capitalize(map.get(Helper.TAG_WEATHER_DESCRIPTION)));
            basicWeatherDescriptionTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);

            //update temperature
            TextView temperatureTextView = (TextView) rootView.findViewById(R.id.temperature);
            temperatureTextView.setText(helper.formatTemperature(map.get(Helper.TAG_TEMP)));
            temperatureTextView.setGravity(Gravity.CENTER_VERTICAL);

            //update time
            DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy, HH:mm");
            String date = df.format(Calendar.getInstance().getTime());
            dateTime = (TextView) rootView.findViewById(R.id.dateTime);
            dateTime.setText(date);

            String[] forecastDescriptions = helper.getForecastDescriptions();
            String[] tagNames = helper.getTagNames();
            String[] tagUnits = helper.getForecastUnits();
            MyExpandableHeightGridView gridView = (MyExpandableHeightGridView) rootView.findViewById(R.id.gridview);
            gridView.setExpanded(true);

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
            MySimpleAdapter adapter = new MySimpleAdapter(getActivity(), fillMaps, R.layout.grid_item, from, to);
            gridView.setAdapter(adapter);

            // fill in the homeNext layout
            firstLine = (TextView) rootView.findViewById(R.id.homeNext_firstLine);
            secondLine = (TextView) rootView.findViewById(R.id.homeNext_secondLine);
            thirdLine = (TextView) rootView.findViewById(R.id.homeNext_thirdLine);
            forthLine = (TextView) rootView.findViewById(R.id.homeNext_forthLine);
            nextImageView = (ImageView) rootView.findViewById(R.id.homeNext_icon);

            nextImageView.setImageResource(getResources().getIdentifier("open" + mapToFillHomeNext.get(Helper.TAG_WEATHER_IMAGE), "drawable", getActivity().getApplicationContext().getPackageName()));
            firstLine.setText(mapToFillHomeNext.get("formattedDate"));
            secondLine.setText(helper.formatTemperature(mapToFillHomeNext.get(Helper.TAG_TEMP)) + " " + helper.capitalize(mapToFillHomeNext.get(Helper.TAG_WEATHER_DESCRIPTION)));
            thirdLine.setText(Html.fromHtml("<b>Wind:</b> " + mapToFillHomeNext.get(Helper.TAG_WINDSP) + " " + Helper.UNIT_WIND_SPEED + " " + helper.windDegreesToDirection(mapToFillHomeNext.get(Helper.TAG_WINDDIR)) + " <b>Pressure:</b> " + mapToFillHomeNext.get(Helper.TAG_MSLP) + " " + Helper.UNIT_MSLP));
            if (!mapToFillHomeNext.get(Helper.TAG_SNOW).equals("0.0")) {
                forthLine.setText(Html.fromHtml("<b>Snow:</b> " + mapToFillHomeNext.get(Helper.TAG_RAIN) + " " +  Helper.UNIT_SNOW  + " <b>Humidity:</b> " + mapToFillHomeNext.get(Helper.TAG_RELHUM) + " " + Helper.UNIT_RELHUM));
            } else if (!mapToFillHomeNext.get(Helper.TAG_RAIN).equals("0.0")) {
                forthLine.setText(Html.fromHtml("<b>Rain:</b> " + mapToFillHomeNext.get(Helper.TAG_RAIN) + " " + Helper.UNIT_RAIN + " <b>Humidity:</b> " + mapToFillHomeNext.get(Helper.TAG_RELHUM) + " " + Helper.UNIT_RELHUM));
            }  else {
                forthLine.setText(Html.fromHtml("<b>Humidity:</b> " + mapToFillHomeNext.get(Helper.TAG_RELHUM) + " " + Helper.UNIT_RELHUM));
            }
            homeNext.setVisibility(View.VISIBLE);

            //show current lan/lon for beta testers
            /*TextView debug = (TextView) rootView.findViewById(R.id.debug);
            debug.setText(Html.fromHtml("device lat: " + mLastLocation.getLatitude() + " lon: " + mLastLocation.getLongitude() +
                    "<br>server lat: " + map.get(Helper.TAG_LAT) + " lon: " + map.get(Helper.TAG_LON)));*/
        }
    }

    public void updateUiFromStoredForecasts() {

        Log.d(Helper.LOG_TAG,"updateUiFromStoredForecasts is running...");

        //first update the location name
        String locationName = helper.getLocationName(mLastLocation);
        if (locationName.equals(getString(R.string.waiting_for_location))) {
            if (!getLocationNameIsRunning) {
                new GetLocationName(getActivity()).execute();
            }
        } else {
            locationTextView.setText(locationName);
        }

        //and then update the forecast
        retrievedLocations = helper.getRetrievedForecasts();

        // looping through all locations
        try {
            for (int i = 0; i < retrievedLocations.length(); i++) {
                JSONObject list = retrievedLocations.getJSONObject(i);

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
                //Log.d(Helper.LOG_TAG,"yy: " + yy + " mm: " + mm + " dd: " + dd + " hh " + hh);

                //get main inside list
                JSONObject main = list.getJSONObject(Helper.TAG_MAIN);
                String temp = main.getString(Helper.TAG_TEMP);
                String mslp = main.getString(Helper.TAG_MSLP);
                String rain = "0.0";
                if (list.has(Helper.TAG_RAIN)) {
                    JSONObject rainJson = list.getJSONObject(Helper.TAG_RAIN);
                    if (rainJson.has(Helper.TAG_RAIN_3H)) {
                        rain = rainJson.getString(Helper.TAG_RAIN_3H);
                    }
                }
                String snow = "0.0";
                if (list.has(Helper.TAG_SNOW)) {
                    JSONObject snowJson = list.getJSONObject(Helper.TAG_SNOW);
                    if (snowJson.has(Helper.TAG_SNOW_3H)) {
                        snow = snowJson.getString(Helper.TAG_SNOW_3H);
                    }
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

                //parse date
                Calendar cal = GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC"));
                String target = yy + " " + mm + " " + dd + " " + hh + " UTC";
                //Date result = null;
                try {
                    cal.setTime(sdf.parse(target));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                //now add it to dates list to compare it when the loop is finished
                Date dat = cal.getTime();
                dates.put(dat, id);

                // creating new HashMap
                HashMap<String, String> map = new HashMap<String, String>();

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

                // adding HashList to ArrayList
                retrievedLocationsList.add(map);
            }

            //find the date closest to "now"
            Date now = new Date();
            //convert the Hashmap to List to find the closest date
            List<Date> datesToCompare = new ArrayList<Date>(dates.keySet());
            Date closest = helper.getNearestDate(datesToCompare, now);

            //loop retrievedLocationsList to send the closest location forecast to updateForecastInUi method
            for (int i = 0; i < retrievedLocationsList.size(); i++) {

                //parse current date to compare it to nearest date
                Calendar cal = GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC"));
                String target = retrievedLocationsList.get(i).get(Helper.TAG_YEAR) + " " +
                        retrievedLocationsList.get(i).get(Helper.TAG_MONTH) + " " +
                        retrievedLocationsList.get(i).get(Helper.TAG_DAY) + " " +
                        retrievedLocationsList.get(i).get(Helper.TAG_HOUR) + " UTC";
                try {
                    cal.setTime(sdf.parse(target));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Date currentDate = cal.getTime();


                if (currentDate.compareTo(closest) == 0) {
                    //store selected forecast's date to helper
                    helper.setCurrentForecastDateTime(
                            retrievedLocationsList.get(i).get(Helper.TAG_YEAR) + " " +
                                    retrievedLocationsList.get(i).get(Helper.TAG_MONTH) + " " +
                                    retrievedLocationsList.get(i).get(Helper.TAG_DAY) + " " +
                                    retrievedLocationsList.get(i).get(Helper.TAG_HOUR)
                    );
                    updateForecastOnUi(retrievedLocationsList.get(i));
                    spinner.setVisibility(View.GONE);
                    ((MainActivity)getActivity()).showTabs();
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void repeatGetLocationName() {
        Timer timer = new Timer();
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                try {
                    new GetLocationName(getActivity()).execute();
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                }
            }
        };
        timer.schedule(doAsynchronousTask,10000); //execute in 10 seconds
    }

    private class GetLocationName extends AsyncTask<String, Void, String> {

        Context ctx;

        public GetLocationName(Context ctx) {
            this.ctx = ctx;
        }

        protected String doInBackground(String... args) {

            getLocationNameIsRunning = true;

            setUpLocationClientIfNeeded();
            String locationName = getString(R.string.waiting_for_location);
            if (!mLocationClient.isConnected()) {
                mLocationClient.connect();
            } else if (mLocationClient.getLastLocation() == null) {
                locationName = helper.getLastKnownLocation();
                repeatGetLocationName(); //retry in 10 seconds
            } else {
                mLastLocation = mLocationClient.getLastLocation();
                locationName = helper.getLocationName(mLastLocation);
            }
            return locationName;
        }

        @Override
        protected void onPostExecute(String locationName) {
            if (getFragmentManager() != null) { //run only if app is active
                getFragmentManager().executePendingTransactions(); //to make sure isAdded returns the correct value
                if (isAdded()) { //prevent onPostExecute to run if the fragment is not still attached to the activity
                    if (!locationName.equals(getString(R.string.waiting_for_location))) {
                        locationTextView.setText(locationName);
                        getLocationNameIsRunning = false;
                    }
                }
            }
        }
    }

    private class ConnectToServer extends AsyncTask<Void, Void, HashMap<String,String>> {

        Context ctx;

        public ConnectToServer(Context ctx) {
            this.ctx = ctx;
        }

        protected HashMap<String,String> doInBackground(Void... args) {

            connectToServerIsRunning = true;

            Log.d(Helper.LOG_TAG, "Meteoclima connecting to server...");

            //check for internet connection
            ConnectionChecker cc = new ConnectionChecker(ctx);

            //Hashmap to pass to onPostExecute in case of errors.
            results = new HashMap<String,String>();

            if (cc.isConnectingToInternet()) {
                if (!mLocationClient.isConnected()) {
                    mLocationClient.connect();
                } else {
                    //get the current latitude and longtitude from our LocationClient
                    mLastLocation = mLocationClient.getLastLocation();
                    String latitude = String.valueOf(mLastLocation.getLatitude());
                    String longitude = String.valueOf(mLastLocation.getLongitude());

                    // Building Parameters
                    List<NameValuePair> params = new ArrayList<NameValuePair>();
                    params.add(new BasicNameValuePair("lat", latitude));
                    params.add(new BasicNameValuePair("lon", longitude));
                    params.add(new BasicNameValuePair("units", "metric"));
                    params.add(new BasicNameValuePair("APPID", Helper.API_KEY));

                    // getting JSON string from URL
                    JSONObject json = jParser.makeHttpRequest(Helper.url_server, "GET", params);

                    try {
                        // Checking if not empty
                        if (!json.isNull("list")) {
                            // locations found
                            // Getting Array of retrieved locations
                            retrievedLocations = json.getJSONArray(Helper.TAG_LOCATIONS);

                            //store retrievedLocations to helper class
                            helper.storeForecasts(retrievedLocations);
                            Helper.setGotForecasts(true);

                            // looping through all locations
                            for (int i = 0; i < retrievedLocations.length(); i++) {
                                JSONObject list = retrievedLocations.getJSONObject(i);

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
                                //Log.d(Helper.LOG_TAG,"yy: " + yy + " mm: " + mm + " dd: " + dd + " hh " + hh);

                                //get main inside list
                                JSONObject main = list.getJSONObject(Helper.TAG_MAIN);
                                String temp = main.getString(Helper.TAG_TEMP);
                                String mslp = main.getString(Helper.TAG_MSLP);
                                String rain = "0.0";
                                if (list.has(Helper.TAG_RAIN)) {
                                    JSONObject rainJson = list.getJSONObject(Helper.TAG_RAIN);
                                    if (rainJson.has(Helper.TAG_RAIN_3H)) {
                                        rain = rainJson.getString(Helper.TAG_RAIN_3H);
                                    }
                                }
                                String snow = "0.0";
                                if (list.has(Helper.TAG_SNOW)) {
                                    JSONObject snowJson = list.getJSONObject(Helper.TAG_SNOW);
                                    if (snowJson.has(Helper.TAG_SNOW_3H)) {
                                        snow = snowJson.getString(Helper.TAG_SNOW_3H);
                                    }
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

                                //Log.d(Helper.LOG_TAG, "description: " + weatherDescription + " icon: " + weatherImage);

                                //parse date
                                Calendar cal = GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC"));
                                String target = yy + " " + mm + " " + dd + " " + hh + " UTC";
                                //Date result = null;
                                try {
                                    cal.setTime(sdf.parse(target));
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }

                                //now add it to dates list to compare it when the loop is finished
                                Date dat = cal.getTime();
                                dates.put(dat,id);

                                // creating new HashMap
                                HashMap<String, String> map = new HashMap<String, String>();

                                // adding each child node to HashMap key => value
                                map.put(Helper.TAG_ID,id);
                                map.put(Helper.TAG_YEAR,yy);
                                map.put(Helper.TAG_MONTH,mm);
                                map.put(Helper.TAG_DAY,dd);
                                map.put(Helper.TAG_HOUR,hh);
                                map.put(Helper.TAG_MSLP,mslp);
                                map.put(Helper.TAG_TEMP,temp);
                                map.put(Helper.TAG_RAIN,rain);
                                map.put(Helper.TAG_SNOW,snow);
                                map.put(Helper.TAG_WINDSP,windsp);
                                map.put(Helper.TAG_WINDDIR,winddir);
                                map.put(Helper.TAG_RELHUM,relhum);
                                map.put(Helper.TAG_WEATHER_IMAGE,weatherImage);
                                map.put(Helper.TAG_WEATHER_DESCRIPTION, weatherDescription);

                                // adding HashList to ArrayList
                                retrievedLocationsList.add(map);
                            }

                            //find the closest date
                            Date now = new Date();
                            //convert the Hashmap to List to find the closest date
                            List<Date> datesToCompare = new ArrayList<Date>(dates.keySet());
                            Date closest = helper.getNearestDate(datesToCompare, now);

                            Boolean fillHomeNext = false;

                            //loop retrievedLocationsList to send the closest location forecast to updateForecastInUi method
                            for (int i = 0; i < retrievedLocationsList.size(); i++) {

                                String dateStr = retrievedLocationsList.get(i).get(Helper.TAG_YEAR) + " " +
                                        retrievedLocationsList.get(i).get(Helper.TAG_MONTH) + " " +
                                        retrievedLocationsList.get(i).get(Helper.TAG_DAY) + " " +
                                        retrievedLocationsList.get(i).get(Helper.TAG_HOUR);

                                //use the next one to fill in the homeNext layout
                                if (fillHomeNext) {
                                    //parse date and convert it from UTC to local time
                                    SimpleDateFormat readFormat = new SimpleDateFormat("yyyy MM dd HH");
                                    readFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                                    Date date = null;
                                    try {
                                        date = readFormat.parse(dateStr);
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                    SimpleDateFormat printFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm", Locale.ENGLISH);
                                    printFormat.setTimeZone(TimeZone.getDefault());
                                    String formattedDate = printFormat.format(date);
                                    mapToFillHomeNext = retrievedLocationsList.get(i);
                                    mapToFillHomeNext.put("formattedDate", formattedDate);
                                    Log.d(Helper.LOG_TAG,"mapToFillHomeNext is filled");
                                    fillHomeNext = false;
                                }

                                //parse current date to compare it to nearest date
                                Calendar cal = GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC"));
                                String target = retrievedLocationsList.get(i).get(Helper.TAG_YEAR) + " " +
                                        retrievedLocationsList.get(i).get(Helper.TAG_MONTH) + " " +
                                        retrievedLocationsList.get(i).get(Helper.TAG_DAY) + " " +
                                        retrievedLocationsList.get(i).get(Helper.TAG_HOUR) + " UTC";
                                try {
                                    cal.setTime(sdf.parse(target));
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                Date currentDate = cal.getTime();

                                if (currentDate.compareTo(closest) == 0) {
                                        //store selected forecast's date to helper
                                        helper.setCurrentForecastDateTime(dateStr);

                                        fillHomeNext = true;

                                        results = retrievedLocationsList.get(i);
                                    }
                            }
                        } else {
                            // no retrieved locations found
                            results.put("error", getString(R.string.sorry_no_forecast_available));
                            return results;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                results.put("error", getString(R.string.sorry_no_internet_connection));
                return results;
            }
            return results;
        }

        @Override
        protected void onPostExecute(HashMap<String,String> result) {
            super.onPostExecute(result);
            Log.d(Helper.LOG_TAG,"ConnectToServer onPostExecute");
            if (getFragmentManager() != null) { //run only if app is active
                getFragmentManager().executePendingTransactions(); //to make sure isAdded returns the correct value
            }
            if (isAdded()) { //prevent onPostExecute to run if the fragment is not still attached to the activity
                if (result.containsKey("error")) {
                    Toast.makeText(getActivity(), result.get("error"), Toast.LENGTH_LONG).show();
                    ProgressBar progressSpinner = (ProgressBar) rootView.findViewById(R.id.marker_progress);
                    progressSpinner.setVisibility(View.GONE);
                    TextView errorMsg = (TextView) rootView.findViewById(R.id.waitingMessage);
                    errorMsg.setText(result.get("error"));
                    Button retryBtn = (Button) rootView.findViewById(R.id.button_retry);
                    retryBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            updateUi();
                        }
                    });
                    retryBtn.setVisibility(View.VISIBLE);
                } else {
                    updateForecastOnUi(result);
                    spinner.setVisibility(View.GONE);
                    ((MainActivity)getActivity()).showTabs();
                }
                connectToServerIsRunning = false;
            }
        }

    }
}
