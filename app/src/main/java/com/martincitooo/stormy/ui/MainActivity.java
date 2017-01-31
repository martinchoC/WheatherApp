package com.martincitooo.stormy.ui;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.martincitooo.stormy.R;
import com.martincitooo.stormy.gps.GPSTracker;
import com.martincitooo.stormy.weather.Current;
import com.martincitooo.stormy.weather.Day;
import com.martincitooo.stormy.weather.Forecast;
import com.martincitooo.stormy.weather.Hour;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();
    public static final String DAILY_FORECAST = "DAILY_FORECAST";
    public static final String HOURLY_FORECAST = "HOURLY_FORECAST";
    public static final String CITY_NAME = "CITY_NAME";

    // GPSTracker class
    GPSTracker mLocation;
    private Forecast mForecast;
    private String mCityName;
    private double latitude;
    private double longitude;

    @BindView(R.id.timeLabel) TextView mTimeLabel;
    @BindView(R.id.locationLabel) TextView mLocationLabel;
    @BindView(R.id.temperatureLabel) TextView mTemperatureLabel;
    @BindView(R.id.humidityValue) TextView mHumidityValue;
    @BindView(R.id.precipValue) TextView mPrecipValue;
    @BindView(R.id.summaryLabel) TextView mSummaryLabel;
    @BindView(R.id.iconImageView) ImageView mIconImageView;
    @BindView(R.id.refreshImageView) ImageView mRefreshImageView;
    @BindView(R.id.progressBar) ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mProgressBar.setVisibility(View.INVISIBLE);
        mRefreshImageView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                getForecast();
            }
        });
        getForecast();
    }

    private void getForecast() {
        String apiKey = "2d9b100348afdf4a92c95e546066d976";
        mLocation = new GPSTracker(MainActivity.this);
        boolean canGetLocation = mLocation.canGetLocation();
        if(canGetLocation == true) {
            setLatitudeLongitude();
            Geocoder gcd = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = null;
            try {
                addresses = gcd.getFromLocation(latitude, longitude, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (addresses.size() > 0) {
                mCityName = addresses.get(0).getLocality();
                mLocationLabel.setText(mCityName + "");
            }
            String forecastUrl = "https://api.darksky.net/forecast/" + apiKey + "/" + latitude + "," + longitude + "?units=si&lang=es";
            //Log.d("LATLONG","Lat: "+latitude+", long: "+longitude);

            if (isNetworkAvailable()) {
                toogleRefresh();
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(forecastUrl).build();
                Call call = client.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                toogleRefresh();
                            }
                        });
                        alertUserAboutError();
                    }
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                toogleRefresh();
                            }
                        });
                        try {
                            String jsonData = response.body().string();
                            if (response.isSuccessful()) {
                                mForecast = parseForecastDetails(jsonData);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        updateDisplay();
                                    }
                                });
                            } else {
                                alertUserAboutError();
                            }
                        } catch (IOException e) {
                            Log.e(TAG, "Exception caught", e);
                        } catch (JSONException e) {
                            Log.e(TAG, "JSON Exception", e);
                        }
                    }
                });
            } else {
                Toast.makeText(this, getString(R.string.network_unavailable_message), Toast.LENGTH_LONG).show();
            }
        }
        else{
            mTemperatureLabel.setText("--");
            mPrecipValue.setText("--");
            mHumidityValue.setText("--");
            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            mLocation.showSettingsAlert();

        }
    }

    //setter for the current city latitude and longitude
    private void setLatitudeLongitude(){
        boolean canGetLocation = mLocation.canGetLocation();
        System.out.println("can get location? : "+canGetLocation);
        // check if GPS enabled
        if(canGetLocation == true){
            latitude = mLocation.getLatitude();
            longitude = mLocation.getLongitude();
            mLocation.stopUsingGPS();
        }else{
            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            mLocation.showSettingsAlert();
            mTemperatureLabel.setText("N/A");
        }
    }

    //Refresh button in the top of the main screen
    private void toogleRefresh() {
        if(mProgressBar.getVisibility() == View.INVISIBLE) {
            mProgressBar.setVisibility(View.VISIBLE);
            mRefreshImageView.setVisibility(View.INVISIBLE);
        }
        else{
            mProgressBar.setVisibility(View.INVISIBLE);
            mRefreshImageView.setVisibility(View.VISIBLE);
        }
    }

    //method for updating the current weather in the main screen
    private void updateDisplay() {
        Current current = mForecast.getCurrent();
        mTemperatureLabel.setText(current.getTemperature()+"");
        mTimeLabel.setText("A las "+ current.getFormattedTime());
        mHumidityValue.setText(current.getHumidity()+"%");
        mPrecipValue.setText(current.getPrecipChance()+"%");
        mSummaryLabel.setText(current.getSummary());
        Drawable drawable = getResources().getDrawable(current.getIconId());
        mIconImageView.setImageDrawable(drawable);
    }

    //method for creating a forecast object from a string that is a json
    private Forecast parseForecastDetails(String jsonData) throws JSONException {
        Forecast forecast = new Forecast();
        forecast.setCityName(mCityName);
        forecast.setCurrent(getCurrentDetails(jsonData));
        forecast.setHourlyForecast(getHourlyForecast(jsonData));
        forecast.setDailyForecast(getDailyForecast(jsonData));
        return forecast;
    }

    //method that returns an array having the next 7-days weather
    private Day[] getDailyForecast(String jsonData)throws JSONException {
        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");
        JSONObject daily = forecast.getJSONObject("daily");
        JSONArray data = daily.getJSONArray("data");

        Day[]days = new Day[data.length()];

        for(int i = 0; i < data.length(); i++){
            JSONObject jsonDay = data.getJSONObject(i);
            Day day = new Day();
            day.setSummary(jsonDay.getString("summary"));
            day.setIcon(jsonDay.getString("icon"));
            day.setTemperatureMax(jsonDay.getDouble("temperatureMax"));
            day.setTime(jsonDay.getLong("time"));
            day.setTimezone(timezone);
            days[i]=day;
        }
        return days;
    }

    //method that returns the weather for the next hours
    private Hour[] getHourlyForecast(String jsonData) throws JSONException {
        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");
        JSONObject hourly = forecast.getJSONObject("hourly");
        JSONArray data = hourly.getJSONArray("data");
        Hour[] hours = new Hour[data.length()];
        for(int i=0; i<data.length(); i++){
            JSONObject jsonHour = data.getJSONObject(i);
            Hour hour = new Hour();
            hour.setSummary(jsonHour.getString("summary"));
            hour.setTemperature(jsonHour.getDouble("temperature"));
            hour.setIcon(jsonHour.getString("icon"));
            hour.setTime(jsonHour.getLong("time"));
            hour.setTimezone(timezone);
            hours[i]=hour;
        }
        return hours;
    }


    //Creates a current object from a string that is a json
    private Current getCurrentDetails(String jsonData) throws JSONException {
        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");
        JSONObject currently = forecast.getJSONObject("currently");
        Current current = new Current();
        current.setHumidity(currently.getDouble("humidity"));
        current.setTime(currently.getLong("time"));
        current.setIcon(currently.getString("icon"));
        current.setPrecipChance(currently.getDouble("precipProbability"));
        current.setSummary(currently.getString("summary"));
        current.setTemperature(currently.getDouble("temperature"));
        current.setTimeZone(timezone);
        return current;
    }


    // checks if network is available
    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if(networkInfo !=null && networkInfo.isConnected()){
            isAvailable = true;
        }
        return isAvailable;
    }

    private void alertUserAboutError() {
        AlertDialogFragment dialog = new AlertDialogFragment();
        dialog.show(getFragmentManager(),"error_dialog");
    }

    //Button "clima por hora" clicked
    @OnClick (R.id.dailyButton)
    public  void startDailyActivity(View view){
        boolean canGetLocation = mLocation.canGetLocation();
        if(canGetLocation && mTemperatureLabel.getText()!="--") {
            Intent intent = new Intent(this, DailyForecastActivity.class);
            intent.putExtra(DAILY_FORECAST, mForecast.getDailyForecast());
            intent.putExtra(CITY_NAME, mLocationLabel.getText());
            startActivity(intent);
        }
        else {
            mLocation.showSettingsAlert();
        }
    }

    //Button "clima la proxima semana" clicked
    @OnClick (R.id.hourlyButton)
    public void startHourlyActivity(View view){
        boolean canGetLocation = mLocation.canGetLocation();
        if(canGetLocation && mTemperatureLabel.getText()!="--") {
            Intent intent = new Intent(this,HourlyForecastActivity.class);
            intent.putExtra(HOURLY_FORECAST,mForecast.getHourlyForecast());
            startActivity(intent);
        }
        else {
            mLocation.showSettingsAlert();
        }
    }
}
