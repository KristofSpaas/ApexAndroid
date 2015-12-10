package com.groep9.apex.apexandroid.Login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.groep9.apex.apexandroid.AppFunctions;
import com.groep9.apex.apexandroid.MainActivity;
import com.groep9.apex.apexandroid.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.entity.UrlEncodedFormEntity;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.message.BasicNameValuePair;
import cz.msebera.android.httpclient.protocol.HTTP;
import cz.msebera.android.httpclient.util.EntityUtils;

public class LoginActivity extends AppCompatActivity {

    private Animation animFade, animTranslate;
    private TextView logo_login;

    private EditText usernameText;
    private EditText passwordText;
    private Button loginButton;

    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);

        logo_login = (TextView) findViewById(R.id.logo_login);
        animFade = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        animTranslate = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.translate);

        loginButton = (Button) findViewById(R.id.btn_login);
        usernameText = (EditText) findViewById(R.id.input_username);
        passwordText = (EditText) findViewById(R.id.input_password);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        startAnimation();
    }

    private void startAnimation() {
        animTranslate.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation arg0) {
            }

            @Override
            public void onAnimationRepeat(Animation arg0) {
            }

            @Override
            public void onAnimationEnd(Animation arg0) {
                LinearLayout loginBox = (LinearLayout) findViewById(R.id.ll_loginbox);
                loginBox.setVisibility(View.VISIBLE);
                loginBox.startAnimation(animFade);
            }
        });

        logo_login.setVisibility(View.VISIBLE);
        logo_login.startAnimation(animFade);

        logo_login.postDelayed(new Runnable() {
            @Override
            public void run() {
                translatePositionLogo();
            }
        }, 900);
    }

    private void translatePositionLogo() {
        logo_login.startAnimation(animTranslate);
    }

    private void login() {
        if (!AppFunctions.isNetworkAvailable(getBaseContext())) {
            Toast.makeText(getBaseContext(), "Netwerk niet beschikbaar", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!validate()) {
            return;
        }

        String username = String.valueOf(usernameText.getText());
        String password = String.valueOf(passwordText.getText());

        loginButton.setEnabled(false);

        progressDialog = new ProgressDialog(LoginActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Inloggen...");
        progressDialog.show();

        new LoginTask(username, password).execute();
    }

    public boolean validate() {
        boolean valid = true;

        String username = usernameText.getText().toString();
        String password = passwordText.getText().toString();

        if (username.isEmpty()) {
            usernameText.setError("Geef een username in");
            valid = false;
        } else {
            usernameText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            passwordText.setError("Geef een password in");
            valid = false;
        } else {
            passwordText.setError(null);
        }

        return valid;
    }


    private class LoginTask extends AsyncTask<Void, Void, JSONObject> {

        private String username, password;

        public LoginTask(String username, String password) {
            this.username = username;
            this.password = password;
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            List<NameValuePair> nameValuePairs = new ArrayList<>();
            nameValuePairs.add(new BasicNameValuePair("grant_type", "password"));
            nameValuePairs.add(new BasicNameValuePair("username", username));
            nameValuePairs.add(new BasicNameValuePair("password", password));

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://apexbackend.azurewebsites.net/token");
            httppost.setHeader(HTTP.CONTENT_TYPE, "application/x-www-form-urlencoded;charset=UTF-8");

            try {
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            JSONObject json = null;

            // Execute HTTP Post Request
            try {
                HttpResponse response = httpclient.execute(httppost);
                String json_string = EntityUtils.toString(response.getEntity());
                json = new JSONObject(json_string);
                System.out.println(json);
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            return json;
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            if (json != null) {
                String KEY_ACCESS_TOKEN = "pref_access_token";
                String KEY_PATIENTID = "pref_patient_id";
                String token = "";
                String role = "";
                int patientId = 0;

                try {
                    token = json.getString("access_token");
                    role = json.getString("Role");
                    patientId = json.getInt("PatientId");
                } catch (JSONException e) {
                    e.printStackTrace();
                    loginFailed();
                }

                if (role.equals("Patient")) {
                    // Get SharedPreference file
                    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(
                            getBaseContext());

                    sharedPref.edit().putString(KEY_ACCESS_TOKEN, token).apply();
                    sharedPref.edit().putInt(KEY_PATIENTID, patientId).apply();

                    Intent intent = new Intent(getBaseContext(), MainActivity.class);
                    startActivity(intent);
                } else {
                    loginFailed();
                }
            } else {
                loginFailed();
            }
        }
    }

    private void loginFailed() {
        Toast.makeText(getBaseContext(), "Inloggen niet gelukt", Toast.LENGTH_SHORT).show();
        progressDialog.dismiss();
        loginButton.setEnabled(true);
    }
}
