package gr.qpc.meteoclimaandroid;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
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
import java.util.Map;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import gr.qpc.meteoclimaandroid.adapters.MySimpleAdapter;

public class MeteoclimaMainFragment extends Fragment implements
        ConnectionCallbacks,
        OnConnectionFailedListener,
        LocationListener {

    private View rootView;
    private LocationClient mLocationClient;
    private TextView locationTextView;
    private TextView dateTime;
    private ImageView imageView;
    private LinearLayout spinner;
    private Helper helper;
    private ArrayList<HashMap<String, String>> retrievedLocationsList;
    private HashMap<String,String> results;
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

        //first get the loading spinner
        spinner = (LinearLayout) rootView.findViewById(R.id.spinner);

        locationTextView = (TextView) rootView.findViewById(R.id.location);
        locationTextView.setText(helper.getLastKnownLocation());

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        //if forecast is already downloaded use this one
        if (helper.isGotForecasts()) {
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
            if (helper.isGotForecasts()) {
                updateUiFromStoredForecasts();
            } else {
                setUpLocationClientIfNeeded();
                if (!mLocationClient.isConnected()) {
                    mLocationClient.connect();
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
        helper.setGotForecasts(false);
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
        setUpLocationClientIfNeeded();
        if (!mLocationClient.isConnected()) {
            mLocationClient.connect();
        }
        updateUi();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        mLocationClient.requestLocationUpdates(
                REQUEST,
                this);  // LocationListener
        getFragmentManager().executePendingTransactions(); //to make sure isAdded returns the correct value
        if (isAdded()) { //prevent running if the fragment is not still attached to the activity
            if (helper.isGotForecasts()) {
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
        if (rootView != null) {
            imageView = (ImageView) rootView.findViewById(R.id.imageView);
            Resources res = getResources();
            Drawable drawable = res.getDrawable(helper.returnDrawableId(Integer.parseInt(map.get(Helper.TAG_WEATHER_IMAGE))));
            imageView.setImageDrawable(drawable);
            TextView basicWeatherDescriptionTextView = (TextView) rootView.findViewById(R.id.basicWeather);
            basicWeatherDescriptionTextView.setText(helper.returnBasicWeatherDescription(Integer.parseInt(map.get(Helper.TAG_WEATHER_IMAGE))));
            basicWeatherDescriptionTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);

            //update time on ui
            DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy, HH:mm");
            String date = df.format(Calendar.getInstance().getTime());
            dateTime = (TextView) rootView.findViewById(R.id.dateTime);
            dateTime.setText(date);

            String[] forecastDescriptions = helper.getForecastDescriptions();
            String[] tagNames = helper.getTagNames();
            GridView gridView = (GridView) rootView.findViewById(R.id.gridview);

            // create the grid item mapping
            String[] from = new String[] {"forecast_name", "value"};
            int[] to = new int[] { R.id.item1, R.id.item2};

            // prepare the list of all records
            List<HashMap<String, String>> fillMaps = new ArrayList<HashMap<String, String>>();
            for(int i = 0; i < forecastDescriptions.length; i++){
                HashMap<String, String> mapToFill = new HashMap<String, String>();
                mapToFill.put("forecast_name", forecastDescriptions[i]);
                mapToFill.put("value", map.get(tagNames[i]));
                fillMaps.add(mapToFill);
            }

            // fill in the grid_item layout
            MySimpleAdapter adapter = new MySimpleAdapter(getActivity(), fillMaps, R.layout.grid_item, from, to);
            gridView.setAdapter(adapter);
        }
    }

    public void updateUiFromStoredForecasts() {

        Log.d(Helper.LOG_TAG,"updateUiFromStoredForecasts is running...");

        //first update the location name
        String locationName = helper.getLocationName(mLastLocation);
        if (locationName.equals(getString(R.string.waiting_for_location))) {
            updateUi();
        } else {
            locationTextView.setText(locationName);
            locationTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
        }

        //and then update the forecast
        retrievedLocations = helper.getRetrievedForecasts();

        // looping through all locations
        try {
            for (int i = 0; i < retrievedLocations.length(); i++) {
                JSONObject c = retrievedLocations.getJSONObject(i);

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
                String relhum = c.getString(Helper.TAG_RELHUM);
                String weatherImage = c.getString(Helper.TAG_WEATHER_IMAGE);
                String windBeaufort = c.getString(Helper.TAG_WIND_BEAUFORT);
                String landOrSea = c.getString(Helper.TAG_LAND_OR_SEA);

                //parse date
                Calendar cal = GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC"));
                String target = yy + " " + mm + " " + dd + " " + hh + " UTC";
                Date result = null;
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
                map.put(Helper.TAG_LAT, lat);
                map.put(Helper.TAG_LON, lon);
                map.put(Helper.TAG_MSLP, mslp);
                map.put(Helper.TAG_TEMP, temp);
                map.put(Helper.TAG_RAIN, rain);
                map.put(Helper.TAG_SNOW, snow);
                map.put(Helper.TAG_WINDSP, windsp);
                map.put(Helper.TAG_WINDDIR, winddir);
                map.put(Helper.TAG_RELHUM, relhum);
                map.put(Helper.TAG_WEATHER_IMAGE, weatherImage);
                map.put(Helper.TAG_WIND_BEAUFORT, windBeaufort);
                map.put(Helper.TAG_LAND_OR_SEA, landOrSea);

                // adding HashList to ArrayList
                retrievedLocationsList.add(map);
            }

            //find the date closest to "now"
            //long now = System.currentTimeMillis();


            //TESTING ONLY NOW VALUE
            Date d = null;
            try {
                d = sdf.parse("2015 02 02 10 UTC");
            } catch (ParseException e) {
                e.printStackTrace();
            }
            final long now = d.getTime();
            //DON'T FORGET TO REMOVE


            //convert the Hashmap to List to find the closest date
            List<Date> datesToCompare = new ArrayList<Date>(dates.keySet());
            Date closest = helper.getNearestDate(datesToCompare, d);

            //loop retrievedLocationsList to send the closest location forecast to updateForecastInUi method
            for (int i = 0; i < retrievedLocationsList.size(); i++) {
                if (retrievedLocationsList.get(i).get(Helper.TAG_ID) == dates.get(closest)) {
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
            }
            if (mLocationClient.getLastLocation() == null) {
                locationName = helper.getLastKnownLocation();

            } else {
                mLastLocation = mLocationClient.getLastLocation();
                locationName = helper.getLocationName(mLastLocation);
            }
            return locationName;
        }

        @Override
        protected void onPostExecute(String locationName) {
            if (locationName.equals(getString(R.string.waiting_for_location))) {
                locationTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
                getLocationNameIsRunning = false;
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

                    // getting JSON string from URL
                    JSONObject json = jParser.makeHttpRequest(Helper.url_server, "GET", params);

                    try {
                        // Checking for SUCCESS TAG
                        int success = json.getInt(Helper.TAG_SUCCESS);

                        if (success == 1) {
                            // locations found
                            // Getting Array of retrieved locations
                            retrievedLocations = json.getJSONArray(Helper.TAG_LOCATIONS);

                            //store retrievedLocations to helper class
                            helper.storeForecasts(retrievedLocations);
                            helper.setGotForecasts(true);

                            // looping through all locations
                            for (int i = 0; i < retrievedLocations.length(); i++) {
                                JSONObject c = retrievedLocations.getJSONObject(i);

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
                                String relhum = c.getString(Helper.TAG_RELHUM);
                                String weatherImage = c.getString(Helper.TAG_WEATHER_IMAGE);
                                String windBeaufort = c.getString(Helper.TAG_WIND_BEAUFORT);
                                String landOrSea = c.getString(Helper.TAG_LAND_OR_SEA);

                                //parse date
                                Calendar cal = GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC"));
                                String target = yy + " " + mm + " " + dd + " " + hh + " UTC";
                                Date result = null;
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
                                map.put(Helper.TAG_LAT,lat);
                                map.put(Helper.TAG_LON,lon);
                                map.put(Helper.TAG_MSLP,mslp);
                                map.put(Helper.TAG_TEMP,temp);
                                map.put(Helper.TAG_RAIN,rain);
                                map.put(Helper.TAG_SNOW,snow);
                                map.put(Helper.TAG_WINDSP,windsp);
                                map.put(Helper.TAG_WINDDIR,winddir);
                                map.put(Helper.TAG_RELHUM,relhum);
                                map.put(Helper.TAG_WEATHER_IMAGE,weatherImage);
                                map.put(Helper.TAG_WIND_BEAUFORT,windBeaufort);
                                map.put(Helper.TAG_LAND_OR_SEA, landOrSea);

                                // adding HashList to ArrayList
                                retrievedLocationsList.add(map);
                            }

                            //find the date closest to "now"
                            //long now = System.currentTimeMillis();



                            //TESTING ONLY NOW VALUE
                            Date d = null;
                            try {
                                d = sdf.parse("2015 09 01 10 UTC");
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            final long now = d.getTime();
                            //DON'T FORGET TO REMOVE


                            //convert the Hashmap to List to find the closest date
                            List<Date> datesToCompare = new ArrayList<Date>(dates.keySet());
                            Date closest = helper.getNearestDate(datesToCompare, d);

                            //loop retrievedLocationsList to send the closest location forecast to updateForecastInUi method
                            for (int i = 0; i < retrievedLocationsList.size(); i++) {
                                if (retrievedLocationsList.get(i).get(Helper.TAG_ID) == dates.get(closest)) {
                                    //store selected forecast's date to helper
                                    helper.setCurrentForecastDateTime(
                                            retrievedLocationsList.get(i).get(Helper.TAG_YEAR) + " " +
                                                    retrievedLocationsList.get(i).get(Helper.TAG_MONTH) + " " +
                                                    retrievedLocationsList.get(i).get(Helper.TAG_DAY) + " " +
                                                    retrievedLocationsList.get(i).get(Helper.TAG_HOUR)
                                    );

                                    return retrievedLocationsList.get(i);
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
            return null;
        }

        @Override
        protected void onPostExecute(HashMap<String,String> result) {
            Log.d(Helper.LOG_TAG,"ConnectToServer onPostExecute");
            getFragmentManager().executePendingTransactions(); //to make sure isAdded returns the correct value
            Log.d(Helper.LOG_TAG,"isAdded is: " + isAdded());
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
