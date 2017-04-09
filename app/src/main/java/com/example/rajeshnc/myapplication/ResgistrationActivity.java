package com.example.rajeshnc.myapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

public class ResgistrationActivity extends AppCompatActivity implements View.OnClickListener{
    private ProgressDialog pDialog;
    private String TAG = MapsActivity.class.getSimpleName();
    private static String url = "http://gpslocatorapp.azurewebsites.net/api/Contacts";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);
        Button mRegBtn=(Button)findViewById(R.id.reg_register);
        mRegBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.reg_register:
                String name=((EditText)findViewById(R.id.reg_name)).getText().toString();
                String mobNumber=((EditText)findViewById(R.id.reg_cn)).getText().toString();
                String password=((EditText)findViewById(R.id.reg_password)).getText().toString();
                JSONObject j=new JSONObject();
                try {
                    j.put("Name",name);
                    j.put("Self",mobNumber);
                    j.put("Zip",password);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String s=null;
                try {
                     s=new GetContacts().execute(j).get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                if(s.equals("This mobile is already registered"))
                {
                    Toast.makeText(getApplicationContext(),"This mobile is already registered\nPlease try using different mobile number",Toast.LENGTH_LONG).show(); ;
                }
                else{
                    Toast.makeText(getApplicationContext(),"Successfully registered",Toast.LENGTH_LONG).show();
                    startActivity(new Intent(this,LoginActivity.class));
                    finish();
                }

                break;
        }
    }


    private class GetContacts extends AsyncTask<JSONObject, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(ResgistrationActivity.this);
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
