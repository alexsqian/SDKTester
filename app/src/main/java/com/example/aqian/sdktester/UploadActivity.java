package com.example.aqian.sdktester;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
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
import android.widget.TextView;
import android.widget.Toast;

public class UploadActivity extends AppCompatActivity
{
    public final static String HOST_KEY = "host_key";
    public final static String USER_KEY = "user_key";
    public final static String DESTINATION_PATH_KEY = "destination_path_key";
    public final static String PASSWORD_KEY = "password_key";
    public final static String SSH_PORT_KEY = "ssh_port_key";
    public final static String FILE_URI_KEY = "file_uri_key";


    private EditText mInputHost;
    private EditText mInputDestinationPath;
    private EditText mInputUser;
    private EditText mInputPassword;
    private EditText mInputSshPort;
    private Button mButtonPick;
    private TextView mTextFileSelect;
    private Button mButtonSend;
    private Uri mFileUri = null;

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
        {
            updateUploadButton();}

        @Override
        public void onTextChanged (CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged (Editable s)
        {saveEditTextValue(mInputHost);
            updateUploadButton();}
    };

    private TextWatcher userTextWatcher = new TextWatcher()
    {
        @Override
        public void beforeTextChanged (CharSequence s, int start, int count, int after)
        {
            updateUploadButton();}

        @Override
        public void onTextChanged (CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged (Editable s)
        {saveEditTextValue(mInputUser);
            updateUploadButton();}
    };

    private TextWatcher passwordTextWatcher = new TextWatcher()
    {
        @Override
        public void beforeTextChanged (CharSequence s, int start, int count, int after)
        {
            updateUploadButton();}

        @Override
        public void onTextChanged (CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged (Editable s)
        {saveEditTextValue(mInputPassword);
            updateUploadButton();}
    };

    private TextWatcher sshTextWatcher = new TextWatcher()
    {
        @Override
        public void beforeTextChanged (CharSequence s, int start, int count, int after)
        {
            updateUploadButton();}

        @Override
        public void onTextChanged (CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged (Editable s)
        {saveEditTextValue(mInputSshPort);
            updateUploadButton();}
    };

    private TextWatcher destinationTextWatcher = new TextWatcher()
    {
        @Override
        public void beforeTextChanged (CharSequence s, int start, int count, int after)
        {
            updateUploadButton();}

        @Override
        public void onTextChanged (CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged (Editable s)
        {saveEditTextValue(mInputDestinationPath);
            updateUploadButton();}
    };

    @Override
    protected void onCreate (Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        bindViews();
        setupViews();
    }

    //Basic initializing of the Edit Text
    private void bindViews ()
    {
        mInputHost = (EditText) findViewById(R.id.input_host);
        mInputDestinationPath = (EditText) findViewById(R.id.input_destination);
        mInputUser = (EditText) findViewById(R.id.input_user);
        mInputPassword = (EditText) findViewById(R.id.input_password);
        mInputSshPort = (EditText) findViewById(R.id.input_ssh_port);
        mButtonPick = (Button) findViewById(R.id.button_pick);
        mTextFileSelect = (TextView) findViewById(R.id.text_file_selected);
        mButtonSend = (Button) findViewById(R.id.button_send);
    }

    //Sets up input text to respond dynamically to changes made by the user
    private void setupViews ()
    {
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);

        //Saves/sets the text so user does not have to retype
        mInputHost.setText(preferences.getString(HOST_KEY, ""));
        mInputDestinationPath.setText(preferences.getString(DESTINATION_PATH_KEY, ""));
        mInputUser.setText(preferences.getString(USER_KEY, ""));
        mInputPassword.setText(preferences.getString(PASSWORD_KEY, ""));
        mInputSshPort.setText(preferences.getString(SSH_PORT_KEY, ""));

        //Registers the user has input text and then sets it up to be saved
        mInputHost.setOnFocusChangeListener(mFocusListener);
        mInputDestinationPath.setOnFocusChangeListener(mFocusListener);
        mInputUser.setOnFocusChangeListener(mFocusListener);
        mInputPassword.setOnFocusChangeListener(mFocusListener);
        mInputSshPort.setOnFocusChangeListener(mFocusListener);

        mInputHost.addTextChangedListener(hostTextWatcher);
        mInputDestinationPath.addTextChangedListener(destinationTextWatcher);
        mInputUser.addTextChangedListener(userTextWatcher);
        mInputPassword.addTextChangedListener(passwordTextWatcher);
        mInputSshPort.addTextChangedListener(sshTextWatcher);

        //Choose the file to upload
        mButtonPick.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick (View v)
            {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                startActivityForResult(intent, 1);
            }
        });

        mButtonSend.setActivated(false);
        mButtonSend.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick (View v)
            {
                Intent intent = new Intent(UploadActivity.this, UploadService.class);
                intent.putExtra(HOST_KEY, mInputHost.getText().toString());
                intent.putExtra(DESTINATION_PATH_KEY, mInputDestinationPath.getText().toString());
                intent.putExtra(USER_KEY, mInputUser.getText().toString());
                intent.putExtra(PASSWORD_KEY, mInputPassword.getText().toString());
                String sshPortString = mInputSshPort.getText().toString();
                if (!TextUtils.isEmpty(sshPortString))
                    intent.putExtra(SSH_PORT_KEY, Integer.valueOf(sshPortString));
                intent.putExtra(FILE_URI_KEY, mFileUri.toString());

                startService(intent);

                Toast.makeText(UploadActivity.this, "Sent to UploadService's queue", Toast.LENGTH_SHORT).show();
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
            case R.id.input_host:
                key = HOST_KEY;
                break;
            case R.id.input_destination:
                key = DESTINATION_PATH_KEY;
                break;
            case R.id.input_user:
                key = USER_KEY;
                break;
            case R.id.input_password:
                key = PASSWORD_KEY;
                break;
            case R.id.input_ssh_port:
                key = SSH_PORT_KEY;
                break;
            default:
                throw new IllegalArgumentException("Could not find a view matching the one passed as an argument");
        }

        preferencesEditor.putString(key, editText.getText().toString()).commit();
    }

    //Makes the upload button pressable once all fields have been filled out
    private void updateUploadButton()
    {
        if (!TextUtils.isEmpty(mInputHost.getText().toString()) && !TextUtils.isEmpty(mInputUser.getText().toString()) && !TextUtils.isEmpty(mInputPassword.getText().toString()) && mFileUri != null)
            mButtonSend.setEnabled(true);
        else
            mButtonSend.setEnabled(false);
    }

    private void updateTextSelectedFile ()
    {
        if (mFileUri != null)
            mTextFileSelect.setText("File selected: " + mFileUri.toString());
        else
            mTextFileSelect.setText("No file selected");
    }

    //Checks if a file was actually selected, and if so update the file name and upload button
    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data)
    {
        if (requestCode == 1 && resultCode == Activity.RESULT_OK)
        {
            if (data != null)
            {
                Uri uri = data.getData();

                if (uri != null)
                {
                    mFileUri = uri;
                    updateTextSelectedFile();
                    updateUploadButton();
                }
            }
        }
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
