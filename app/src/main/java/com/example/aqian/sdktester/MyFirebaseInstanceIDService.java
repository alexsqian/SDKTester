package com.example.aqian.sdktester;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    private static final String TAG = "MyFirebaseIIDService";

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.e(TAG, "Refreshed token: " + refreshedToken);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(refreshedToken);
    }

    private void sendRegistrationToServer(String token) {
        Log.e(TAG, "Sent Token somehow");
        URL url = null;
        try {
            url = new URL("http://10.6.2.103:8888/androidserver.php");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        String content = "token=" + token;

        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
        } catch (IOException e) {
            e.printStackTrace();
        }
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Length", "" + Integer.toString(content.getBytes().length));
        conn.setRequestProperty("Content-Language", "en-US");

        //Send request
        DataOutputStream wr = null;
        try {
            wr = new DataOutputStream(conn.getOutputStream());
            wr.writeBytes(content);
            wr.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                wr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //Get Response
        InputStream is = null;
        BufferedReader rd = null;
        try {
            is = conn.getInputStream();
            rd = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuffer response = new StringBuffer();
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            Log.e(TAG, "response=" + response.toString());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                rd.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
