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

            imageView.setImageResource(context.getResources().getIdentifier("open" + item.get(Helper.TAG_WEATHER_IMAGE), "drawable", context.getPackageName()));
            firstLine.setText(item.get("formattedDate"));
            secondLine.setText(helper.formatTemperature(item.get(Helper.TAG_TEMP)) + " " + helper.capitalize(item.get(Helper.TAG_WEATHER_DESCRIPTION)));
            thirdLine.setText(Html.fromHtml("<b>Wind:</b> " + item.get(Helper.TAG_WINDSP) + " " + Helper.UNIT_WIND_SPEED + " " + helper.windDegreesToDirection(item.get(Helper.TAG_WINDDIR)) + " <b>Pressure:</b> " + item.get(Helper.TAG_MSLP) + " " + Helper.UNIT_MSLP));
            if (!item.get(Helper.TAG_SNOW).equals("0.0")) {
                forthLine.setText(Html.fromHtml("<b>Snow:</b> " + item.get(Helper.TAG_RAIN) + " " +  Helper.UNIT_SNOW  + " <b>Humidity:</b> " + item.get(Helper.TAG_RELHUM) + " " + Helper.UNIT_RELHUM));
            } else if (!item.get(Helper.TAG_RAIN).equals("0.0")) {
                forthLine.setText(Html.fromHtml("<b>Rain:</b> " + item.get(Helper.TAG_RAIN) + " " + Helper.UNIT_RAIN + " <b>Humidity:</b> " + item.get(Helper.TAG_RELHUM) + " " + Helper.UNIT_RELHUM));
            }  else {
                forthLine.setText(Html.fromHtml("<b>Humidity:</b> " + item.get(Helper.TAG_RELHUM) + " " + Helper.UNIT_RELHUM));
            }
        }

        return rowView;
    }

    public String getId(int position) {
        return ids_list.get(position);
    }
}