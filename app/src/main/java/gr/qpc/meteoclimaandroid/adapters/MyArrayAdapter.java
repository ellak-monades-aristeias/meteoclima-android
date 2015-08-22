package gr.qpc.meteoclimaandroid.adapters;

import android.content.Context;
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

    public MyArrayAdapter(Context context, ArrayList<HashMap<String,String>> values) {
        super(context, -1, values);
        this.context = context;
        this.values = values;
        helper = new Helper(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.list_item, parent, false);
        TextView firstLine = (TextView) rowView.findViewById(R.id.firstLine);
        TextView secondLine = (TextView) rowView.findViewById(R.id.secondLine);
        TextView thirdLine = (TextView) rowView.findViewById(R.id.thirdLine);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
        HashMap<String,String> item = values.get(position);

        firstLine.setText(item.get("1"));
        secondLine.setText(helper.returnBasicWeatherDescription(Integer.parseInt(item.get("3"))));
        thirdLine.setText("Temperature: " + item.get("2"));
        imageView.setImageResource(helper.returnDrawableId(Integer.parseInt(item.get("3"))));

        return rowView;
    }
}