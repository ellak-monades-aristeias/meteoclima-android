package gr.qpc.meteoclimaandroid;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.util.Log;

import org.json.JSONArray;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by spyros on 8/18/15.
 */

public class Helper {

    private Context ctx;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private static JSONArray retrievedForecasts;
    private static boolean gotForecasts = false;
    private static boolean blockWidgetService = false;
    private static String currentForecastDateTime;

    private static final String PREF_NAME = "MeteoclimaPreferences";
    private static final String LAST_KNOWN_LOCATION = "LAST_KNOWN_LOCATION";
    private static final String WIDGET_UPDATE_INTERVAL_MINS_PREF = "WIDGET_UPDATE_INTERVAL_MINS_PREF";

    //Application Tag for Logger
    public static final String LOG_TAG = "Meteoclima";

    // url to get the location list
    public static String url_server = "http://83.212.85.153/~spyros/meteoclima/db_get_locations.php";

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
    public static final String TAG_WIND_BEAUFORT = "windBeaufort";
    public static final String TAG_LAND_OR_SEA = "landOrSea";

    public Helper(Context context){
        ctx = context;
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void storeLastKnownLocation(String location) {
        editor.putString(LAST_KNOWN_LOCATION,location);
        editor.commit();
    }

    public String getLastKnownLocation() {
        return prefs.getString(LAST_KNOWN_LOCATION,"Waiting for location.");
    }

    public String getLocationName(Location location) {
        String locality = ctx.getString(R.string.waiting_for_location);
        if (location != null) {
            try {
                Geocoder geo = new Geocoder(ctx, Locale.getDefault());
                List<Address> addresses = geo.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                if (addresses.isEmpty()) {
                    locality = "Location not found.";
                }
                else {
                    if (addresses.size() > 0) {
                        locality = addresses.get(0).getLocality();
                        //store it to preferences
                        storeLastKnownLocation(locality);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return locality;
    }

    public static void storeForecasts(JSONArray retrievedLocations) {
        Helper.retrievedForecasts = retrievedLocations;
        Helper.gotForecasts = true;
        Log.d(LOG_TAG,"Forecasts are stored to Helper.");
    }

    public static JSONArray getRetrievedForecasts() {
        if (retrievedForecasts != null) {
            return retrievedForecasts;
        } else {
            retrievedForecasts = null;
        }
        return retrievedForecasts;
    }

    public static boolean isGotForecasts() {
        return gotForecasts;
    }

    public static void setGotForecasts(boolean gotForecasts) {
        Helper.gotForecasts = gotForecasts;
    }

    public static boolean isBlockWidgetService() {
        return blockWidgetService;
    }

    public static void setBlockWidgetService(boolean blockWidgetService) {
        Helper.blockWidgetService = blockWidgetService;
    }

    public static String getCurrentForecastDateTime() {
        return currentForecastDateTime;
    }

    public static void setCurrentForecastDateTime(String currentForecastDateTime) {
        Helper.currentForecastDateTime = currentForecastDateTime;
    }

    public String returnBasicWeatherDescription(int num) {
        String basicWeatherDescription;
        switch (num) {
            case 1:  basicWeatherDescription = "Sunny";
                break;
            case 2:  basicWeatherDescription = "Mostly Sunny";
                break;
            case 3:  basicWeatherDescription = "Mostly Cloudy";
                break;
            case 4:  basicWeatherDescription = "Cloudy";
                break;
            case 5:  basicWeatherDescription = "Mostly Cloudy - Rain";
                break;
            case 6:  basicWeatherDescription = "Cloudy - Rain";
                break;
            case 7:  basicWeatherDescription = "Mostly Cloudy - Storm";
                break;
            case 8:  basicWeatherDescription = "Cloudy - Storm";
                break;
            case 9:  basicWeatherDescription = "Mostly Cloudy - Snow";
                break;
            case 10: basicWeatherDescription = "Cloudy - Snow";
                break;
            case 11: basicWeatherDescription = "Mostly Cloudy - A lot of Snow";
                break;
            case 12: basicWeatherDescription = "Cloudy - A lot of Snow";
                break;
            default: basicWeatherDescription = "Not Available";
                break;
        }
        return basicWeatherDescription;
    }

    public int returnDrawableId(int num) {
        int drawableId;
        switch (num) {
            case 1:  drawableId = R.drawable.sunny;
                break;
            case 2:  drawableId = R.drawable.m_cloudy;
                break;
            case 3:  drawableId = R.drawable.partly_cloudy;
                break;
            case 4:  drawableId = R.drawable.cloudy;
                break;
            case 5:  drawableId = R.drawable.m_c_rain;
                break;
            case 6:  drawableId = R.drawable.rainy;
                break;
            case 7:  drawableId = R.drawable.chance_storm;
                break;
            case 8:  drawableId = R.drawable.t_storm_rain;
                break;
            case 9:  drawableId = R.drawable.m_c_snow;
                break;
            case 10: drawableId = R.drawable.snow_shower;
                break;
            case 11: drawableId = R.drawable.p_c_snow;
                break;
            case 12: drawableId = R.drawable.snow;
                break;
            default: drawableId = R.drawable.na;
                break;
        }
        return drawableId;
    }

    public String[] getForecastDescriptions() {
        return new String[]{"Pressure (hPa)","Temperature (℃)","Rain (mm)","Snow (mm)","Wind speed (m/s)","Wind direction (degrees)","Humidity (%)","landOrSea"};
    }

    public String[] getTagNames() {
        return new String[]{"mslp","temp","rain","snow","windsp","winddir","relhum","landOrSea"};
    }

    public String formatTemperature(String temp) {
        Double tempDouble = Double.parseDouble(temp);
        return String.format("%.1f", tempDouble) + "℃";
    }

    public static Date getNearestDate(List<Date> dates, Date currentDate) {
        long minDiff = -1, currentTime = currentDate.getTime();
        Date minDate = null;
        for (Date date : dates) {
            long diff = Math.abs(currentTime - date.getTime());
            if ((minDiff == -1) || (diff < minDiff)) {
                minDiff = diff;
                minDate = date;
            }
        }
        return minDate;
    }


    //settings and widget stuff
    public void setWidgetUpdateIntervalPref(int intervalInMinutes) {
        editor.putInt(WIDGET_UPDATE_INTERVAL_MINS_PREF, intervalInMinutes * 60 * 1000);
        editor.commit();
    }

    public int getWidgetUpdateIntervalPref() {
        int interval = prefs.getInt(WIDGET_UPDATE_INTERVAL_MINS_PREF, 1800000) / 60 / 1000;
        return interval;
    }


    public void scheduleWidgetUpdate(Context context) {
        int interval = prefs.getInt(WIDGET_UPDATE_INTERVAL_MINS_PREF, 1800000);//default interval is 30 minutes
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pi = getAlarmIntentForWidgetUpdate(context);
        am.cancel(pi);
        am.setInexactRepeating(AlarmManager.RTC, System.currentTimeMillis(), interval, pi);
    }

    private PendingIntent getAlarmIntentForWidgetUpdate(Context context) {
        Intent intent = new Intent(context, MeteoclimaAppWidget.class);
        intent.setAction(MeteoclimaAppWidget.ACTION_UPDATE);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);
        return pi;
    }

    public void clearWidgetUpdate(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(getAlarmIntentForWidgetUpdate(context));
    }

    public boolean isWidgetServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}
