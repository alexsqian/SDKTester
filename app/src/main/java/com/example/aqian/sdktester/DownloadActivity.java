package com.example.aqian.sdktester;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class DownloadActivity extends AppCompatActivity {

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE };

    public final static String HOST_KEY = "host_key";
    public final static String USER_KEY = "user_key";
    public final static String PASSWORD_KEY = "password_key";
    public final static String SSH_PORT_KEY = "ssh_port_key";
    public final static String FILE_LOCATION_KEY = "file_location_key";

    private EditText mInputHost;
    private EditText mInputUser;
    private EditText mInputPassword;
    private EditText mInputSshPort;
    private EditText mInputFileLocation;
    private Button mButtonDownload;

    private View.OnFocusChangeListener mFocusListener = new View.OnFocusChangeListener()
    {
        @Override
        public void onFocusChange (View v, boolean hasFocus)
        {
            if (!hasFocus && v instanceof EditText)
                saveEditTextValue((EditText) v);
        }
    };

    //Text Watchers respond to the input text (main usage is to update the button once everything
    //is filled out and save the text so the user does not have to retype
    private TextWatcher hostTextWatcher = new TextWatcher()
    {
        @Override
        public void beforeTextChanged (CharSequence s, int start, int count, int after)
        {updateDownloadButton();}

        @Override
        public void onTextChanged (CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged (Editable s)
        {saveEditTextValue(mInputHost);updateDownloadButton();}
    };

    private TextWatcher userTextWatcher = new TextWatcher()
    {
        @Override
        public void beforeTextChanged (CharSequence s, int start, int count, int after)
        {updateDownloadButton();}

        @Override
        public void onTextChanged (CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged (Editable s)
        {saveEditTextValue(mInputUser);updateDownloadButton();}
    };

    private TextWatcher passwordTextWatcher = new TextWatcher()
    {
        @Override
        public void beforeTextChanged (CharSequence s, int start, int count, int after)
        {updateDownloadButton();}

        @Override
        public void onTextChanged (CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged (Editable s)
        {saveEditTextValue(mInputPassword);updateDownloadButton();}
    };

    private TextWatcher sshTextWatcher = new TextWatcher()
    {
        @Override
        public void beforeTextChanged (CharSequence s, int start, int count, int after)
        {updateDownloadButton();}

        @Override
        public void onTextChanged (CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged (Editable s)
        {saveEditTextValue(mInputSshPort);updateDownloadButton();}
    };

    private TextWatcher fileTextWatcher = new TextWatcher()
    {
        @Override
        public void beforeTextChanged (CharSequence s, int start, int count, int after)
        {updateDownloadButton();}

        @Override
        public void onTextChanged (CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged (Editable s)
        {saveEditTextValue(mInputFileLocation);updateDownloadButton();}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        bindViews();
        setupViews();
        updateDownloadButton();

    }

    //Basic initializing of the Edit Text
    private void bindViews ()
    {
        mInputHost = (EditText) findViewById(R.id.input_download_host);
        mInputUser = (EditText) findViewById(R.id.input_download_user);
        mInputPassword = (EditText) findViewById(R.id.input_download_password);
        mInputSshPort = (EditText) findViewById(R.id.input_download_ssh_port);
        mInputFileLocation = (EditText) findViewById(R.id.input_download_file_location);
        mButtonDownload = (Button) findViewById(R.id.button_download);
    }

    //Sets up input text to respond dynamically to changes made by the user
    private void setupViews () {
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);

        //Saves/sets the text so user does not have to retype
        mInputHost.setText(preferences.getString(HOST_KEY, ""));
        mInputUser.setText(preferences.getString(USER_KEY, ""));
        mInputPassword.setText(preferences.getString(PASSWORD_KEY, ""));
        mInputSshPort.setText(preferences.getString(SSH_PORT_KEY, ""));
        mInputFileLocation.setText(preferences.getString(FILE_LOCATION_KEY, ""));

        //Registers the user has input text and then sets it up to be saved
        mInputHost.setOnFocusChangeListener(mFocusListener);
        mInputUser.setOnFocusChangeListener(mFocusListener);
        mInputPassword.setOnFocusChangeListener(mFocusListener);
        mInputSshPort.setOnFocusChangeListener(mFocusListener);
        mInputFileLocation.setOnFocusChangeListener(mFocusListener);

        mInputHost.addTextChangedListener(hostTextWatcher);
        mInputUser.addTextChangedListener(userTextWatcher);
        mInputPassword.addTextChangedListener(passwordTextWatcher);
        mInputSshPort.addTextChangedListener(sshTextWatcher);
        mInputFileLocation.addTextChangedListener(fileTextWatcher);

        mButtonDownload.setActivated(false);
        mButtonDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyStoragePermissions(DownloadActivity.this);
                Intent intent = new Intent(DownloadActivity.this, DownloadService.class);
                intent.putExtra(HOST_KEY, mInputHost.getText().toString());
                intent.putExtra(USER_KEY, mInputUser.getText().toString());
                intent.putExtra(PASSWORD_KEY, mInputPassword.getText().toString());
                intent.putExtra(FILE_LOCATION_KEY, mInputFileLocation.getText().toString());
                String sshPortString = mInputSshPort.getText().toString();
                if (!TextUtils.isEmpty(sshPortString))
                    intent.putExtra(SSH_PORT_KEY, Integer.valueOf(sshPortString));

                startService(intent);

                Toast.makeText(DownloadActivity.this, "Sent to DownloadService's queue", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //Saves the string in the text line for future convenience
    private void saveEditTextValue (EditText editText)
    {
        SharedPreferences.Editor preferencesEditor = getPreferences(Context.MODE_PRIVATE).edit();
        String key;

        switch (editText.getId())
        {
            case R.id.input_download_host:
                key = HOST_KEY;
                break;
            case R.id.input_download_user:
                key = USER_KEY;
                break;
            case R.id.input_download_password:
                key = PASSWORD_KEY;
                break;
            case R.id.input_download_ssh_port:
                key = SSH_PORT_KEY;
                break;
            case R.id.input_download_file_location:
                key = FILE_LOCATION_KEY;
                break;
            default:
                throw new IllegalArgumentException("Could not find a view matching the one passed as an argument");
        }

        preferencesEditor.putString(key, editText.getText().toString()).commit();
    }

    //Makes the download button pressable once all fields have been filled out
    private void updateDownloadButton()
    {
        if (!TextUtils.isEmpty(mInputHost.getText().toString()) && !TextUtils.isEmpty(mInputUser.getText().toString())
                && !TextUtils.isEmpty(mInputPassword.getText().toString()) && !TextUtils.isEmpty(mInputFileLocation.getText().toString()))
            mButtonDownload.setEnabled(true);
        else
            mButtonDownload.setEnabled(false);
    }

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        updateDownloadButton();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
}
