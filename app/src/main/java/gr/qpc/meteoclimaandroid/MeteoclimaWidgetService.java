package gr.qpc.meteoclimaandroid;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.os.StrictMode;
import android.util.Log;
import android.widget.RemoteViews;

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

/**
 * Created by spyros on 8/24/15.
 */

public class MeteoclimaWidgetService extends Service implements
        ConnectionCallbacks,
        OnConnectionFailedListener,
        LocationListener {

    private Context ctx;
    private int widget_id;
    private static final LocationRequest REQUEST = LocationRequest.create()
            .setInterval(50000)         // 5 seconds
            .setFastestInterval(500)    // 16ms = 60fps
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    private LocationClient mLocationClient;
    private Location mLastLocation;
    private String locationName;
    private Helper helper;
    private ArrayList<HashMap<String, String>> retrievedLocationsList;
    private HashMap<String,String> results;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(Helper.LOG_TAG, "MeteoclimaWidgetService is running!");

        //fix strict mode network exception
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        ctx = this;
        widget_id = intent.getIntExtra("widget_id",0);
        helper = new Helper(this);
        //create the location client if needeed
        if (mLocationClient == null) {
            mLocationClient = new LocationClient(
                    this,
                    this,  // ConnectionCallbacks
                    this); // OnConnectionFailedListener
        }
        if (!mLocationClient.isConnected()) {
            mLocationClient.connect();
        }
        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        mLocationClient.disconnect();
        super.onDestroy();
    }

    @Override
    public void onLocationChanged(Location location) {
        //do nothing
    }

    @Override
    public void onConnected(Bundle bundle) {
        /*mLocationClient.requestLocationUpdates(
                REQUEST,
                this);  // LocationListener*/
        updateWidget();
    }

    @Override
    public void onDisconnected() {
        //do nothing
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        //do nothing
    }

    public void updateWidget() {
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(this.getPackageName(), R.layout.meteoclima_app_widget);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);

        // First find the location name
        if (mLocationClient.getLastLocation() == null && helper.getLastKnownLocation() != null) {
            views.setTextViewText(R.id.appwidget_text, helper.getLastKnownLocation());
        } else {
            mLastLocation = mLocationClient.getLastLocation();
            if (mLastLocation != null) {
                locationName = helper.getLocationName(mLastLocation);
            } else {
                Log.d(Helper.LOG_TAG,"MeteoclimeWidgetService: mLastLocation is null: " + mLastLocation);
            }

        }

        ConnectionChecker cc = new ConnectionChecker(ctx);

        //Hashmap to pass to onPostExecute in case of errors.
        results = new HashMap<String,String>();

        if (cc.isConnectingToInternet()) {
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("lat", String.valueOf(mLastLocation.getLatitude())));
            params.add(new BasicNameValuePair("lon", String.valueOf(mLastLocation.getLongitude())));
            params.add(new BasicNameValuePair("units", "metric"));
            params.add(new BasicNameValuePair("APPID", Helper.API_KEY));

            // Creating JSON Parser object
            JSONParser jParser = new JSONParser();

            // getting JSON string from URL
            JSONObject json = jParser.makeHttpRequest(Helper.url_server, "GET", params);

            // Hashmap for retrieved locations
            // (I am passing the right map from inside this one to onPostExecute and then to updateForecastOnUi).
            retrievedLocationsList = new ArrayList<HashMap<String, String>>();

            //build the SimpleDateFormat here to use it everywhere as it is
            DateFormat sdf = new SimpleDateFormat("yyyy MM dd HH z");
            sdf.setTimeZone(new SimpleTimeZone(SimpleTimeZone.UTC_TIME, "UTC"));

            //create a list to compare dates and find the nearest
            //List<Date> dates = new ArrayList<Date>();
            Map<Date,String> dates = new HashMap<Date,String>();

            //JSONArray
            JSONArray retrievedLocations = null;

            try {
                if (!json.isNull("list")) {
                    // locations found
                    // Getting Array of retrieved locations
                    retrievedLocations = json.getJSONArray(Helper.TAG_LOCATIONS);

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

                        // Storing each json item in variable
                        //get main inside list
                        JSONObject main = list.getJSONObject(Helper.TAG_MAIN);
                        String temp = main.getString(Helper.TAG_TEMP);
                        JSONArray weather_jarray = list.getJSONArray(Helper.TAG_WEATHER);

                        JSONObject weather = weather_jarray.getJSONObject(0);
                        String weatherImage = weather.getString(Helper.TAG_WEATHER_IMAGE);
                        String weatherDescription = weather.getString(Helper.TAG_WEATHER_DESCRIPTION);

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
                        map.put(Helper.TAG_ID, id);
                        map.put(Helper.TAG_YEAR,yy);
                        map.put(Helper.TAG_MONTH, mm);
                        map.put(Helper.TAG_DAY,dd);appWidgetManager.updateAppWidget(widget_id, views);
                        map.put(Helper.TAG_HOUR, hh);
                        map.put(Helper.TAG_TEMP,temp);
                        map.put(Helper.TAG_WEATHER_IMAGE,weatherImage);
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
                        if (retrievedLocationsList.get(i).get(Helper.TAG_ID) == dates.get(closest)) {
                            //store selected forecast's date to helper
                            helper.setCurrentForecastDateTime(
                                    retrievedLocationsList.get(i).get(Helper.TAG_YEAR) + " " +
                                            retrievedLocationsList.get(i).get(Helper.TAG_MONTH) + " " +
                                            retrievedLocationsList.get(i).get(Helper.TAG_DAY) + " " +
                                            retrievedLocationsList.get(i).get(Helper.TAG_HOUR)
                            );

                            //update widget's views
                            //set current date
                            DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy, HH:mm");
                            String date = df.format(Calendar.getInstance().getTime());
                            views.setTextViewText(R.id.widgetDateTime,date);
                            //add temperature to location name
                            views.setTextViewText(R.id.appwidget_text, locationName + " " + helper.formatTemperature(retrievedLocationsList.get(i).get(Helper.TAG_TEMP)));
                            views.setImageViewResource(R.id.widgetImageView, getResources().getIdentifier("open" + retrievedLocationsList.get(i).get(Helper.TAG_WEATHER_IMAGE), "drawable", getApplicationContext().getPackageName()));
                            views.setTextViewText(R.id.widgetBasicWeather, helper.capitalize(retrievedLocationsList.get(i).get(Helper.TAG_WEATHER_DESCRIPTION)));
                        }
                    }

                } else {
                    // no retrieved locations found
                    views.setTextViewText(R.id.widgetDateTime, getString(R.string.forecast_not_available));
                    Log.d(Helper.LOG_TAG, "MeteoclimeWidgetService: Sorry! Forecast not available.");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            views.setTextViewText(R.id.widgetDateTime, getString(R.string.no_internet_connection));
            Log.d(Helper.LOG_TAG, "MeteoclimeWidgetService: Sorry! You are not connected to the Internet.");
        }
    //update the widget's views either way
    appWidgetManager.updateAppWidget(widget_id, views);
    //stop the service
    stopSelf();
    }

}
