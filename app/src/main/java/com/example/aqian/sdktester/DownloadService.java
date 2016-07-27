package com.example.aqian.sdktester;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.asperasoft.mobile.AbstractFaspSession;
import com.asperasoft.mobile.FaspSession;
import com.asperasoft.mobile.FaspSessionListener;
import com.asperasoft.mobile.FaspSessionParameters;
import com.asperasoft.mobile.FaspSessionState;
import com.asperasoft.mobile.FaspSessionStats;

import java.io.File;
import java.util.ArrayList;


public class DownloadService extends IntentService {

    private final static String TAG = "DownloadService";
    private FaspSession currentSession = null;
    private static String destinationPath = null;

    public DownloadService ()
    {
        super("DownloadService");
    }

    //Create the location where the file will be downloaded
    File pkgDownloads = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "AsperaDownloadedFiles");


    protected void onHandleIntent (Intent intent) {

        if (intent.getExtras() != null) {

            //Get the initial target rate and other settings
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);


            // Get data from intent extras
            String host = intent.getStringExtra(DownloadActivity.HOST_KEY);
            String user = intent.getStringExtra(DownloadActivity.USER_KEY);
            String password = intent.getStringExtra(DownloadActivity.PASSWORD_KEY);
            String fileLocation = intent.getStringExtra(DownloadActivity.FILE_LOCATION_KEY);
            int sshPort = intent.getIntExtra(DownloadActivity.SSH_PORT_KEY, 22);
            float targetRateSetting = (Float.parseFloat(sharedPrefs.getString(
                    getString(R.string.pref_target_rate_key),
                    getString(R.string.pref_target_rate_default))) * 1000000);
            long targetRate = (long) targetRateSetting;
            destinationPath = pkgDownloads.getAbsolutePath();
            pkgDownloads.mkdirs();

//            Log.i(TAG, "Target rate: " + targetRate);
            Log.i(TAG, "Intent received with file location: " + fileLocation);

            /* Checks if external storage is available for read and write */
            String state = Environment.getExternalStorageState();
            if (Environment.MEDIA_MOUNTED.equals(state)) {

                //Fill the parameters with the data from intent
                FaspSessionParameters params = FaspSessionParameters.Builder.download(host, user)
                        .withPassword(password)
                        .withSshPort(sshPort)
                        .withDestinationPath(destinationPath)
                        .withInitialTargetRate(targetRate)
                        .build();

                //Creating the FASP session, fileLocation in this case being /Upload in demo server
                ArrayList<String> sources = new ArrayList<String>();
                sources.add(fileLocation);
                FaspSession currentSession = new FaspSession(params, sources);

                //Adding a listener to the FASP session
                currentSession.addListener(callbacks);

                // Start the transfer
                currentSession.start();

                //Assigns the file locaiton to download to
                File sourceFile = new File(fileLocation);
                File downloadedFile = new File(pkgDownloads, sourceFile.getName());
                Log.i(TAG, sourceFile.getAbsolutePath());
                Log.i(TAG, downloadedFile.getAbsolutePath());

                Intent fileGetter = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                fileGetter.setData(Uri.fromFile(downloadedFile));
                sendBroadcast(fileGetter);
            }
            else
            {
                Log.e(TAG, "External storage is not accessible");
            }
        }
    }

    @Override
    public void onDestroy ()
    {
        super.onDestroy();

        if (currentSession != null)
            currentSession.stop();
        Toast.makeText(DownloadService.this, "Finished Downloading", Toast.LENGTH_LONG).show();
    }

    //Log messages and useful statistics
    private static FaspSessionListener callbacks = new FaspSessionListener()
    {
        @Override
        public void onSessionStart (AbstractFaspSession abstractFaspSession)
        {
            Log.i(TAG, "Transfer: onSessionStart");
        }

        @Override
        public void onSessionDataStart (AbstractFaspSession abstractFaspSession)
        {
            Log.i(TAG, "Transfer: onSessionDataStart");
        }

        @Override
        public void onSessionProgress (AbstractFaspSession abstractFaspSession)
        {
            Log.i(TAG, "Transfer: onSessionProgress");
            Log.i(TAG, "Downloaded dir: " + destinationPath);
            FaspSessionStats stats = abstractFaspSession.getStats();
            Log.i(TAG, " * bytes total: " + stats.getBytesTotal());
            Log.i(TAG, " * bytes written: " + stats.getBytesWritten());
            Log.i(TAG, " * bytes remaining: " + stats.getBytesRemaining());
            Log.i(TAG, " * average bits per second: " + stats.getAverageBitsPerSecond());
            Log.i(TAG, " * estimated seconds remaining: " + stats.getAverageSecondsRemaining());
        }

        @Override
        public void onFileStart (AbstractFaspSession abstractFaspSession)
        {
            Log.i(TAG, "Transfer: onFileStart");
        }

        @Override
        public void onFileStop (AbstractFaspSession abstractFaspSession)
        {
            Log.i(TAG, "Transfer: onFileStop");
        }

        @Override
        public void onSessionStopRequested (AbstractFaspSession abstractFaspSession)
        {
            Log.i(TAG, "Transfer: onSessionStopRequested");
        }

        @Override
        public void onSessionDataEnd (AbstractFaspSession abstractFaspSession)
        {
            Log.i(TAG, "Transfer: onSessionStop");

        }

        @Override
        public void onSessionEnd (AbstractFaspSession abstractFaspSession, FaspSessionState finalState)
        {

            Log.i(TAG, "Transfer: onSessionEnd: " + finalState);
            if (finalState == FaspSessionState.SUCCEEDED) {
                Log.i(TAG, "Transfer succeeded");
            }
            else if (finalState == FaspSessionState.STOPPED) {
                Log.i(TAG, "Transfer stopped");
            }
            else {
                Log.e(TAG, "Transfer failed");
            }
        }

    };

}
