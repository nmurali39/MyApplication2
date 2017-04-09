package com.example.rajeshnc.myapplication;

import android.*;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int REQUEST_CODE_PICK_CONTACTS = 1, PERMISSION_LOCATION = 1, PERMISSION_CONTACTS = 1, PERMISSIONS_MULTIPLE_REQUEST = 123;
    private Uri uriContact;
    private String contactID;
    private ProgressDialog pDialog;
    private String TAG = MapsActivity.class.getSimpleName();
    private static String url = "http://gpslocatorapp.azurewebsites.net/api/Login";// contacts unique ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        Button btn_Login = (Button) findViewById(R.id.login_login);
        Button btn_Register = (Button) findViewById(R.id.login_register);
        btn_Register.setOnClickListener(this);
        btn_Login.setOnClickListener(this);

        if (ContextCompat.checkSelfPermission(LoginActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(LoginActivity.this, android.Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(LoginActivity.this, android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(LoginActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(LoginActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(LoginActivity.this, android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(LoginActivity.this,
                    new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_CONTACTS, android.Manifest.permission.WRITE_CONTACTS, android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.READ_PHONE_STATE, android.Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSIONS_MULTIPLE_REQUEST
            );
        }
    }
        @Override
        public void onClick (View v){
            switch (v.getId()) {
                case R.id.login_register:
                    Intent i = new Intent();
                    i.setClass(getApplicationContext(), ResgistrationActivity.class);
                    startActivity(i);
                    break;
                case R.id.login_login:
                    String userName=((EditText)findViewById(R.id.login_uname)).getText().toString();
                    String passWord=((EditText)findViewById(R.id.login_pwd)).getText().toString();
                    JSONObject j =new JSONObject();
                    String s=null;
                    try {
                        j.put("UserName",userName);
                        j.put("Password",passWord);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    try {
                        s=new GetContacts().execute(j).get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                    if(s.contains("true")) {
                        startActivity(new Intent(this,MapsActivity.class).putExtra("ContactID",s));
                    }
                    else {
                        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
                    }
                    break;
            }
        }

    private class GetContacts extends AsyncTask<JSONObject, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(LoginActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(JSONObject... arg0) {
            HttpHandler sh = new HttpHandler();
            // Making a request to url and getting response
            String jsonStr = sh. makeServiceCall(url,arg0[0]);
            return jsonStr;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();
//            startActivity(new Intent( ResgistrationActivity.this,LoginActivity.class));
//            finish();
            /**
             * Updating parsed JSON data into ListView
             * */
        }

    }
    }
