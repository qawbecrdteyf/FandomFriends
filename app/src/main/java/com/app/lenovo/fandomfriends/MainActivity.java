package com.app.lenovo.fandomfriends;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private String profile = "Aditya";
    private float radius = 2;
    public static final int GET_FROM_GALLERY = 3;
    SharedPreferences sharedPreferences,sharedPreferencesProfileImage;
    String biotext = "bio";
    String bioedittext;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private final static int ACCESS_FINE_LOCATION = 0;
    private final static int ACCESS_COARSE_LOCATION = 1;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private double currentLatitude;
    int flag = 0;
    private Bitmap bitmap;
    private ProgressDialog dialog;
    private double currentLongitude;
    String name, lati, longi, fandom1, fandom2, fandom3, fandom4, fandom5;
    private static final int SIGN_IN_REQUEST_CODE = 200;
    Location location=null;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.dummy);
        resetLayout();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                // The next two lines tell the new client that “this” current class will handle connection stuff
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                //fourth line adds the LocationServices API endpoint from GooglePlayServices
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds
    }
    public void resetLayout()
    {
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences(biotext, Context.MODE_PRIVATE);
        EditText bio = (EditText) findViewById(R.id.Bio);
        bioedittext = sharedPreferences.getString("bio", "Enter your bio here");
        bio.setText(bioedittext);
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        final ImageButton edit =  findViewById(R.id.imageButton);
        edit.setOnClickListener(new View.OnClickListener() {

            EditText bio = (EditText) findViewById(R.id.Bio);

            //int flag = 1;
            @Override
            public void onClick(View view) {
                if (!bio.isEnabled()) {
                    bio.setEnabled(true);
                    bio.setCursorVisible(true);
                    bio.setFocusableInTouchMode(true);
                    edit.setImageResource(R.drawable.nrg);
                } else {
                    String bioedit = bio.getText().toString();
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(biotext, bioedit);
                    editor.commit();
                    bio.setEnabled(false);
                    bio.setCursorVisible(false);
                    bio.setFocusableInTouchMode(false);
                    edit.setImageResource(R.drawable.sm);

                }
            }
        });

        Button upload = findViewById(R.id.upload);
        upload.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), GET_FROM_GALLERY);
            }
        });


        //connectionsClient = Nearby.getConnectionsClient(this);
        //startAdvertising();
        //startDiscovery();
        Button chat = findViewById(R.id.buttonChat);
        chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //setContentView(R.layout.chatting);
                try {
                    Intent myIntent = new Intent(MainActivity.this,
                            Chatting.class);
                    startActivity(myIntent);
                } catch (Exception e) {
                    Log.e("Error", e.getMessage());
                }
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);



        //Detects request codes
        if(requestCode==GET_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            Uri selectedImage = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                CircleImageView img = findViewById(R.id.Profilepic);
                img.setImageBitmap(bitmap);

                String img_string = getStringImage(bitmap);
                Log.d("Huhu",img_string);
                UploadImage uploadImage = new UploadImage(this);
                uploadImage.execute(img_string, FirebaseAuth.getInstance().getCurrentUser().getDisplayName().replaceAll("\\s", "").toLowerCase());

            } catch (FileNotFoundException e) {

                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String getStringImage(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }

    public void StartIt(View view)
    {
        String method = "find_friends";
        BackgroundTask backgroundTask = new BackgroundTask(this);
        backgroundTask.execute(method);

    }

    public void btRegister(View view) {
        /*first_name =  ET_FIRST_NAME.getText().toString();
        last_name = ET_LAST_NAME.getText().toString();
        address = ET_ADDRESS.getText().toString();
        email = ET_EMAIL.getText().toString();
        password = ET_PASSWORD.getText().toString();*/
        name = "ChiragNighut";
        fandom1 = "Miss AC";
        fandom2 = "Ben10";
        fandom3 = "StarWars";
        fandom4 = "GOT";
        fandom5 = "Friends";
        String method = "register";
        BackgroundTask backgroundTask = new BackgroundTask(this);
        backgroundTask.execute(method, name, lati, longi, fandom1, fandom2, fandom3, fandom4, fandom5);

        //finish();
    }

    public void btStart(View view) {

        DownloadImageTask dit=new DownloadImageTask((CircleImageView) findViewById(R.id.Profilepic));
        dit.execute("http://almat.almafiesta.com/images/ChiragNighut.jpg");
    }




    @Override
    protected void onResume() {
        super.onResume();
        //Now lets connect to the API
        mGoogleApiClient.connect();
        //Toast.makeText(this, "In onResume()",  Toast.LENGTH_LONG).show();
    }

    protected void onPause() {
        super.onPause();
        Log.v(this.getClass().getSimpleName(), "onPause()");

        //Disconnect from API onPause()
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onStart()
    {
        super.onStart();
        /*DownloadImageTask dit = new DownloadImageTask((CircleImageView) findViewById(R.id.Profilepic));
        dit.execute("http://almat.almafiesta.com/images/"+FirebaseAuth.getInstance()
                .getCurrentUser()
                .getDisplayName().replaceAll("\\s", "").toLowerCase()+".jpg");*/
    }
    @Override
    public void onConnected(Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Requesting...", Toast.LENGTH_LONG).show();
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    ACCESS_FINE_LOCATION);
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    ACCESS_COARSE_LOCATION);
        }
        else {
            location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (location == null) {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

            } else {
                //If everything went fine lets get latitude and longitude
                currentLatitude = location.getLatitude();
                currentLongitude = location.getLongitude();
                lati = currentLatitude + "";
                longi = currentLongitude + "";
                //Toast.makeText(this, currentLatitude + " WORKS " + currentLongitude + "", Toast.LENGTH_LONG).show();
                //TextView tv=findViewById(R.id.FandomsFollowed);
                //tv.setText(currentLatitude+" , "+currentLongitude);
            }
            try {
                FirebaseApp.initializeApp(this);
                mAuth = FirebaseAuth.getInstance();
                if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                    // Start sign in/sign up activity
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .build(),
                            SIGN_IN_REQUEST_CODE
                    );
                    //setContentView(R.layout.activity_main);
                } else {

                    Toast.makeText(this,
                            "Welcome " + FirebaseAuth.getInstance()
                                    .getCurrentUser()
                                    .getDisplayName(),
                            Toast.LENGTH_LONG)
                            .show();
                    DownloadImageTask dit = new DownloadImageTask((CircleImageView) findViewById(R.id.Profilepic));
                    dit.execute("http://almat.almafiesta.com/images/"+FirebaseAuth.getInstance()
                            .getCurrentUser()
                            .getDisplayName().replaceAll("\\s", "").toLowerCase()+".jpg");

                }
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
            }
        }
        }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case ACCESS_COARSE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    flag++;
                } else {
                    //finish();
                }
            }

            case ACCESS_FINE_LOCATION:
            {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    resetLayout();

                } else {
                    //finish();
                }
            }
        }
    }
    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
            /*
             * Google Play services can resolve some errors it detects.
             * If the error has a resolution, try sending an Intent to
             * start a Google Play services activity that can resolve
             * error.
             */
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
                    /*
                     * Thrown if Google Play services canceled the original
                     * PendingIntent
                     */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
                /*
                 * If no resolution is available, display a dialog to the
                 * user with the error.
                 */
            Log.e("Error", "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }
    @Override
    public void onLocationChanged(Location location) {
        currentLatitude = location.getLatitude();
        currentLongitude = location.getLongitude();

        Toast.makeText(this, currentLatitude + " WORKS " + currentLongitude + "", Toast.LENGTH_LONG).show();
    }

    public void FindFriends(View view) {


                try {
                    Intent myIntent = new Intent(MainActivity.this,
                            FindFriends.class);
                    startActivity(myIntent);
                } catch (Exception e) {
                    Log.e("Error", e.getMessage());
                }
            }
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        CircleImageView bmImage;

        public DownloadImageTask(CircleImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            if(result==null)
                bmImage.setImageResource(R.drawable.nrg);
            else
                bmImage.setImageBitmap(result);
            //return result;

        }
    }

}
