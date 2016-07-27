
package com.example.aqian.sdktester;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.OpenableColumns;
import android.util.Log;
import android.widget.Toast;

import com.asperasoft.mobile.AbstractFaspSession;
import com.asperasoft.mobile.FaspSession;
import com.asperasoft.mobile.FaspSessionListener;
import com.asperasoft.mobile.FaspSessionParameters;
import com.asperasoft.mobile.FaspSessionState;
import com.asperasoft.mobile.FaspSessionStats;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class UploadService extends IntentService
{
    private final static String TAG = "UploadService";
    private FaspSession currentSession = null;

    public UploadService ()
    {
        super("UploadService");
    }

    @Override
    protected void onHandleIntent (Intent intent)
    {
        SharedPreferences sharedPrefs =
                PreferenceManager.getDefaultSharedPreferences(this);

        if (intent.getExtras() != null)
        {
            // Get data from intent extras

            String host = intent.getStringExtra(UploadActivity.HOST_KEY);
            String user = intent.getStringExtra(UploadActivity.USER_KEY);
            String destinationPath = intent.getStringExtra(UploadActivity.DESTINATION_PATH_KEY);
            String password = intent.getStringExtra(UploadActivity.PASSWORD_KEY);
            int sshPort = intent.getIntExtra(UploadActivity.SSH_PORT_KEY, 22);
            int targetRate = Integer.parseInt(sharedPrefs.getString(
                    getString(R.string.pref_target_rate_key),
                    getString(R.string.pref_target_rate_default)));
            Uri fileUri = Uri.parse(intent.getStringExtra(UploadActivity.FILE_URI_KEY));

            Log.i(TAG, "Intent received with Uri: " + fileUri.toString());

            // Get file from Uri and save it in a temporary folder

            String state = Environment.getExternalStorageState();
            if (Environment.MEDIA_MOUNTED.equals(state))
            {
                File externalCacheDir = getExternalCacheDir();
                File tempLocalFile = saveUriAsFile(fileUri, externalCacheDir);

                // Make sure we have a file and it exists

                if (tempLocalFile != null && tempLocalFile.exists())
                {
                    // Create the parameters of the FaspSession
                    FaspSessionParameters params = FaspSessionParameters.Builder.upload(host, user)
                            .withPassword(password)
                            .withSshPort(sshPort)
                            .withDestinationPath(destinationPath)
                            .withInitialTargetRate(targetRate)
                            .build();

                    // Create the FaspSession

                    currentSession = new FaspSession(params, tempLocalFile.getAbsolutePath());
                    currentSession.addListener(callbacks);

                    // Start the transfer

                    currentSession.start();
                }
                else
                {
                    Log.e(TAG, "Could not open the file with Uri: " + fileUri.toString());
                }
            }
            else
            {
                Log.e(TAG, "External storage is not accessible");
            }
        }
    }

    /**
     * Get the name of the file through the content provider. Save the file under this name in the directory
     *
     * @param uri       uri of the file we want to upload
     * @param directory folder where the file is saved
     * @return the name of the file
     */
    private File saveUriAsFile (Uri uri, File directory)
    {
        // Query a cursor from the content provider for the specified uri

        Cursor cursor = getContentResolver().query(uri, null, null, null, null);

        try
        {
            if (cursor != null && cursor.moveToFirst())
            {
                // Get the name of the file

                String fileName = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME));

                // If no name, we abort

                if (fileName == null)
                    return null;

                // Copy the file in our temporary directory

                InputStream inputStream = getContentResolver().openInputStream(uri);

                File outputFile = new File(directory, fileName);

                Log.i(TAG, "Opening file...");

                FileOutputStream fileOutputStream = new FileOutputStream(outputFile);

                int read = 0;
                byte[] bytes = new byte[1024];

                while ((read = inputStream.read(bytes)) != -1)
                {
                    fileOutputStream.write(bytes, 0, read);
                }

                Log.i(TAG, "File opened at : " + outputFile.getAbsolutePath());

                return outputFile;
            }

            return null;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }
        catch (IllegalArgumentException e)
        {
            e.printStackTrace();
            return null;
        }
        finally
        {
            if (cursor != null)
                cursor.close();
        }
    }

    @Override
    public void onDestroy ()
    {
        super.onDestroy();

        if (currentSession != null)
            currentSession.stop();
        Toast.makeText(getApplicationContext(), "Finished Uploading", Toast.LENGTH_LONG).show();
    }

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
