package com.example.anton.mywidgetrss;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by anton on 30.08.2016.
 */
public class SettingsActivity extends Activity {

    Integer widgetID;
    Handler handler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            widgetID = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }
        setContentView(R.layout.settings);
    }

    public void onClick(View v) {
        SettingTask as = new SettingTask();
        as.execute();
    }

    class SettingTask extends AsyncTask<Void, Void, Void> {

        String textNews;
        Document document;
        String title = "Заголовок";
        String text = "Текст новости";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            EditText editText = (EditText) findViewById(R.id.address);
            textNews = editText.getText().toString();
        }

        @Override
        protected Void doInBackground(Void... params) {

            try {
                URL url = new URL(textNews);
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
                finish();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            RssWidget.updateWidget(SettingsActivity.this, AppWidgetManager.getInstance(SettingsActivity.this), widgetID, document);
            SharedPreferences sp = getSharedPreferences("settengs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("address", textNews);
            editor.apply();
            Toast toast = Toast.makeText(SettingsActivity.this,  "Загрузка завершена", Toast.LENGTH_SHORT);
            if (document == null)
                toast = Toast.makeText(SettingsActivity.this,  "Лента не найдена", Toast.LENGTH_SHORT);
            toast.show();
            finish();
        }
    }
}
