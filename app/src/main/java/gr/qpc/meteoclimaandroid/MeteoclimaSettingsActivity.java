package gr.qpc.meteoclimaandroid;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MeteoclimaSettingsActivity extends AppCompatActivity {

    private ActionBar actionbar;
    private Helper helper;
    private EditText intervalEdtTxt;
    private Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;
        actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        helper = new Helper(this);
        setContentView(R.layout.activity_meteoclima_settings);
        intervalEdtTxt = (EditText) findViewById(R.id.widget_interval_value);
        intervalEdtTxt.setText(String.valueOf(helper.getWidgetUpdateIntervalPref()));
        Button saveButton = (Button) findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                helper.setWidgetUpdateIntervalPref(Integer.parseInt(intervalEdtTxt.getText().toString()));
                helper.clearWidgetUpdate(activity.getApplicationContext());
                helper.scheduleWidgetUpdate(activity.getApplicationContext());
                activity.finish();
            }
        });
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
