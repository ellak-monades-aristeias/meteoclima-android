package gr.qpc.meteoclimaandroid;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.LinearLayout;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;

import java.util.ArrayList;

public class MeteoclimaChartActivity extends AppCompatActivity {

    private ActionBar actionbar;
    private GraphicalView mChart;
    private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
    private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
    private XYSeries mCurrentSeries;
    private XYMultipleSeriesRenderer mCurrentRenderer;
    private int chartPointsCounter;
    private Boolean chartCompleted;
    private ArrayList<String> chartHoursPoints;
    private ArrayList<String> chartTempPoints;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meteoclima_chart);
        actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        chartHoursPoints = getIntent().getStringArrayListExtra("hours");
        chartTempPoints = getIntent().getStringArrayListExtra("temp_points");
    }

    @Override
    protected void onResume() {
        super.onResume();
        LinearLayout layout = (LinearLayout) findViewById(R.id.chart);
        if (mChart == null) {
            initChart();
            mChart = ChartFactory.getLineChartView(this, mDataset, mRenderer);
            layout.addView(mChart);
        } else {
            mChart.repaint();
        }
    }

    private void initChart() {
        mCurrentSeries = new XYSeries("Temperature");
        mDataset.addSeries(mCurrentSeries);
        /*mCurrentRenderer = new XYMultipleSeriesRenderer();
        mCurrentRenderer.setPointStyle(PointStyle.CIRCLE);
        mCurrentRenderer.setLineWidth(6);
        mCurrentRenderer.setPointStrokeWidth(14);
        mCurrentRenderer.setColor(Color.parseColor("#505050"));
        mRenderer.addSeriesRenderer(mCurrentRenderer);*/
        mRenderer.setZoomEnabled(false);
        mRenderer.setMarginsColor(Color.WHITE);
        //mRenderer.setShowLegend(false);
        mRenderer.setApplyBackgroundColor(true);
        mRenderer.setAxesColor(Color.BLACK);
        mRenderer.setXLabelsColor(Color.WHITE);
        mRenderer.setYLabelsColor(0, Color.RED);
        //mRenderer.setYLabelsPadding(-100);
        //mRenderer.setLabelsTextSize(35);
        mRenderer.setBackgroundColor(0x300000FF);

        for (int i = 0; i < chartHoursPoints.size(); i++) {
            mCurrentSeries.add(Double.parseDouble(chartHoursPoints.get(i)), Double.parseDouble(chartTempPoints.get(i)));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
