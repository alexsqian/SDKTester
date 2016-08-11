package com.example.aqian.sdktester;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

public class MyJobService extends JobService {
    private AsyncTask asyncTask;
    private final String TAG = "JobService";
    FirebaseRemoteConfig mFirebaseRemoteConfig;

    @Override
    public boolean onStartJob(JobParameters job) {
        Log.i(TAG, "Started the job");

        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(true)
                .build();
        mFirebaseRemoteConfig.setConfigSettings(configSettings);
        configSettings.isDeveloperModeEnabled();

        //Default parameters set in the xml files. However, Remote Config will take the
        //server side's set and default values first, and if they don't exist, then use these
        mFirebaseRemoteConfig.setDefaults(R.xml.default_parameters);

        // Begin some async work
        asyncTask = new AsyncTask<Object, Object, Object>() {
            protected Object doInBackground(Object... objects) {
                mFirebaseRemoteConfig.fetch(0).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.i(TAG, "Fetch was successful");

                            // Once the config is successfully fetched it must be activated before newly fetched
                            // values are returned.
                            mFirebaseRemoteConfig.activateFetched();
                            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                            SharedPreferences.Editor editor = pref.edit();
                            editor.putString(getString(R.string.button_disabled), mFirebaseRemoteConfig.getString("button_disabled"));
                            editor.apply();
                        }
                    }
                });
                return null;
            }
        };

        asyncTask.execute();

//        return true; /* Still doing work */
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        asyncTask.cancel(true);

        return true; /* we're not done, please reschedule */
    }
}
