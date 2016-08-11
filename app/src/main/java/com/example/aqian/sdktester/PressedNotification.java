package com.example.aqian.sdktester;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class PressedNotification extends AppCompatActivity {
    private AsyncTask asyncTask;
    private String date = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        retrieveSomethingFromServer();
        setContentView(R.layout.activity_pressed_notification);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void retrieveSomethingFromServer() {
        asyncTask = new AsyncTask<Object, Object, Object>() {
            protected Object doInBackground(Object... objects) {
                String result = "";
                try {

                    URL url = new URL("http://10.6.2.103:8888/senddata.php");
                    URLConnection conn = url.openConnection();

                    HttpURLConnection httpConn = (HttpURLConnection) conn;
                    httpConn.setAllowUserInteraction(false);
                    httpConn.setInstanceFollowRedirects(true);
                    httpConn.setRequestMethod("GET");
                    httpConn.connect();

                    InputStream is = httpConn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    result = reader.readLine();

                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.e("gotten from php", result);
                return result;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                date = (String) o;

            }
        };
        asyncTask.execute();
    }
}
