package gr.qpc.meteoclimaandroid.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

import gr.qpc.meteoclimaandroid.R;

/**
 * Created by spyros on 8/18/15.
 */
public class MySimpleAdapter extends SimpleAdapter {

    public MySimpleAdapter(Context context, List<HashMap<String, String>> items, int resource, String[] from, int[] to) {
        super(context, items, resource, from, to);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);

        TextView first = (TextView) view.findViewById(R.id.item1);
        TextView second = (TextView) view.findViewById(R.id.item2);

        if (position % 2 == 0) {
            view.setBackgroundColor(Color.GRAY);
            first.setTextColor(Color.WHITE);
            second.setTextColor(Color.WHITE);
        } else {
            view.setBackgroundColor(0x300000FF);
        }



        return view;
    }
}
