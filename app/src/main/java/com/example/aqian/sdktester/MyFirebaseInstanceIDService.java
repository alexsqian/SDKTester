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
import java.net.ProtocolException;
import java.net.URL;

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    private static final String TAG = "MyFirebaseIIDService";


    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.e(TAG, "Refreshed token: " + refreshedToken);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        try {
            sendRegistrationToServer(refreshedToken);
        } catch (IOException e) {
            Log.e(TAG, "IOEXCEPTION");
        }
    }

    private void sendRegistrationToServer(String token) throws ProtocolException, MalformedURLException, IOException {
        Log.e(TAG, "Sent Token somehow");
        URL url = null;
        url = new URL("http://10.6.2.103:8888/androidserver.php");


        String content = "token=" + token;

        HttpURLConnection conn = null;
        conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");

        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Length", "" + Integer.toString(content.getBytes().length));
        conn.setRequestProperty("Content-Language", "en-US");

        //Send request
        DataOutputStream wr = null;
        wr = new DataOutputStream(conn.getOutputStream());
        wr.writeBytes(content);
        wr.flush();

        if (wr != null) {
            wr.close();
        }

        //Get Response
        InputStream is = null;
        BufferedReader rd = null;
        is = conn.getInputStream();
        rd = new BufferedReader(new InputStreamReader(is));
        String line;
        StringBuffer response = new StringBuffer();
        while ((line = rd.readLine()) != null) {
            response.append(line);
            response.append('\r');
            if (rd != null) {
                rd.close();
            }
        }
    }
}
