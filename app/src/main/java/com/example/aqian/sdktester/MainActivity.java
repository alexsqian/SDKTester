package com.example.aqian.sdktester;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.Driver;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    public static FirebaseAnalytics mFirebaseAnalytics;

    public static String buttonGone = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final SharedPreferences mSharedPreference = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Driver myDriver = new GooglePlayDriver(MainActivity.this);
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(myDriver);

        Job job = dispatcher.newJobBuilder()
                .setService(MyJobService.class)
                .setTag("my-tag")
                .setConstraints(
                        Constraint.ON_ANY_NETWORK)
                .setTrigger(Trigger.executionWindow(3600, 3600 + 120))
                .setLifetime(Lifetime.UNTIL_NEXT_BOOT)
                .setRecurring(true)
                .setReplaceCurrent(true)
                .build();

        int result = dispatcher.schedule(job);
        if (result != FirebaseJobDispatcher.SCHEDULE_RESULT_SUCCESS) {
        }

        buttonGone = mSharedPreference.getString(getString(R.string.button_disabled), "");

        if (buttonGone.equals("upload")) {
            setContentView(R.layout.activity_main_no_up);
            createDownloadButton();
        } else if (buttonGone.equals("download")) {
            setContentView(R.layout.activity_main_no_down);
            createUploadButton();
        } else {
            setContentView(R.layout.activity_main);
            createDownloadButton();
            createUploadButton();
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    protected void onStart() {
        super.onStart();

//        String test_AB = FirebaseRemoteConfig.getInstance().getString("button_disabled");
//        AppMeasurement.getInstance(getApplicationContext()).setUserProperty("MyExperiment", test_AB);

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

    }

    public void createUploadButton() {
        Button upload = (Button) findViewById(R.id.uploadButton);
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseMessaging.getInstance().unsubscribeFromTopic("news");
                startActivity(new Intent(MainActivity.this, UploadActivity.class));
            }
        });
    }

    public void createDownloadButton() {
        Button download = (Button) findViewById(R.id.downloadButton);
        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseMessaging.getInstance().subscribeToTopic("news");
                startActivity(new Intent(MainActivity.this, DownloadActivity.class));
            }
        });
    }

}
