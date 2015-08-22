package gr.qpc.meteoclimaandroid;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import gr.qpc.meteoclimaandroid.adapters.MySimpleAdapter;

@SuppressWarnings("ALL")
public class MeteoclimaMainFragment extends Fragment implements
        ConnectionCallbacks,
        OnConnectionFailedListener,
        LocationListener {

    private View rootView;
    private LocationClient mLocationClient;
    private TextView locationTextView;
    private TextView dateTime;
    private ImageView imageView;
    private Bitmap forecastBitmap;
    private LinearLayout spinner;
    private Helper helper;
    private ArrayList<HashMap<String, String>> retrievedLocationsList;
    private HashMap<String,String> results;

    // Creating JSON Parser object
    JSONParser jParser = new JSONParser();

    // url to get the location list
    private static String url_server = "http://83.212.85.153/~spyros/meteoclima/db_get_locations.php";

    // JSON Node names
    public static final String TAG_SUCCESS = "success";
    public static final String TAG_LOCATIONS = "locations";
    public static final String TAG_ID = "id";
    public static final String TAG_YEAR = "yy";
    public static final String TAG_MONTH = "mm";
    public static final String TAG_DAY = "dd";
    public static final String TAG_HOUR = "hh";
    public static final String TAG_LAT = "lat";
    public static final String TAG_LON = "lon";
    public static final String TAG_MSLP = "mslp";
    public static final String TAG_TEMP = "temp";
    public static final String TAG_RAIN = "rain";
    public static final String TAG_SNOW = "snow";
    public static final String TAG_WINDSP = "windsp";
    public static final String TAG_WINDDIR = "winddir";
    public static final String TAG_RELHUM = "relhum";
    public static final String TAG_LCOUD = "lcloud";
    public static final String TAG_MCLOUD = "mcloud";
    public static final String TAG_HCLOUD = "hcloud";
    public static final String TAG_WEATHER_IMAGE = "weatherImage";
    public static final String TAG_WIND_WAVE_IMAGE = "windWaveImage";
    public static final String TAG_LAND_OR_SEA = "landOrSea";

    // products JSONArray
    JSONArray retrievedLocations = null;

    // These settings are the same as the settings for the map. They will in fact give you updates
    // at the maximal rates currently possible.
    private static final LocationRequest REQUEST = LocationRequest.create()
            .setInterval(60000)         // 1 minute
            .setFastestInterval(16)    // 16ms = 60fps
            .setSmallestDisplacement(10)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    private static final String TAG = "Meteoclima-MainActivity";

    private Location mLastLocation;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (rootView == null) {
            rootView = inflater.inflate(R.layout.activity_meteoclima_main, container, false);
        }

        helper = new Helper(getActivity());

        //fix strict mode network exception
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        //first get the current location
        setUpLocationClientIfNeeded();
        if (!mLocationClient.isConnected()) {
            mLocationClient.connect();
        }
        locationTextView = (TextView) rootView.findViewById(R.id.location);
        locationTextView.setText(helper.getLastKnownLocation());

        //get the loading spinner
        spinner = (LinearLayout) rootView.findViewById(R.id.spinner);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpLocationClientIfNeeded();
        if (!mLocationClient.isConnected()) {
            mLocationClient.connect();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
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
        updateLocationOnUi();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        mLocationClient.requestLocationUpdates(
                REQUEST,
                this);  // LocationListener
        updateLocationOnUi();
    }

    @Override
    public void onDisconnected() {
        // Do nothing
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Do nothing
    }

    public void updateLocationOnUi() {
        if (mLocationClient.getLastLocation() == null) {
            locationTextView.setText(helper.getLastKnownLocation());
            if (!helper.getLastKnownLocation().equals("Waiting for location...")) {
                locationTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
            }
        } else {
            mLastLocation = mLocationClient.getLastLocation();
            String locationName = helper.getLocationName(mLastLocation);
            locationTextView.setText(locationName);
            if (!locationName.equals("Waiting for location...")) {
                locationTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
            }
            //new ConnectToServer().doInBackground(String.valueOf(mLastLocation.getLatitude()),String.valueOf(mLastLocation.getLongitude()));
            //test only!
            //new ConnectToServer().doInBackground("38.66900000","12.20800000");
            new ConnectToServer().execute("38.66900000", "12.20800000");
        }
    }

    public void updateForecastOnUi(HashMap<String,String> map) {
        imageView = (ImageView) rootView.findViewById(R.id.imageView);
        Resources res = getResources();
        Drawable drawable = res.getDrawable(helper.returnDrawableId(Integer.parseInt(map.get(TAG_WEATHER_IMAGE))));
        imageView.setImageDrawable(drawable);
        TextView basicWeatherDescriptionTextView = (TextView) rootView.findViewById(R.id.basicWeather);
        basicWeatherDescriptionTextView.setText(helper.returnBasicWeatherDescription(Integer.parseInt(map.get(TAG_WEATHER_IMAGE))));
        basicWeatherDescriptionTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);

        //update time on ui
        DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy, HH:mm");
        String date = df.format(Calendar.getInstance().getTime());
        dateTime = (TextView) rootView.findViewById(R.id.dateTime);
        dateTime.setText(date);

        String[] forecastDescriptions = helper.getForecastDescriptions();
        String[] tagNames = new String[]{"mslp","temp","rain","snow","windsp","winddir","relhum","lcloud","mcloud","hcloud","landOrSea"};
        GridView gridView = (GridView) rootView.findViewById(R.id.gridview);

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
        MySimpleAdapter adapter = new MySimpleAdapter(getActivity(), fillMaps, R.layout.grid_item, from, to);
        gridView.setAdapter(adapter);

    }

    private class ConnectToServer extends AsyncTask<String, Void, HashMap<String,String>> {

        protected HashMap<String,String> doInBackground(String... args) {

            //check for internet connection
            ConnectionChecker cc = new ConnectionChecker(getActivity());

            //Hashmap to pass to onPostExecute in case of errors.
            results = new HashMap<String,String>();

            if (cc.isConnectingToInternet()) {
                // Building Parameters
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("lat", args[0]));
                params.add(new BasicNameValuePair("lon", args[1]));

                // getting JSON string from URL
                JSONObject json = jParser.makeHttpRequest(url_server, "GET", params);

                // Check your log cat for JSON reponse
                //Log.d("Data retrieved from server: ", json.toString());

                // Hashmap for retrieved locations
                // (I am passing the right map from inside this one to onPostExecute and then to updateForecastOnUi).
                retrievedLocationsList = new ArrayList<HashMap<String, String>>();

                //build the SimpleDateFormat here to use it everywhere as it is
                DateFormat sdf = new SimpleDateFormat("yyyy MM dd HH z");
                sdf.setTimeZone(new SimpleTimeZone(SimpleTimeZone.UTC_TIME, "UTC"));

                //create a list to compare dates and find the nearest
                //List<Date> dates = new ArrayList<Date>();
                Map<Date,String> dates = new HashMap<Date,String>();

                try {
                    // Checking for SUCCESS TAG
                    int success = json.getInt(TAG_SUCCESS);

                    if (success == 1) {
                        // locations found
                        // Getting Array of retrieved locations
                        retrievedLocations = json.getJSONArray(TAG_LOCATIONS);

                        //store retrievedLocations to helper class
                        helper.storeForecasts(retrievedLocations);
                        helper.setGotForecasts(true);

                        // looping through all locations
                        for (int i = 0; i < retrievedLocations.length(); i++) {
                            JSONObject c = retrievedLocations.getJSONObject(i);

                            // Storing each json item in variable
                            String id = c.getString(TAG_ID);
                            String yy = c.getString(TAG_YEAR);
                            String mm = c.getString(TAG_MONTH);
                            String dd = c.getString(TAG_DAY);
                            String hh = c.getString(TAG_HOUR);
                            String lat = c.getString(TAG_LAT);
                            String lon = c.getString(TAG_LON);
                            String mslp = c.getString(TAG_MSLP);
                            String temp = c.getString(TAG_TEMP);
                            String rain = c.getString(TAG_RAIN);
                            String snow = c.getString(TAG_SNOW);
                            String windsp = c.getString(TAG_WINDSP);
                            String winddir = c.getString(TAG_WINDDIR);
                            String relhum = c.getString(TAG_RELHUM);
                            String lcloud = c.getString(TAG_LCOUD);
                            String mcloud = c.getString(TAG_MCLOUD);
                            String hcloud = c.getString(TAG_HCLOUD);
                            String weatherImage = c.getString(TAG_WEATHER_IMAGE);
                            String windWaveImage = c.getString(TAG_WIND_WAVE_IMAGE);
                            String landOrSea = c.getString(TAG_LAND_OR_SEA);

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
                            map.put(TAG_ID,id);
                            map.put(TAG_YEAR,yy);
                            map.put(TAG_MONTH,mm);
                            map.put(TAG_DAY,dd);
                            map.put(TAG_HOUR,hh);
                            map.put(TAG_LAT,lat);
                            map.put(TAG_LON,lon);
                            map.put(TAG_MSLP,mslp);
                            map.put(TAG_TEMP,temp);
                            map.put(TAG_RAIN,rain);
                            map.put(TAG_SNOW,snow);
                            map.put(TAG_WINDSP,windsp);
                            map.put(TAG_WINDDIR,winddir);
                            map.put(TAG_RELHUM,relhum);
                            map.put(TAG_LCOUD,lcloud);
                            map.put(TAG_MCLOUD,mcloud);
                            map.put(TAG_HCLOUD,hcloud);
                            map.put(TAG_WEATHER_IMAGE,weatherImage);
                            map.put(TAG_WIND_WAVE_IMAGE,windWaveImage);
                            map.put(TAG_LAND_OR_SEA, landOrSea);

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



                        Date closest = Collections.min(dates.keySet(), new Comparator<Date>() {
                            public int compare(Date d1, Date d2) {
                                long diff1 = Math.abs(d1.getTime() - now);
                                long diff2 = Math.abs(d2.getTime() - now);
                                return Long.compare(diff1, diff2);
                            }
                        });

                        //loop retrievedLocationsList to send the closest location forecast to updateForecastInUi method
                        for (int i = 0; i < retrievedLocationsList.size(); i++) {
                            if (retrievedLocationsList.get(i).get(TAG_ID) == dates.get(closest)) {
                                //store selected forecast's date to helper
                                helper.setCurrentForecastDateTime(
                                    retrievedLocationsList.get(i).get(TAG_YEAR) + " " +
                                    retrievedLocationsList.get(i).get(TAG_MONTH) + " " +
                                    retrievedLocationsList.get(i).get(TAG_DAY) + " " +
                                    retrievedLocationsList.get(i).get(TAG_HOUR)
                                );

                                return retrievedLocationsList.get(i);
                            }
                        }

                    } else {
                        // no retrieved locations found
                        results.put("error", "Sorry! Forecast cannot be found.");
                        return results;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                results.put("error","Sorry! You are not connected to the Internet.");
                return results;
            }
            return null;
        }

        @Override
        protected void onPostExecute(HashMap<String,String> result) {
            if (isAdded()) { //prevent onPostExecute to run if the fragment is not still attached to the activity
                if (result.containsKey("error")) {
                    Toast.makeText(getActivity(), result.get("error"), Toast.LENGTH_LONG).show();
                } else {
                    updateForecastOnUi(result);
                    spinner.setVisibility(View.GONE);
                    ((MainActivity)getActivity()).showTabs();
                }
            }

        }

    }
}
