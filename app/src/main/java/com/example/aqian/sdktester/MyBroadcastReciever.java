package com.example.aqian.sdktester;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class MyBroadcastReciever extends BroadcastReceiver {
    private static final String TAG = "BroadcastReceiver";
    private AsyncTask asyncTask;

    public MyBroadcastReciever() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String messageID = "messageID=" + intent.getStringExtra("uniqueMessageID");

        //Check the action and perform it accordingly
        if (intent.getAction().equals("date")) {
            Intent i = new Intent(context.getApplicationContext(), PressedNotification.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
        else if (intent.getAction().equals("log")) {
            Log.e(TAG, messageID);
        }

        sendCancelNotificationToServer(messageID);
    }

    //Sends the fcm_push_id to be cancelled to a php script on a local server
    //Script then sends a data message via FCM with the action "cancel"
    private void sendCancelNotificationToServer(final String messageID) {
        asyncTask = new AsyncTask<Object, Object, Object>() {
            protected Object doInBackground(Object... objects) {
                URL url;
                String response = "";
                try {
                    url = new URL("http://10.6.2.103:8888/notificationcancellation.php");

                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(15000);
                    conn.setConnectTimeout(15000);
                    conn.setRequestMethod("POST");
                    conn.setDoInput(true);
                    conn.setDoOutput(true);


                    OutputStream os = conn.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(
                            new OutputStreamWriter(os, "UTF-8"));
                    writer.write(messageID);

                    writer.flush();
                    writer.close();
                    os.close();
                    int responseCode = conn.getResponseCode();

                    if (responseCode == HttpsURLConnection.HTTP_OK) {
                        String line;
                        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        while ((line = br.readLine()) != null) {
                            response += line;
                        }
                    } else {
                        response = "";

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return null;
            }
        };
        asyncTask.execute();
    }
}
