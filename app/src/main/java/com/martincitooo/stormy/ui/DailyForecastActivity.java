package com.martincitooo.stormy.ui;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.martincitooo.stormy.R;
import com.martincitooo.stormy.adapters.DayAdapter;
import com.martincitooo.stormy.weather.Day;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DailyForecastActivity extends Activity {

    private Day[] mDays;
    private String mCityName;
    @BindView(android.R.id.list) ListView mListView;
    @BindView(android.R.id.empty) TextView mEmptyTextView;
    @BindView(R.id.cityLabel) TextView mLocationLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_forecast);

        ButterKnife.bind(this);

        Intent intent = getIntent();
        Parcelable[] parcelables = intent.getParcelableArrayExtra(MainActivity.DAILY_FORECAST);

        mCityName = intent.getStringExtra(MainActivity.CITY_NAME);

        mLocationLabel.setText(mCityName+"");
        mDays = Arrays.copyOf(parcelables, parcelables.length, Day[].class);

        //copyOf makes a copy of one array into another
        //1st parameter is the array I want to copy from (parcelables in this case)
        //2nd parameter is the length (parcelables.length in this case)
        //3rd parameter is the type that I'm going to use for the new array (Day[].class in this case)

        /*I MUST delete this because is a default adapter...

        String[] daysOfTheWeek = {"Domingo","Lunes","Martes","Miércoles","Jueves","Viernes","Sábado"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,daysOfTheWeek);
        //The three pieces of information we need
        //this = the context
        //android.R.layout.simple_list_item_1 = id of the layout I want to use for each item in the list
        //simple_list_item_1 is a sample layout provided by Android
        //daysOfTheWeek = array I want to adapt. Here is the array that has the days of the week

        //set this new adapter as the adapter for my List View
        setListAdapter(adapter);

        */

        DayAdapter adapter = new DayAdapter(this, mDays);
        //first parameter is this context
        //second parameter is my array of DailyForecast in the Forecast object, which is still only available in the main activity
        mListView.setAdapter(adapter);
        mListView.setEmptyView(mEmptyTextView);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String dayOfTheWeek = mDays[position].getDayOfTheWeek();
                String conditions = mDays[position].getSummary();
                String highTemp = mDays[position].getTemperatureMax() + "°";
                String message = String.format("El clima del %s será: %s y la máxima será de %s", dayOfTheWeek, conditions, highTemp);
                Toast.makeText(DailyForecastActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }
}
