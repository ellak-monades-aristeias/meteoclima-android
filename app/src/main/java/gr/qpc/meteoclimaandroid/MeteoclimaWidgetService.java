package gr.qpc.meteoclimaandroid;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
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

        System.out.println("MeteoclimaWidgetService is running!");

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
        mLocationClient.requestLocationUpdates(
                REQUEST,
                this);  // LocationListener
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
                System.out.println("MeteoclimeWidgetService: mLastLocation is null: " + mLastLocation);
            }

        }
        //new ConnectToServer().doInBackground(String.valueOf(mLastLocation.getLatitude()),String.valueOf(mLastLocation.getLongitude()));
        //test only!
        //new ConnectToServer().doInBackground("38.66900000","12.20800000");
        //check for internet connection
        ConnectionChecker cc = new ConnectionChecker(ctx);

        //Hashmap to pass to onPostExecute in case of errors.
        results = new HashMap<String,String>();

        if (cc.isConnectingToInternet()) {
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("lat", String.valueOf(mLastLocation.getLatitude())));
            params.add(new BasicNameValuePair("lon", String.valueOf(mLastLocation.getLongitude())));

            // Creating JSON Parser object
            JSONParser jParser = new JSONParser();

            // getting JSON string from URL
            JSONObject json = jParser.makeHttpRequest(MeteoclimaMainFragment.url_server, "GET", params);

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
                // Checking for SUCCESS TAG
                int success = json.getInt(MeteoclimaMainFragment.TAG_SUCCESS);

                if (success == 1) {
                    // locations found
                    // Getting Array of retrieved locations
                    retrievedLocations = json.getJSONArray(MeteoclimaMainFragment.TAG_LOCATIONS);

                    // looping through all locations
                    for (int i = 0; i < retrievedLocations.length(); i++) {
                        JSONObject c = retrievedLocations.getJSONObject(i);

                        // Storing each json item in variable
                        String id = c.getString(MeteoclimaMainFragment.TAG_ID);
                        String yy = c.getString(MeteoclimaMainFragment.TAG_YEAR);
                        String mm = c.getString(MeteoclimaMainFragment.TAG_MONTH);
                        String dd = c.getString(MeteoclimaMainFragment.TAG_DAY);
                        String hh = c.getString(MeteoclimaMainFragment.TAG_HOUR);
                        String lat = c.getString(MeteoclimaMainFragment.TAG_LAT);
                        String lon = c.getString(MeteoclimaMainFragment.TAG_LON);
                        String temp = c.getString(MeteoclimaMainFragment.TAG_TEMP);
                        String weatherImage = c.getString(MeteoclimaMainFragment.TAG_WEATHER_IMAGE);

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
                        map.put(MeteoclimaMainFragment.TAG_ID,id);
                        map.put(MeteoclimaMainFragment.TAG_YEAR,yy);
                        map.put(MeteoclimaMainFragment.TAG_MONTH, mm);
                        map.put(MeteoclimaMainFragment.TAG_DAY,dd);appWidgetManager.updateAppWidget(widget_id, views);
                        map.put(MeteoclimaMainFragment.TAG_HOUR, hh);
                        map.put(MeteoclimaMainFragment.TAG_LAT,lat);
                        map.put(MeteoclimaMainFragment.TAG_LON,lon);
                        map.put(MeteoclimaMainFragment.TAG_TEMP,temp);
                        map.put(MeteoclimaMainFragment.TAG_WEATHER_IMAGE,weatherImage);

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

                    /*Date closest = Collections.min(dates.keySet(), new Comparator<Date>() {
                        public int compare(Date d1, Date d2) {
                            long diff1 = Math.abs(d1.getTime() - now);
                            long diff2 = Math.abs(d2.getTime() - now);
                            //return Long.compare(diff1, diff2);
                            return Long.compare(diff1, diff2);
                        }
                    });*/

                    //convert the Hashmap to List to find the closest date
                    List<Date> datesToCompare = new ArrayList<Date>(dates.keySet());
                    Date closest = helper.getNearestDate(datesToCompare, d);

                    //loop retrievedLocationsList to send the closest location forecast to updateForecastInUi method
                    for (int i = 0; i < retrievedLocationsList.size(); i++) {
                        if (retrievedLocationsList.get(i).get(MeteoclimaMainFragment.TAG_ID) == dates.get(closest)) {
                            //store selected forecast's date to helper
                            helper.setCurrentForecastDateTime(
                                    retrievedLocationsList.get(i).get(MeteoclimaMainFragment.TAG_YEAR) + " " +
                                            retrievedLocationsList.get(i).get(MeteoclimaMainFragment.TAG_MONTH) + " " +
                                            retrievedLocationsList.get(i).get(MeteoclimaMainFragment.TAG_DAY) + " " +
                                            retrievedLocationsList.get(i).get(MeteoclimaMainFragment.TAG_HOUR)
                            );

                            //update widget's views
                            //set current date
                            DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy, HH:mm");
                            String date = df.format(Calendar.getInstance().getTime());
                            views.setTextViewText(R.id.widgetDateTime,date);
                            //add temperature to location name
                            views.setTextViewText(R.id.appwidget_text, locationName + " " + helper.formatTemperature(retrievedLocationsList.get(i).get(MeteoclimaMainFragment.TAG_TEMP)));
                            views.setImageViewResource(R.id.widgetImageView, helper.returnDrawableId(Integer.parseInt(retrievedLocationsList.get(i).get(MeteoclimaMainFragment.TAG_WEATHER_IMAGE))));
                            views.setTextViewText(R.id.widgetBasicWeather, helper.returnBasicWeatherDescription(Integer.parseInt(retrievedLocationsList.get(i).get(MeteoclimaMainFragment.TAG_WEATHER_IMAGE))));
                            appWidgetManager.updateAppWidget(widget_id, views);
                        }
                    }

                } else {
                    // no retrieved locations found
                    System.out.println("MeteoclimeWidgetService: Sorry! Forecast cannot be found.");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("MeteoclimeWidgetService: Sorry! You are not connected to the Internet.");
        }
    //stop the service
    stopSelf();
    }

}
