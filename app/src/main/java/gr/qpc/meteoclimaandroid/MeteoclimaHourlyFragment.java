package gr.qpc.meteoclimaandroid;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

import gr.qpc.meteoclimaandroid.adapters.MyArrayAdapter;

/**
 * Created by spyros on 8/18/15.
 */
public class MeteoclimaHourlyFragment extends Fragment {

    private View rootView;
    private LinearLayout spinnerHourly;
    private Helper helper;
    private JSONArray retrievedForecasts;
    private ArrayList<HashMap<String, String>> list;

    private GraphicalView mChart;
    private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
    private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
    private XYSeries mCurrentSeries;
    private XYSeriesRenderer mCurrentRenderer;
    private int chartPointsCounter;
    private Boolean chartCompleted;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_hourly, container,
                false);

        spinnerHourly = (LinearLayout) rootView.findViewById(R.id.spinner_hourly);

        helper = new Helper(getActivity());

        chartPointsCounter = 0;
        chartCompleted = false;

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        LinearLayout layout = (LinearLayout) rootView.findViewById(R.id.chart);
        if (mChart == null) {
            initChart();
            mChart = ChartFactory.getCubeLineChartView(getActivity(), mDataset, mRenderer, 0.3f);
            layout.addView(mChart);
        } else {
            mChart.repaint();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            populateList();
        }
    }

    private void initChart() {
        mCurrentSeries = new XYSeries("Temperature");
        mDataset.addSeries(mCurrentSeries);
        mCurrentRenderer = new XYSeriesRenderer();
        mCurrentRenderer.setPointStyle(PointStyle.CIRCLE);
        mCurrentRenderer.setLineWidth(6);
        mCurrentRenderer.setPointStrokeWidth(14);
        mRenderer.addSeriesRenderer(mCurrentRenderer);
        mRenderer.setGridColor(Color.CYAN);
        mRenderer.setZoomEnabled(false);
        mRenderer.setMarginsColor(Color.WHITE);
        mRenderer.setShowLegend(false);
    }

    private void addDataToChart(double y) {
        if (!chartCompleted) {
            mCurrentSeries.add(chartPointsCounter,y);
            mChart.repaint();
            chartPointsCounter = chartPointsCounter+3;
            //System.out.println("Added point " + chartPointsCounter + "," + y + " to chart");
        }
    }

    private void populateList() {
        if (Helper.isGotForecasts()) {
            retrievedForecasts = helper.getRetrievedForecasts();

            list = new ArrayList<HashMap<String, String>>();

            // looping through all locations
            try {
                for (int i = 0; i < retrievedForecasts.length(); i++) {
                    JSONObject c = retrievedForecasts.getJSONObject(i);

                    if (c.has("error")) {
                        System.out.println(c.getString("error"));
                    }

                    // Storing each json item in variable
                    String id = c.getString(MeteoclimaMainFragment.TAG_ID);
                    String yy = c.getString(MeteoclimaMainFragment.TAG_YEAR);
                    String mm = c.getString(MeteoclimaMainFragment.TAG_MONTH);
                    String dd = c.getString(MeteoclimaMainFragment.TAG_DAY);
                    String hh = c.getString(MeteoclimaMainFragment.TAG_HOUR);
                    String temp = c.getString(MeteoclimaMainFragment.TAG_TEMP);
                    String weatherImage = c.getString(MeteoclimaMainFragment.TAG_WEATHER_IMAGE);

                    //parse date and convert it from UTC to local time
                    String dateStr = yy + " " + mm + " " + dd + " " + hh;
                    SimpleDateFormat readFormat = new SimpleDateFormat("yyyy MM dd HH");
                    readFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                    Date date = readFormat.parse(dateStr);

                    SimpleDateFormat printFormat = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
                    printFormat.setTimeZone(TimeZone.getDefault());
                    String formattedDate = printFormat.format(date);
                    //System.out.println("formattedDate: " + printFormat.format(date));

                    //parse the current forecasts date from helper to compare it
                    Date currentForecastDate = readFormat.parse(helper.getCurrentForecastDateTime());

                    Calendar cal1 = Calendar.getInstance();
                    Calendar cal2 = Calendar.getInstance();
                    cal1.setTime(date);
                    cal2.setTime(currentForecastDate);
                    boolean sameDay = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);

                    //keep only current day's forecasts
                    if (sameDay) {
                        //put everything in hashmaps and then in an ArrayList to populate the listView
                        HashMap<String,String> map = new HashMap<String,String>();
                        map.put("1", formattedDate);
                        map.put("2", temp);
                        map.put("3", weatherImage);
                        list.add(map);
                        //add point to chart
                        addDataToChart(Double.parseDouble(temp));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            chartCompleted = true;
            MyArrayAdapter adapter = new MyArrayAdapter(getActivity(), list);
            spinnerHourly.setVisibility(View.GONE);
            if (rootView == null) {
                rootView = getView();
            }
            ListView listView = (ListView) rootView.findViewById(R.id.listview_hourly);
            listView.setAdapter(adapter);
        }
    }
}
