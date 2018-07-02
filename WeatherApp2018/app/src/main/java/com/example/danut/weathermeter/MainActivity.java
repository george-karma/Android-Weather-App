package com.example.danut.weathermeter;
//Created by  Nitu Danut-2018
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.StaticLayout;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

//Location Listener is implemented here to get the coordonates of the user more accurately and in real-time
public class MainActivity extends AppCompatActivity implements LocationListener {
    //declaring the necessary on screen elements
    static TextView cityTx;
    static TextView timeTx;
    static TextView tempTx;
    static TextView detailsTx;
    static TextView humidityTx;
    static TextView presureTx;
    static ImageView icon;
    //api key constant
    String apikey = "dbebc322e834378716ce279e858935d5";

    //geo-location provider along with a locaiton manager to be used in getting the users locaiton
    String bestProvider;
    LocationManager locationManager;

    //so theuser dosnt think the app has frozen if it takes a few seconds
    static ProgressBar progressBar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

            cityTx= (TextView) findViewById(R.id.city_field);
            timeTx=(TextView) findViewById(R.id.updated_field);
            tempTx=(TextView) findViewById(R.id.current_temperature_field);
            detailsTx=(TextView) findViewById(R.id.details_field);
            humidityTx=(TextView) findViewById(R.id.humidity_field);
            presureTx=(TextView) findViewById(R.id.pressure_field);
            progressBar = (ProgressBar) findViewById(R.id.progressBar);
            icon = (ImageView) findViewById(R.id.imageView);

        //checking if the device is online and giving an errror if it is not
        isOnline();
        if(isOnline() == false)
        {
            Toast.makeText(getApplicationContext(),"Please connect to the network and restart the applicaiton", Toast.LENGTH_SHORT).show();
        }
        else {
            getlocation();
        }
    }

    public void getlocation() {
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        bestProvider = String.valueOf(locationManager.getBestProvider(criteria, false)).toString();

        //Checks if the user has granted locaiton permisions to the app
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                ||
                ActivityCompat.checkSelfPermission(getApplicationContext(),
                        android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            //Prompts the user for permission
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 101);

        }
        //double checking the permisions before trying to get the last known location
        //not checking this here seems to lead to errors if the user exists the app in the middle of the background task
        else if (ContextCompat.checkSelfPermission(getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                ||
                ActivityCompat.checkSelfPermission(getApplicationContext(),
                        android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            //if the last known location is not null, we use that to avoid draining battery life
            Location location = locationManager.getLastKnownLocation(bestProvider);
            if (location != null) {
                Log.e("TAG", "GPS is on");

                //getting the latitudine and longitudine  from the last known locaiton
                String lat = String.valueOf(location.getLatitude());
                String lon = String.valueOf(location.getLongitude());

                DownloadTask getWeather = new DownloadTask();
                //executing the async task with the latitudine and longitudine varargs
                getWeather.execute(lat,lon);
            }
            else {
            //This is the juicy stuff right here
                // Only runs if there is no known last location
            refreshLocation();
        }

        }
        else
        {
             Toast.makeText(getApplicationContext(), "Please restart application and enable location services", Toast.LENGTH_LONG).show();
        }




    }
    //we suppress missing permisions because the code is unreachable without accepting the permisions
    @SuppressLint("MissingPermission")
    public void refreshLocation()
    {
        //requesting a live location
        locationManager.requestLocationUpdates(bestProvider, 5000, 5, this);
    }
    //callback/entrypoint after requesting a live location with refreshLocation()
    @Override
    public void onLocationChanged(Location location) {

        //remove location callback:
        locationManager.removeUpdates(this);


        String lat = String.valueOf(location.getLatitude());
        String lon = String.valueOf(location.getLongitude());
        DownloadTask getWeather = new DownloadTask();
        //executing the async task with the latitudine and longitudine varargs
        getWeather.execute(lat,lon);
    }


    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    //shows an error if location services are not available
    @Override
    public void onProviderDisabled(String s) {
        Toast.makeText(getApplicationContext(),"Location Providers Disables, Please Enable them in system settings", Toast.LENGTH_SHORT).show();
    }
    //checks if the device is online by checking the network status
    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }
}

