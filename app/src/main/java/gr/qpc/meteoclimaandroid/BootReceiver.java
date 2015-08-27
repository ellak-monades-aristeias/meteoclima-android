package gr.qpc.meteoclimaandroid;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {
    public BootReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Helper helper = new Helper(context);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] ids = appWidgetManager.getAppWidgetIds(new ComponentName(context, MeteoclimaAppWidget.class));
        if (ids.length > 0) {
            helper.scheduleWidgetUpdate(context);
        }
    }
}
