package com.example.anton.mywidgetrss;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.EditText;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by anton on 02.09.2016.
 */
public class UpdateService extends Service {

    static String address;
    Integer widgetID;
    @Override
    public void onCreate()
    {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {

        Bundle extras = intent.getExtras();
        if (extras != null) {
            widgetID = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            address = getSharedPreferences("settengs", Context.MODE_PRIVATE).getString("address", "0");
        }
        ServiceTask serviceTask = new ServiceTask();
        serviceTask.execute();
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    class ServiceTask extends AsyncTask<Void, Void, Void> {

        Document document;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Void doInBackground(Void... params) {

            try {
                URL url = new URL(address);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    InputStream is = conn.getInputStream();
                    DocumentBuilderFactory dbf = DocumentBuilderFactory
                            .newInstance();
                    DocumentBuilder db = dbf.newDocumentBuilder();
                    document = db.parse(is);

                    is.close();
                }
            } catch (Exception e) {

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            RssWidget.updateWidget(UpdateService.this, AppWidgetManager.getInstance(UpdateService.this), widgetID, document);
            Toast toast = Toast.makeText(UpdateService.this,  "Сервис обновил ленту", Toast.LENGTH_SHORT);
            if (document == null)
                toast = Toast.makeText(UpdateService.this,  "Лента не найдена", Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}
