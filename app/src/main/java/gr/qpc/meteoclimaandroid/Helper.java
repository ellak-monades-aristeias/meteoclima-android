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

    //OpenWeatherMap API Key
    public static String API_KEY = "2631c63cc233d2b187e4b0bd102b5ce4";

    // url to get the location list
    public static String url_server = "http://api.openweathermap.org/data/2.5/forecast";

    // JSON Node names
    public static final String TAG_SUCCESS = "success";
    public static final String TAG_LOCATIONS = "list";
    public static final String TAG_MAIN = "main";
    public static final String TAG_ID = "dt";
    public static final String TAG_DATE_HOUR = "dt_txt";
    public static final String TAG_YEAR = "yy";
    public static final String TAG_MONTH = "mm";
    public static final String TAG_DAY = "dd";
    public static final String TAG_HOUR = "hh";
    public static final String TAG_LAT = "lat";
    public static final String TAG_LON = "lon";
    public static final String TAG_MSLP = "pressure";
    public static final String TAG_TEMP = "temp";
    public static final String TAG_RAIN = "rain";
    public static final String TAG_SNOW = "snow";
    public static final String TAG_WIND = "wind";
    public static final String TAG_WINDSP = "speed";
    public static final String TAG_WINDDIR = "deg";
    public static final String TAG_RELHUM = "humidity";
    public static final String TAG_WEATHER = "weather";
    public static final String TAG_WEATHER_DESCRIPTION = "description";
    public static final String TAG_WEATHER_IMAGE = "icon";

    //units of measurement
    public static final String UNIT_MSLP = "hPa";
    public static final String UNIT_RAIN = "mm";
    public static final String UNIT_SNOW = "mm";
    public static final String UNIT_RELHUM = "%";
    public static final String UNIT_WIND_SPEED = "m/s";

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
                    locality = ctx.getString(R.string.unknown);
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

    public String[] getForecastDescriptions() {
        return new String[]{"Pressure","Rain","Snow","Humidity"};
    }

    public String[] getForecastUnits() {
        return new String[]{UNIT_MSLP,UNIT_RAIN,UNIT_SNOW,UNIT_RELHUM};
    }

    public String[] getTagNames() {
        return new String[]{TAG_MSLP,TAG_RAIN,TAG_SNOW,TAG_RELHUM};
    }

    public String formatTemperature(String temp) {
        Double tempDouble = Double.parseDouble(temp);
        return String.format("%.1f", tempDouble) + "℃";
    }

    public String capitalize(String givenString) {
        String[] arr = givenString.split(" ");
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < arr.length; i++) {
            sb.append(Character.toUpperCase(arr[i].charAt(0)))
                    .append(arr[i].substring(1)).append(" ");
        }
        return sb.toString().trim();
    }

    public String windDegreesToDirection(String deg) {
        float degrees = Float.parseFloat(deg);
        if (degrees >= 348.75000 && degrees <= 365 || degrees >= 0.0 && degrees <= 11.24000) {
            return "N";
        }
        if (degrees >= 11.25000 && degrees <= 33.74000) {
            return "NNE";
        }
        if (degrees >= 33.75000 && degrees <= 56.24000) {
            return "NE";
        }
        if (degrees >= 56.25000 && degrees <= 78.74000) {
            return "ENE";
        }
        if (degrees >= 78.75000 && degrees <= 101.24000) {
            return "E";
        }
        if (degrees >= 101.25000 && degrees <= 123.74000) {
            return "ESE";
        }
        if (degrees >= 123.75000 && degrees <= 146.24000) {
            return "SE";
        }
        if (degrees >= 146.25000 && degrees <= 168.74000) {
            return "SSE";
        }
        if (degrees >= 168.75000 && degrees <= 191.24000) {
            return "S";
        }
        if (degrees >= 191.25000 && degrees <= 213.74000) {
            return "SSW";
        }
        if (degrees >= 213.75000 && degrees <= 236.24000) {
            return "SW";
        }
        if (degrees >= 236.25000 && degrees <= 258.74000) {
            return "WSW";
        }
        if (degrees >= 258.75000 && degrees <= 281.24000) {
            return "W";
        }
        if (degrees >= 281.25000 && degrees <= 303.74000) {
            return "WNW";
        }
        if (degrees >= 303.75000 && degrees <= 326.24000) {
            return "NW";
        }
        if (degrees >= 326.25000 && degrees <= 348.74000) {
            return "NNW";
        }
        return "N/A";
    }

    public static Date getNearestDate(List<Date> dates, Date currentDate) {
        long minDiff = -1;
        long currentTime = currentDate.getTime();
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
