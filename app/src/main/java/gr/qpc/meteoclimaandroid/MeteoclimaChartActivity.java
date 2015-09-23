package gr.qpc.meteoclimaandroid;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.widget.LinearLayout;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class MeteoclimaChartActivity extends AppCompatActivity {

    private ActionBar actionbar;
    private GraphicalView mChart;
    private XYMultipleSeriesDataset mDataset;
    private XYMultipleSeriesRenderer mRenderer;
    private TimeSeries tempSeries;
    private XYSeriesRenderer tempRenderer;
    private TimeSeries windSeries;
    private XYSeriesRenderer windRenderer;
    private ArrayList<String> chartHoursPoints;
    private ArrayList<String> chartTempPoints;
    private ArrayList<String> chartWindPoints;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meteoclima_chart);
        actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        chartHoursPoints = getIntent().getStringArrayListExtra("hours");
        chartTempPoints = getIntent().getStringArrayListExtra("temp_points");
        chartWindPoints = getIntent().getStringArrayListExtra("wind_points");
    }

    @Override
    protected void onResume() {
        super.onResume();
        LinearLayout layout = (LinearLayout) findViewById(R.id.chart);
        if (mChart == null) {
            initChart();
            if (chartHoursPoints.size() > 8) {
                mChart = ChartFactory.getTimeChartView(this, mDataset, mRenderer, "dd/MM/yy");
            } else {
                mChart = ChartFactory.getTimeChartView(this, mDataset, mRenderer, "HH:mm");
            }
            layout.addView(mChart);
        } else {
            mChart.repaint();
        }
    }

    private void initChart() {
        DisplayMetrics metrics = getResources().getDisplayMetrics();

        tempSeries = new TimeSeries("Temperature (℃)");
        windSeries = new TimeSeries("Wind (Bf)");

        //parse date from hour of day
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm", Locale.ENGLISH);
        dateFormat.setTimeZone(TimeZone.getDefault());


        for (int i = 0; i < chartHoursPoints.size(); i++) {
            Date date = null;
            try {
                date = dateFormat.parse(chartHoursPoints.get(i));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            tempSeries.add(date, Double.parseDouble(chartTempPoints.get(i)));
            windSeries.add(date, Double.parseDouble(chartWindPoints.get(i)));
        }

        mDataset = new XYMultipleSeriesDataset();
        mDataset.addSeries(tempSeries);
        mDataset.addSeries(windSeries);

        tempRenderer = new XYSeriesRenderer();
        tempRenderer.setPointStyle(PointStyle.CIRCLE);
        tempRenderer.setLineWidth(4);
        tempRenderer.setColor(Color.RED);
        tempRenderer.setDisplayBoundingPoints(true);
        tempRenderer.setPointStyle(PointStyle.CIRCLE);
        tempRenderer.setPointStrokeWidth(3);

        windRenderer = new XYSeriesRenderer();
        windRenderer.setPointStyle(PointStyle.CIRCLE);
        windRenderer.setLineWidth(4);
        windRenderer.setColor(Color.parseColor("#009933"));
        windRenderer.setDisplayBoundingPoints(true);
        windRenderer.setPointStyle(PointStyle.CIRCLE);
        windRenderer.setPointStrokeWidth(3);
        
        mRenderer = new XYMultipleSeriesRenderer();
        mRenderer.addSeriesRenderer(tempRenderer);
        mRenderer.addSeriesRenderer(windRenderer);
        mRenderer.setYAxisMin(-10);
        mRenderer.setYAxisMax(40);
        mRenderer.setZoomEnabled(false, false);
        mRenderer.setPanEnabled(false, false);
        mRenderer.setMarginsColor(Color.WHITE);
        mRenderer.setLegendTextSize(metrics.scaledDensity * 16);
        mRenderer.setApplyBackgroundColor(true);
        mRenderer.setAxesColor(Color.BLACK);
        mRenderer.setLabelsColor(Color.BLACK);
        mRenderer.setXLabelsColor(Color.BLUE);
        mRenderer.setYLabelsColor(0, Color.BLUE);
        mRenderer.setYLabelsPadding(metrics.scaledDensity * 10);
        mRenderer.setLabelsTextSize(metrics.scaledDensity * 12);
        mRenderer.setShowGrid(true);
        mRenderer.setAxisTitleTextSize(metrics.scaledDensity * 14);

        mRenderer.setXTitle("Time (HH:mm)");
        mRenderer.setMargins(new int[]{(int) metrics.scaledDensity * 30, (int) metrics.scaledDensity * 45, (int)metrics.scaledDensity * 100, (int)metrics.scaledDensity * 20});



    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; go home
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
