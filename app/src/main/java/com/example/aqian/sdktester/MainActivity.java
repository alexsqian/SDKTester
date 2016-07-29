package com.example.aqian.sdktester;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

public class MainActivity extends AppCompatActivity {
    private FirebaseAnalytics mFirebaseAnalytics;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //setting up remote config, and putting it into developer's mode to test many different config values
        FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();
        mFirebaseRemoteConfig.setConfigSettings(configSettings);
//        mFirebaseRemoteConfig.setDefaults(R.xml.default_parameters);
        mFirebaseRemoteConfig.fetch(0);
        mFirebaseRemoteConfig.activateFetched();

        boolean uploadGone = mFirebaseRemoteConfig.getBoolean("upload_button_disabled");
        boolean downloadGone = mFirebaseRemoteConfig.getBoolean("download_button_disabled");

        if (uploadGone) {
            setContentView(R.layout.activity_main_no_up);
            createDownloadButton();
        } else if (downloadGone) {
            setContentView(R.layout.activity_main_no_down);
            createUploadButton();
        } else {
            setContentView(R.layout.activity_main);
            createDownloadButton();
            createUploadButton();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        report(1);

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //Firebase crash reporting. Fatal crashes will automatically be sent to the console
    //But you can use this to see if there are any user errors ie (when a transfer fails)
    public static void report(int i) {
        if (i == 1) {
            FirebaseCrash.report(new Exception("Pressed Menu Button"));
        } else if (i == 2) {
            FirebaseCrash.report(new Exception("Pressed Downloads Button"));
        } else if (i == 3) {
            FirebaseCrash.report(new Exception("Started a Session"));
        }
    }

    public void createUploadButton() {
        Button upload = (Button) findViewById(R.id.uploadButton);
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, UploadActivity.class));
            }
        });
    }

    public void createDownloadButton() {
        Button download = (Button) findViewById(R.id.downloadButton);
        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, DownloadActivity.class));
                report(2);
            }
        });
    }
}
