package cm.aptoide.ptdev.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import cm.aptoide.ptdev.R;

/**
 * Created by brutus on 02-01-2014.
 */
public class SearchWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        int n = appWidgetIds.length;
        for(int i = 0; i < n; i++) {

            Intent intent = new Intent(context, SearchWidgetActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.search_widget);
            remoteViews.setOnClickPendingIntent(R.id.search_widget_text, pendingIntent);
//            remoteViews.setOnClickPendingIntent(R.id.search_widget_button, pendingIntent);

            appWidgetManager.updateAppWidget(appWidgetIds[i], remoteViews);
        }
    }
}