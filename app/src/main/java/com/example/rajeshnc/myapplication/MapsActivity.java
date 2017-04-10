package com.example.rajeshnc.myapplication;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.net.Uri;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiActivity;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.client.methods.HttpPost;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URL;
import java.nio.DoubleBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {


    private GoogleMap mMap;
    static String Response = null;
    Location location;
    double latitude;
    double longitude;
    SQLiteDatabase db;
    String contactName = null;
    private static final int REQUEST_CODE_PICK_CONTACTS = 1, PERMISSION_LOCATION = 1, PERMISSION_CONTACTS = 1, PERMISSIONS_MULTIPLE_REQUEST = 123;
    private Uri uriContact;
    private String contactID, userName, passWord;     // contacts unique ID
    private String TAG = MapsActivity.class.getSimpleName();
    private static String url = "http://gpslocatorapp.azurewebsites.net/api/GetFriends/";
    private static String urlPostFriend = "http://gpslocatorapp.azurewebsites.net/api/Friend/";
    private static String urlDeleteFriend = "http://gpslocatorapp.azurewebsites.net/api/DeleteFriend/";
    private static String urlUpdateContact = "http://gpslocatorapp.azurewebsites.net/api/Contacts/";
    private ProgressDialog pDialog;
    int id, callingMethod, deleteFriendID;
    GoogleApiClient mGoogleApiClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        //new GetContacts().execute();
//        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestEmail()
//                .build();
//        mGoogleApiClient = new GoogleApiClient.Builder(this)
//                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
//                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
//                .build();
        String id1 = getIntent().getStringExtra("ContactID").replace("true", "").replace("\n", "");
        userName = getIntent().getStringExtra("Self");
        passWord = getIntent().getStringExtra("Zip");
        id1 = id1.replace("\"", "");
        Intent i = getIntent();
        id = Integer.parseInt(id1);

        SignInButton signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        findViewById(R.id.sign_in_button).setOnClickListener(this);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        try {
            this.exportDatabse("Friends");
        } catch (Exception e) {
            Toast.makeText(this, "Ex:" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        db = openOrCreateDatabase("Friends", Context.MODE_PRIVATE, null);

        db.execSQL("CREATE TABLE IF NOT EXISTS Friends(id INTEGER PRIMARY KEY AUTOINCREMENT,name VARCHAR,contact VARCHAR,long VARCHAR, lat VARCHAR, URI VARCHAR);");
        db.close();

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        callAsynchronousTask(googleMap);
    }

    //Code to add retrieve phone GPS location

    public void findLatLng() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, false);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            if (ActivityCompat.shouldShowRequestPermissionRationale(MapsActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(MapsActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_LOCATION
                );
                ActivityCompat.requestPermissions(MapsActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_LOCATION
                );

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            Location location = locationManager.getLastKnownLocation(provider);
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            latitude = location.getLatitude();
            longitude = location.getLongitude();

        }
    }

    //Code to add perform action listener when contat is selected and then it will trigger name, phone and photo methods to retrieve


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PICK_CONTACTS && resultCode == RESULT_OK) {
            uriContact = data.getData();
            String id, name = "", phone = "", hasPhone = "";
            int idx;
            Cursor cursor = getContentResolver().query(uriContact, null, null, null, null);
            if (cursor.moveToFirst()) {
                idx = cursor.getColumnIndex(ContactsContract.Contacts._ID);
                id = cursor.getString(idx);

                idx = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
                name = cursor.getString(idx);

                idx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                hasPhone = cursor.getString(idx);

                idx = cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI);
                phone = cursor.getString(idx);
            }
            JSONObject j = new JSONObject();
            try {
                j.put("Self", hasPhone.replace("+91", ""));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            callingMethod = 1;
            String s = null;
            try {
                s = new GetContacts().execute(j).get();
                s = s.replace("", "");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            if (s.contains("Friend is not using Geo Locator app")) {
                Toast.makeText(getApplicationContext(), "Friend is not using Geo Locator app", Toast.LENGTH_LONG).show();
            } else if (s.contains("Friend is already in friends list")) {
                Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
            } else {
                JSONObject j1 = null;
                try {
                    j1 = new JSONObject(s);
                    String lon = Double.toString(longitude);
                    String lat = Double.toString(latitude);
                    String query = "INSERT INTO Friends (name,contact,long,lat,URI) VALUES('" + j1.getString("FriendId")+","+j1.getString("ID") + "','" + hasPhone + "','" + lon + "','" + lat + "','" + uriContact + "');";
                    if(!db.isOpen()) {
                        db=openOrCreateDatabase("Friends", Context.MODE_PRIVATE, null);
                    }
                    db.execSQL(query);
                    mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(lat), Double.parseDouble(lon))).title(name).icon(BitmapDescriptorFactory.fromBitmap(((BitmapDrawable) getDrawable(phone)).getBitmap()))).setTag(uriContact + "," +j1.getString("FriendId")+","+j1.getString("ID"));
                    Toast.makeText(getApplicationContext(), name + " Added to friends list", Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                finally{
                    cursor.close();
                    db.close();
                }
            }
            cursor.close();

        }
        if (requestCode == 2) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            //mStatusTextView.setText(getString(R.string.signed_in_fmt, acct.getDisplayName()));
            // updateUI(true);
        } else {
            // Signed out, show unauthenticated UI.
            //   updateUI(false);
        }
    }


    //Code to add retrieve photo from phone contacts

    private void retrieveContactPhoto() {

        Bitmap photo = null;

        try {
            InputStream inputStream = ContactsContract.Contacts.openContactPhotoInputStream(getContentResolver(),
                    ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, new Long(contactID)));

            if (inputStream != null) {
                photo = BitmapFactory.decodeStream(inputStream);
                ImageView imageView = (ImageView) findViewById(R.id.img_contact);
                imageView.setImageBitmap(photo);
            }

            assert inputStream != null;
            inputStream.close();

            // Toast.makeText(getApplicationContext(),"here is the photo phone",Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    //Code to add retrieve contact number from phone contacts

    private void retrieveContactNumber() {

        String contactNumber = null;

        // getting contacts ID
        Cursor cursorID = getContentResolver().query(uriContact,
                new String[]{ContactsContract.Contacts._ID},
                null, null, null);

        if (cursorID.moveToFirst()) {

            contactID = cursorID.getString(cursorID.getColumnIndex(ContactsContract.Contacts._ID));
        }



        // Log.d(TAG, "Contact ID: " + contactID);

        // Using the contact ID now we will get contact phone number
        Cursor cursorPhone = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},

                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? AND " +
                        ContactsContract.CommonDataKinds.Phone.TYPE + " = " +
                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE,

                new String[]{contactID},
                null);

        if (cursorPhone.moveToFirst()) {
            contactNumber = cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
        }

        cursorPhone.close();
        // Toast.makeText(getApplicationContext(),contactID+"here is the phone",Toast.LENGTH_SHORT).show();
        // Log.d(TAG, "Contact Phone Number: " + contactNumber);
        cursorID.close();
    }

    //Code to add retrieve contact names from phone contacts

    private void retrieveContactName() {
        // querying contact data store
        Cursor cursor = getContentResolver().query(uriContact, null, null, null, null);

        if (cursor.moveToFirst()) {

            // DISPLAY_NAME = The display name for the contact.
            // HAS_PHONE_NUMBER =   An indicator of whether this contact has at least one phone number.

            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            //  Toast.makeText(getApplicationContext(),contactName+" here is the count ",Toast.LENGTH_SHORT).show();
        }

        cursor.close();

        //   Log.d(TAG, "Contact Name: " + contactName);

    }


    //Code to add contacts to app db from main phone contacts

    public void addContact(LatLng a) {
        Intent Kintent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        Kintent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
        latitude = a.latitude;
        longitude = a.longitude;
        startActivityForResult(Kintent, REQUEST_CODE_PICK_CONTACTS);
    }


    //Code to retreive contacts from the app db not from main phone contacts

    public void viewContact() {
        if (!db.isOpen())
        {
            db=openOrCreateDatabase("Friends", Context.MODE_PRIVATE, null);
        }
        Cursor c = db.rawQuery("SELECT * FROM friends", null);

        if (c.getCount() == 0) {
            return;
        }
        StringBuffer buffer = new StringBuffer();
        while (c.moveToNext()) {
            //Toast.makeText(getApplicationContext(), c.getString(0) + "viewcontact", Toast.LENGTH_SHORT).show();

        }
c.close();;
        db.close();

    }

    //Code to delete contacts from the app not from main phone contacts

    public void deleteContact() {
        if (!db.isOpen())
        {
            db=openOrCreateDatabase("Friends", Context.MODE_PRIVATE, null);
        }
        db.execSQL("DELETE FROM student WHERE name='" + contactName + "'");
db.close();
        //  Toast.makeText(getApplicationContext(),contactName+"deleted",Toast.LENGTH_SHORT).show();

    }

    private static void copyFileUsingStream(File source, File dest) throws IOException {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(source);
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } finally {
            is.close();
            os.close();
        }
    }

    public void exportDatabse(String databaseName) {
        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) {
                String currentDBPath = "//data//" + this.getPackageName() + "//databases//" + databaseName + "";
                String backupDBPath = "backupname.db";
                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(sd, backupDBPath);

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    //  Toast.makeText(this,"Ex:"+backupDB.getPath(),Toast.LENGTH_SHORT).show();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Ex:" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private Uri getPhotoUriFromID(String id) {
        try {
            Cursor cur = getContentResolver()
                    .query(ContactsContract.Data.CONTENT_URI,
                            null,
                            ContactsContract.Data.CONTACT_ID
                                    + "="
                                    + id
                                    + " AND "
                                    + ContactsContract.Data.MIMETYPE
                                    + "='"
                                    + ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE
                                    + "'", null, null);
            if (cur != null) {
                if (!cur.moveToFirst()) {
                    return null; // no photo
                }
            } else {
                return null; // error in cursor process
            }
            cur.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;

        }

        Uri person = ContentUris.withAppendedId(
                ContactsContract.Contacts.CONTENT_URI, Long.parseLong(id));
        return Uri.withAppendedPath(person,
                ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
    }

    Drawable getDrawable(String uri) {
        Drawable d;
        try {
            InputStream inputStream = getContentResolver().openInputStream(Uri.parse(uri.toString()));
            d = Drawable.createFromStream(inputStream, uri.toString());
        } catch (Exception e) {
            d = getResources().getDrawable(R.mipmap.ic_launcher_round);
        }
        return d;
    }

    public void deleteContact(String phoneNumber, String name) {
        String s = null;
        try {
            callingMethod = 3;
            deleteFriendID = Integer.parseInt(phoneNumber.split(",")[2]);
            s = new GetContacts().execute().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        if (s.contains("deleted")) {
            String query = "delete from Friends where name like'%," + phoneNumber.split(",")[2] + "';";
            if (!db.isOpen())
            {
                db=openOrCreateDatabase("Friends", Context.MODE_PRIVATE, null);
            }
            db.execSQL(query);
            mMap.clear();
            onMapReady(mMap);
            Toast.makeText(getApplicationContext(), name + ":" + phoneNumber + " Deleted from friends list", Toast.LENGTH_SHORT).show();
            db.close();;
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
            // ...
        }
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, 2);
    }


    private class GetContacts extends AsyncTask<JSONObject, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
//            pDialog = new ProgressDialog(MapsActivity.this);
//            pDialog.setMessage("Please wait...");
//            pDialog.setCancelable(false);
//            pDialog.show();
        }

        @Override
        protected String doInBackground(JSONObject... arg0) {
            HttpHandler sh = new HttpHandler();
            // Making a request to url and getting response
            String jsonStr = null;
            if (callingMethod == 2) {
                jsonStr = sh.makeServiceCall(url + id);
            }
            if (callingMethod == 1) {
                jsonStr = sh.makeServiceCall(urlPostFriend + id, arg0[0]);
            }
            if (callingMethod == 3) {
                jsonStr = sh.makeServiceCallToDelete(urlDeleteFriend + deleteFriendID);
            }
            if (callingMethod == 4) {
                jsonStr = sh.makeServiceCallToUpdate(urlUpdateContact + id, arg0[0]);
            }
            return jsonStr;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
//            if (pDialog.isShowing())
//                pDialog.dismiss();
            ;
//            startActivity(new Intent( ResgistrationActivity.this,LoginActivity.class));
//            finish();
            /**
             * Updating parsed JSON data into ListView
             * */
        }

    }

    public void callAsynchronousTask(final GoogleMap googleMap) {
        final Handler handler = new Handler();
        Timer timer = new Timer();
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                            Criteria criteria = new Criteria();
                            String provider = locationManager.getBestProvider(criteria, false);

                            if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                // TODO: Consider calling
                                //    ActivityCompat#requestPermissions
                                // here to request the missing permissions, and then overriding
                                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                //                                          int[] grantResults)
                                // to handle the case where the user grants the permission. See the documentation
                                // for ActivityCompat#requestPermissions for more details.
                                if (ActivityCompat.shouldShowRequestPermissionRationale(MapsActivity.this,
                                        Manifest.permission.ACCESS_FINE_LOCATION)) {

                                    // Show an explanation to the user *asynchronously* -- don't block
                                    // this thread waiting for the user's response! After the user
                                    // sees the explanation, try again to request the permission.

                                } else {

                                    // No explanation needed, we can request the permission.
                                    ActivityCompat.requestPermissions(MapsActivity.this,
                                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_LOCATION
                                    );
                                    ActivityCompat.requestPermissions(MapsActivity.this,
                                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_LOCATION
                                    );

                                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                                    // app-defined int constant. The callback method gets the
                                    // result of the request.
                                }
                            } else {
                                Location location = locationManager.getLastKnownLocation(provider);
                                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                                JSONObject j = new JSONObject();
                                try {
                                    j.put("ContactId", id);
                                    j.put("Zip", passWord);
                                    j.put("Self", userName);
                                    j.put("Lattitude", latitude);
                                    j.put("Longitude", longitude);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                callingMethod = 4;
                                try {
                                    Response = new GetContacts().execute(j).get();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                } catch (ExecutionException e) {
                                    e.printStackTrace();
                                }
                                Log.d("Response", Response);
                                Toast.makeText(getApplicationContext(), Response, Toast.LENGTH_SHORT).show();
                                updateFriendsLocation(googleMap);
                            }
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                });
            }
        };
        timer.schedule(doAsynchronousTask, 0, 10000); //execute in every 50000 ms
    }
    public void updateFriendsLocation(GoogleMap googleMap)
    {
        if (!db.isOpen())
        {
            db=openOrCreateDatabase("Friends", Context.MODE_PRIVATE, null);
        }
        if(mMap!=null) {
            mMap.clear();
        }
        findLatLng();
        mMap = googleMap;
        mMap.setMaxZoomPreference(14.0f);
        LatLng myLoc = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions().position(myLoc).title("My Location")).setTag("MyContact");
          int sum = 0;
        String Response=null;
        JSONArray jar=null;
        try {
            callingMethod=2;
            Response= new GetContacts().execute().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        try {
            jar=new JSONArray(Response);


        for (int i=0;i<jar.length();i++)
        {

            String qry="select * from Friends where name like'"+jar.getJSONObject(i).getString("ContactId")+",%';";
            Log.d("Quey",qry);
            Cursor cursor = db.rawQuery(qry, null);

            for(cursor.moveToFirst();!cursor.isAfterLast(); cursor.moveToNext()) {
                LatLng l = new LatLng(cursor.getDouble(4), cursor.getDouble(3));

                String id, name = "", phone = "", hasPhone = "";
                int idx;
                Cursor cursorContact = getContentResolver().query(Uri.parse(cursor.getString(5)), null, null, null, null);
                if (cursorContact.moveToFirst()) {
                    idx = cursorContact.getColumnIndex(ContactsContract.Contacts._ID);
                    id = cursorContact.getString(idx);

                    idx = cursorContact.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
                    name = cursorContact.getString(idx);

                    idx = cursorContact.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI);
                    hasPhone = cursorContact.getString(idx);

                }
                mMap.addMarker(new MarkerOptions().position(l).title("FriendLocation").icon(BitmapDescriptorFactory.fromBitmap(((BitmapDrawable) getDrawable(hasPhone)).getBitmap()))).setTag(cursor.getString(5) + "," + cursor.getString(1));
            }
            cursor.close();
        }
        } catch (JSONException e) {
            e.printStackTrace();
        }


        //      copyFileUsingStream();

        mMap.moveCamera(CameraUpdateFactory.newLatLng(myLoc));

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLoc, 18.5f));


        //Code to add activate maplong listner currently to invoke phone contact book

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {

            public static final int PICK_CONTACT = 0;

            @Override
            public void onMapLongClick(LatLng arg0) {
                addContact(arg0);
            }
        });

        //Code to add perform SMS/Phone options on when marker is selected
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {


            public boolean onMarkerClick(final Marker marker) {
                if (marker.getTag().toString().equals("MyContact")) {
                    Toast.makeText(MapsActivity.this, "Please select a Friends Contacts to Open", Toast.LENGTH_SHORT).show();
                } else {
                    final Dialog dialog = new Dialog(MapsActivity.this);
                    dialog.setTitle("Title...");
                    dialog.setContentView(R.layout.dialoglayout);
                    Button Open = (Button) dialog.findViewById(R.id.open);
                    Button Delete = (Button) dialog.findViewById(R.id.delete);
                    Open.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (ContextCompat.checkSelfPermission(MapsActivity.this,
                                    Manifest.permission.READ_CONTACTS)
                                    != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(MapsActivity.this,
                                    Manifest.permission.WRITE_CONTACTS)
                                    != PackageManager.PERMISSION_GRANTED) {
                                if (ActivityCompat.shouldShowRequestPermissionRationale(MapsActivity.this,
                                        Manifest.permission.READ_CONTACTS) && ActivityCompat.shouldShowRequestPermissionRationale(MapsActivity.this,
                                        Manifest.permission.WRITE_CONTACTS)) {

                                    // Show an explanation to the user *asynchronously* -- don't block
                                    // this thread waiting for the user's response! After the user
                                    // sees the explanation, try again to request the permission.

                                } else {
                                    ActivityCompat.requestPermissions(MapsActivity.this,
                                            new String[]{Manifest.permission.READ_CONTACTS}, PERMISSION_CONTACTS
                                    );
                                    ActivityCompat.requestPermissions(MapsActivity.this,
                                            new String[]{Manifest.permission.WRITE_CONTACTS}, PERMISSION_CONTACTS
                                    );
                                }
                            } else {
                                Intent callIntent = new Intent(Intent.ACTION_VIEW);
                                if (callIntent.resolveActivity(getPackageManager()) != null) {

                                    int phoneContactID = new Random().nextInt();
                                    if (!marker.getTag().toString().equals("MyContact")) {
                                        Cursor contactLookupCursor = MapsActivity.this.getContentResolver().query(Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(marker.getTag().toString().split("'")[0])), new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup._ID}, null, null, null);
                                        while (contactLookupCursor.moveToNext()) {
                                            phoneContactID = contactLookupCursor.getInt(contactLookupCursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup._ID));
                                        }
                                        Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, String.valueOf(phoneContactID));
                                        callIntent.setData(uri);
                                        startActivity(callIntent);
                                        dialog.dismiss();
                                        contactLookupCursor.close();;
                                    } else {
                                        Toast.makeText(MapsActivity.this, "Please select a Friends Contacts to Open", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        }
                    });
                    Delete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            deleteContact(marker.getTag().toString(), "Murali");
                            dialog.dismiss();
                        }
                    });
                    dialog.show();
                }
                return true;
            }

        });
        db.close();
    }

}
