package gr.qpc.meteoclimaandroid;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
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
    private static String currentForecastDateTime;

    private static final String PREF_NAME = "MeteoclimaPreferences";
    private static final String LAST_KNOWN_LOCATION = "LAST_KNOWN_LOCATION";

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
        String locality = "Waiting for location...";
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
        return locality;
    }

    public static void storeForecasts(JSONArray retrievedLocations) {
        Helper.retrievedForecasts = retrievedLocations;
        Helper.gotForecasts = true;
    }

    public static JSONArray getRetrievedForecasts() {
        if (retrievedForecasts != null) {
            return retrievedForecasts;
        } else {
            try {
                retrievedForecasts = new JSONArray("[{'error':'No forecasts available'}]");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return retrievedForecasts;
    }

    public static boolean isGotForecasts() {
        return gotForecasts;
    }

    public static void setGotForecasts(boolean gotForecasts) {
        Helper.gotForecasts = gotForecasts;
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
        return new String[]{"Pressure (hPa)","Temperature (C)","Rain (mm)","Snow (mm)","Wind speed (m/s)","Wind direction (degrees)","Humidity (%)","Low clouds","Medium clouds","High clouds","landOrSea"};
    }

}
