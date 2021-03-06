package gr.qpc.meteoclimaandroid;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Implementation of App Widget functionality.
 */
public class MeteoclimaAppWidget extends AppWidgetProvider {

    public static final String ACTION_UPDATE = "gr.qpc.meteoclimaandroid.action.UPDATE";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            updateAppWidget(context, appWidgetManager, appWidgetIds[i]);
        }
    }

    private void onUpdate(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName thisAppWidgetComponentName = new ComponentName(context.getPackageName(),getClass().getName());
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidgetComponentName);
        onUpdate(context, appWidgetManager, appWidgetIds);
    }


    @Override
    public void onEnabled(Context context) {
        Helper helper = new Helper(context);
        helper.scheduleWidgetUpdate(context);
    }

    @Override
    public void onDisabled(Context context) {
        Helper helper = new Helper(context);
        helper.clearWidgetUpdate(context);
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        final Context ctx = context;
        final int widgetId = appWidgetId;
        final AppWidgetManager widgetManager = appWidgetManager;

        final Helper helper = new Helper(ctx);

        final Timer timer = new Timer();
        final TimerTask retryTask = new TimerTask() {
            @Override
            public void run() {
                Log.d(helper.LOG_TAG, "MeteoclimaAppWidget: Retrying...");
                MeteoclimaAppWidget.updateAppWidget(ctx, widgetManager, widgetId);
            }
        };

        //start the MeteoclimaWidgetService to update widget's data
        final Intent i= new Intent(ctx, MeteoclimaWidgetService.class);
        i.putExtra("widget_id", appWidgetId);
        if (!helper.isBlockWidgetService()) {
            ctx.startService(i);
        } else {
            Log.d(helper.LOG_TAG, "MeteoclimaAppWidget: Widget update service is blocked because the main app is running.");
            timer.schedule(retryTask, 60000); //execute in 1 minute
        }

        //open the Meteoclima app on click
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);  // Identifies the particular widget...
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        PendingIntent pendIntent = PendingIntent.getActivity(context, 0, intent, 0);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.meteoclima_app_widget);
        views.setOnClickPendingIntent(R.id.linearLayout1, pendIntent);
        appWidgetManager.updateAppWidget(appWidgetId, views);

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ACTION_UPDATE.equals(intent.getAction())) {
            onUpdate(context);
        } else super.onReceive(context, intent);
    }

}

