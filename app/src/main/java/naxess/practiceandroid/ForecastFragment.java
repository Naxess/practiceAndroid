package naxess.practiceandroid;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ForecastFragment extends Fragment
{
    Button enter;
    EditText zipCode;
    Button testButton;
    ListView lv;
    static String copyResultStrs [];
    static ArrayAdapter<String> a;
    static boolean empty = true;

    public ForecastFragment()
    {

    }
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        inflater.inflate(R.menu.forecastfragment, menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        TextView zipCode = (TextView) getView().findViewById(R.id.zip_code);
        String zipCodeString = zipCode.getText().toString();
        int id = item.getItemId();
        if(id == R.id.action_refresh)
        {
            FetchWeatherTask weatherTask = new FetchWeatherTask();
            weatherTask.execute(zipCodeString);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        String[] filler = {
                "Enter zip code to see forecast"
        };
        List<String> anArray = new ArrayList<String>(Arrays.asList(filler));

        a = new ArrayAdapter<String>(getActivity(),R.layout.list_item_forecast,R.id.list_item_forecast_textview,anArray);

        View rootView = inflater.inflate(R.layout.fragment_main,container,false);

        lv = (ListView) rootView.findViewById(R.id.listview_forecast);
        lv.setAdapter(a);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {

                Intent intent = new Intent("naxess.practiceandroid.ExtraDetails");
                int day = position;
                if(empty == false)
                {
                    intent.putExtra("doorbell","");
                    try
                    {
                        String testS = FetchWeatherTask.getWeatherFromJson(copyResultStrs, day);
                        intent.putExtra("data", testS);
                    }
                    catch (JSONException e)
                    {
                        e.printStackTrace();
                    }
                }
                else if(empty == true)
                {
                    intent.putExtra("doorbell", "nobodyHome");
                }
                startActivity(intent);
            }
        });

        enter = (Button) rootView.findViewById(R.id.enter_button);
        zipCode = (EditText) rootView.findViewById(R.id.zip_code);

        enter.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                try
                {
                    String zipCodeString = zipCode.getText().toString();
                    FetchWeatherTask weatherTask = new FetchWeatherTask();
                    weatherTask.execute(zipCodeString);
                }
                catch (NumberFormatException e)
                {
                    zipCode.setText("");
                    zipCode.setHint("Invalid zip code.");
                }
            }
        });

        testButton = (Button)rootView.findViewById(R.id.test_button); //WILL NOT BE IN THE FINAL VERSION. TESTING PURPOSES ONLY
        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                zipCode.setText("");
            }
        });

        return rootView;
    }
    public static class FetchWeatherTask extends AsyncTask<String,Void,String[]>
    {
        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

        private String getReadableDateString(long time)
        {
            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE, MMM dd");
            return shortenedDateFormat.format(time);
        }

        private String formatHighLows(double high, double low) {
            long roundedHigh = Math.round(high);
            long roundedLow = Math.round(low);

            String highLowStr = roundedHigh + "/" + roundedLow;
            return highLowStr;
        }

        private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)throws JSONException
        {
            final String OWM_LIST = "list";
            final String OWM_WEATHER = "weather";
            final String OWM_TEMPERATURE = "temp";
            final String OWM_MAX = "max";
            final String OWM_MIN = "min";
            final String OWM_DESCRIPTION = "main";

            final String OWM_SPEED = "speed"; //wind speed
            final String OWM_DEG = "deg"; //wind direction(degree)
            final String OWM_PRESSURE = "pressure";
            final String OWM_HUMIDITY = "humidity";

            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

            Time dayTime = new Time();
            dayTime.setToNow();

            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

            dayTime = new Time();
            copyResultStrs = new String[numDays];
            String[] resultStrs = new String[numDays];
            for(int i = 0; i < weatherArray.length(); i++)
            {
                String day;
                String description;
                String highAndLow;

                //Grabs the weather information for the day(i)
                JSONObject dayForecast = weatherArray.getJSONObject(i);

                //DATE(day of week, Month/Day)
                long dateTime;
                dateTime = dayTime.setJulianDay(julianStartDay+i);
                day = getReadableDateString(dateTime);

                //WEATHER DESCRIPTION(Snow, rain, clear, etc..)
                JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                description = weatherObject.getString(OWM_DESCRIPTION);

                //TEMPERATURES(High and low temperatures of the day)
                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                double high = temperatureObject.getDouble(OWM_MAX);
                double low = temperatureObject.getDouble(OWM_MIN);
                highAndLow = formatHighLows(high, low);

                //HUMIDITY
                int humidity = weatherArray.getJSONObject(i).getInt(OWM_HUMIDITY);

                //WIND SPEED
                double speed = weatherArray.getJSONObject(i).getDouble(OWM_SPEED);

                //WIND DIRECTION aka DEGREES
                String direction = "-";
                int deg = weatherArray.getJSONObject(i).getInt(OWM_DEG);
                if(deg > 45 && deg <= 135)
                {
                    direction = "North";
                }
                else if(deg > 135 && deg <= 225)
                {
                    direction = "West";
                }
                else if(deg > 225 && deg <= 315)
                {
                    direction = "South";
                }
                else if((deg > 315 && deg <= 360) || (deg >= 0 && deg <= 45))
                {
                    direction = "East";
                }

                //PRESSURE
                double pressure = weatherArray.getJSONObject(i).getDouble(OWM_PRESSURE);
                copyResultStrs[i] = day
                        + "\n\nExpected weather: " + description
                        + "\nTemperatures: "
                        + "\n       High: " + high + "\u2109"
                        + "\n       Low: " + low + "\u2109"
                        + "\n\nHumidity: " + humidity + "%"
                        + "\nWind Speed: " + speed + " mph"
                        + "\nWind Direction: " + direction
                        + "\nPressure: " + pressure;
                resultStrs[i] = day + " - " + description + " - (" + highAndLow + "\u2109" + ") ";
            }
            for (String s : resultStrs)
            {
                Log.v(LOG_TAG, "Forecast entry: " + s);
            }
            empty = false;
            return resultStrs;
        }
        private static String getWeatherFromJson(String [] copyResultStrs, int day) throws JSONException
        {
            return copyResultStrs[day];
        }


        @Override
        protected String[] doInBackground(String... params)
        {
            if(params.length == 0)
            {
                return null;
            }

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String forecastJsonStr = null;

            String format = "json";
            String units = "imperial";
            int numDays = 14;

            try
            {
                final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
                final String QUERY_PARAM = "zip"; //changed from 'q'. 'q' gives a different city: "Askola"
                final String FORMAT_PARAM = "mode";
                final String UNITS_PARAM = "units";
                final String DAYS_PARAM = "cnt";
                final String APPID_PARAM = "APPID";
                //final String OPEN_WEATHER_MAP_API_KEY = "fbdb1bd9a21f79aa5ab46b5739ee96fc";
                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter(QUERY_PARAM,params[0])
                        .appendQueryParameter(FORMAT_PARAM,format)
                        .appendQueryParameter(UNITS_PARAM,units)
                        .appendQueryParameter(DAYS_PARAM,Integer.toString(numDays))
                        .appendQueryParameter(APPID_PARAM,BuildConfig.OPEN_WEATHER_MAP_API_KEY)
                        .build();
                URL url = new URL(builtUri.toString());

                Log.v(LOG_TAG,"Built URI " + builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null)
                {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null)
                {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0)
                {
                    return null;
                }
                forecastJsonStr = buffer.toString();
                Log.v(LOG_TAG,"Forecast JSON String: " + forecastJsonStr);
            }
            catch (IOException e)
            {
                Log.e(LOG_TAG, "Error ", e);
                return null;
            }
            finally
            {
                if (urlConnection != null)
                {
                    urlConnection.disconnect();
                }
                if (reader != null)
                {
                    try
                    {
                        reader.close();
                    }
                    catch (final IOException e)
                    {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
            try
            {
                return getWeatherDataFromJson(forecastJsonStr,numDays);
            }
            catch(JSONException e)
            {
                Log.e(LOG_TAG, e.getMessage(),e);
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(String[] result)
        {
            if(result != null)
            {
                a.clear();
                for(String dayForecastStr : result)
                {
                    a.add(dayForecastStr);
                }
            }
        }
    }
}