package com.example.danut.weathermeter;
//Created by  Nitu Danut-2018
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

//this class extends asyncTask to download the necessary data off the OpenWeatherMap Server
//We take a string in doInBackground and return a JSONObject to onPostExecute
//The middle is void because we dont provide live updates of the progress
public class DownloadTask extends AsyncTask<String,Void,JSONObject> {

    //bitmap to be used to store and isplay the on screen image
    Bitmap bitmap;

    //constats
    private static final String OPEN_WEATHER_MAP_URL =


            "http://api.openweathermap.org/data/2.5/weather?lat=51&lon=0&units=metric";

    private static final String OPEN_WEATHER_MAP_API = "dbebc322e834378716ce279e858935d5";


    //making the progress bar visible before we start the background operation
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        MainActivity.progressBar.setVisibility(View.VISIBLE);
    }


    @Override
    protected JSONObject doInBackground(String... coordinates) {
        JSONObject weatherResults = null;
        try {
            //we call the getWeather method with two varargs
            weatherResults = getWeather(coordinates[0], coordinates[1]);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        //we return the JSON Object to the onPostExecute method
        return weatherResults;


    }

    //callback method after doInBackground
    @Override
    protected void onPostExecute(JSONObject jsonObject) {

        try
        {

            JSONObject main = new JSONObject(jsonObject.getString("main"));
            JSONObject details = jsonObject.getJSONArray("weather").getJSONObject(0);
            DateFormat df = DateFormat.getDateTimeInstance();

            //we get strings out of the JSON data
            String temperature = String.format("%.2f", main.getDouble("temp"))+ "°";
            String humidity = main.getString("humidity") + "%";
            String pressure = main.getString("pressure") + " hPa";
            String city = jsonObject.getString("name").toUpperCase(Locale.US) + ", " + jsonObject.getJSONObject("sys").getString("country");
            String updatedOn = df.format(new Date(jsonObject.getLong("dt")*1000));
            String description = details.getString("description").toUpperCase(Locale.US);


            //we displat the srigns we got from the JSON data
            MainActivity.tempTx.setText(temperature);
            MainActivity.cityTx.setText(city);
            MainActivity.humidityTx.setText("Humidity: "+humidity);
            MainActivity.presureTx.setText("Pressure: "+pressure);
            MainActivity.timeTx.setText(updatedOn);
            MainActivity.detailsTx.setText(description);

            //using the bitmap we downloaded in doInBackground, we set the icon to match the weather
            //icon kindly provided by the OpenWeatherMap Api
            MainActivity.icon.setImageBitmap(bitmap);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        MainActivity.progressBar.setVisibility(View.INVISIBLE);
    }




    public JSONObject getWeather(String lat,String lon)
    {
        //The String.format() method is like black magic, because i used it in
        //multiple projects and I still don't fully understand how it works.
        //Same can be said for the whole okHttp library.
        String results = null;
        JSONObject jsonObject = null;
        try {
            //query the server with the right api key and location
            URL url = new URL("http://api.openweathermap.org/data/2.5/weather?lat="+lat+ "&lon="+lon+"&units=metric&appid="+OPEN_WEATHER_MAP_API+"");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            InputStream in = connection.getInputStream();
            InputStreamReader reader = new InputStreamReader(in);
            int data = reader.read();
           //we read the data one int at a time and we store it in a int then cast it to chars and sore it in results
            while (data!=-1)
            {

                char current = (char) data;
                results += current;
                data = reader.read();

            }
            //OpenWeatherMap Api starts the return JSON with "null" which breaks the way JSON is formated and results in multiple errors
            //Soe we delete the prefix from the start of the string



































































































































































            String crappyPrefix = "null";
            if(results.startsWith(crappyPrefix)){
                results = results.substring(crappyPrefix.length(), results.length());
            }
            //we need to create a new JSON object out of the string in order to return it
            jsonObject = new JSONObject(results);

            //we also download the bitmap data for the weather icon here
            //because if we do it out of an async task it will run on a UI thread which android does not like
            //thats why we have a global bitmap up at the top, to avoid this issue
            JSONObject details = jsonObject.getJSONArray("weather").getJSONObject(0);
            String icon = details.getString("icon");
            InputStream drawStream = (InputStream) new URL("http://openweathermap.org/img/w/"+icon+".png").openStream();
            bitmap = BitmapFactory.decodeStream(drawStream);

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        //returning the json object with the Open Weather Map data
        return jsonObject;
    }

}
