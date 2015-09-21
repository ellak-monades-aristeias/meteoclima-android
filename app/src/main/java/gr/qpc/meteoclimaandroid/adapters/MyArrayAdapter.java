package gr.qpc.meteoclimaandroid.adapters;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import gr.qpc.meteoclimaandroid.Helper;
import gr.qpc.meteoclimaandroid.R;

/**
 * Created by spyros on 8/18/15.
 */
public class MyArrayAdapter extends ArrayAdapter {
    private final Context context;
    private final ArrayList<HashMap<String,String>> values;
    private Helper helper;
    private HashMap<Integer,String> ids_list;

    public MyArrayAdapter(Context context, ArrayList<HashMap<String,String>> values) {
        super(context, -1, values);
        this.context = context;
        this.values = values;
        helper = new Helper(context);
        ids_list = new HashMap<Integer,String>();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.list_item, parent, false);
        TextView firstLine = (TextView) rowView.findViewById(R.id.firstLine);
        TextView secondLine = (TextView) rowView.findViewById(R.id.secondLine);
        TextView thirdLine = (TextView) rowView.findViewById(R.id.thirdLine);
        TextView forthLine = (TextView) rowView.findViewById(R.id.forthLine);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
        HashMap<String,String> item = values.get(position);

        if (item.get("id") != null) {
            ids_list.put(position, item.get("id"));

            imageView.setImageResource(helper.returnDrawableId(Integer.parseInt(item.get(Helper.TAG_WEATHER_IMAGE))));
            firstLine.setText(item.get("formattedDate"));
            secondLine.setText(helper.formatTemperature(item.get(Helper.TAG_TEMP)) + " " + helper.returnBasicWeatherDescription(Integer.parseInt(item.get(Helper.TAG_WEATHER_IMAGE))));
            thirdLine.setText(Html.fromHtml("<b>Wind:</b> " + item.get(Helper.TAG_WIND_BEAUFORT) + " Bf / " + item.get(Helper.TAG_WINDDIR_SYM) + " <b>Pressure (hPa):</b> " + item.get(Helper.TAG_MSLP)));
            if (!item.get(Helper.TAG_RAIN).equals("0.0")) {
                forthLine.setText(Html.fromHtml("<b>Rain (mm):</b> " + item.get(Helper.TAG_RAIN) + " <b>Humidity (%):</b> " + item.get(Helper.TAG_RELHUM)));
            } else if (!item.get(Helper.TAG_SNOW).equals("0.0")) {
                forthLine.setText(Html.fromHtml("<b>Snow (mm):</b> " + item.get(Helper.TAG_RAIN) + " <b>Humidity (%):</b> " + item.get(Helper.TAG_RELHUM)));
            } else {
                forthLine.setText(Html.fromHtml("<b>Humidity (%):</b> " + item.get(Helper.TAG_RELHUM) + " <b>Heat Index (℃):</b> " + item.get(Helper.TAG_HEAT_INDEX)));
            }
        }

        return rowView;
    }

    public String getId(int position) {
        return ids_list.get(position);
    }
}