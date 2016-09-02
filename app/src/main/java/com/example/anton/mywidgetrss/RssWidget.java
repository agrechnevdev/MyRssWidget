package com.example.anton.mywidgetrss;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Calendar;

/**
 * Created by anton on 30.08.2016.
 */
public class RssWidget extends AppWidgetProvider {

    int widgetID = AppWidgetManager.INVALID_APPWIDGET_ID;
    static String title = "Заголовок";
    static String text = "Текст новости";
    static Document document = null;
    static int numberNews = 0;

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        final AlarmManager m = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        final Calendar TIME = Calendar.getInstance();
        TIME.set(Calendar.MINUTE, 0);
        TIME.set(Calendar.SECOND, 0);
        TIME.set(Calendar.MILLISECOND, 0);
        for (int i : appWidgetIds) {
            updateWidget(context, appWidgetManager, i, null);

            Intent serviceIntent = new Intent(context, UpdateService.class);
            serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, i);
            PendingIntent servicePending = PendingIntent.getService(context, i, serviceIntent, 0);
            m.setRepeating(AlarmManager.RTC, TIME.getTime().getTime(), 60000 * 1, servicePending);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
        Bundle extras = intent.getExtras();

        if (extras != null) {
            mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }
        if (mAppWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            if (intent.getAction().equalsIgnoreCase("ACTION_NEXT")) {
                if (RssWidget.numberNews != (document.getElementsByTagName("item").getLength() - 1)) {
                    RssWidget.numberNews++;
                }
            }
            if (intent.getAction().equalsIgnoreCase("ACTION_PREV")) {
                if (RssWidget.numberNews != 0) {
                    RssWidget.numberNews--;
                }
            }
            updateWidget(context, AppWidgetManager.getInstance(context), mAppWidgetId, document);
        }
    }


    static void updateWidget(Context ctx, AppWidgetManager appWidgetManager,
                             int widgetID, Document document) {
        RssWidget.document = document;
        Intent intent = new Intent(ctx, SettingsActivity.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
        PendingIntent pendingIntent = PendingIntent.getActivity(ctx, widgetID, intent, 0);

        Intent intentNext = new Intent(ctx, RssWidget.class);
        intentNext.setAction("ACTION_NEXT");
        intentNext.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
        PendingIntent pendingIntentNext = PendingIntent.getBroadcast(ctx, widgetID, intentNext, 0);

        Intent intentPrev = new Intent(ctx, RssWidget.class);
        intentPrev.setAction("ACTION_PREV");
        intentPrev.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
        PendingIntent pendingIntentPrev = PendingIntent.getBroadcast(ctx, widgetID, intentPrev, 0);

        RemoteViews views = new RemoteViews(ctx.getPackageName(), R.layout.widget_layout);
        views.setOnClickPendingIntent(R.id.settings, pendingIntent);
        views.setOnClickPendingIntent(R.id.next, pendingIntentNext);
        views.setOnClickPendingIntent(R.id.prev, pendingIntentPrev);

        Toast toast = Toast.makeText(ctx,  "Обновление", Toast.LENGTH_SHORT);
        toast.show();
        Integer visibility;
        if (document != null) {
            visibility = 1;
            NodeList nodeListItems = document.getElementsByTagName("item");
            Node node = nodeListItems.item(numberNews);
            NodeList nodelist = node.getChildNodes();
            for (int i = 0; i < nodelist.getLength(); i++) {
                Node nextNode = nodelist.item(i);
                if (nextNode.getNodeName().equals("title"))
                    title = nextNode.getTextContent();
                if (nextNode.getNodeName().equals("description"))
                    text = nextNode.getTextContent();
            }
        } else {
            visibility = -1;
        }
        views.setViewVisibility(R.id.next, visibility);
        views.setViewVisibility(R.id.prev, visibility);
        views.setTextViewText(R.id.headerText, title);
        views.setTextViewText(R.id.mainText, text);
        appWidgetManager.updateAppWidget(widgetID, views);
    }
}
