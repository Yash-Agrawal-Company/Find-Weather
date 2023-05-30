package com.yashagrawal.findweather;
import androidx.appcompat.app.AppCompatActivity;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputLayout;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {
    TextInputLayout city;
    TextView temp,humid,speed,desp,slogan;
    Button submit;
    Animation topAnim;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Defining hooks
        city = findViewById(R.id.cityName);
        temp = findViewById(R.id.temp);
        humid = findViewById(R.id.humidity);
        speed = findViewById(R.id.speed);
        desp = findViewById(R.id.desp);
        submit = findViewById(R.id.submit);
        slogan = findViewById(R.id.textView);

        SharedPreferences preferences = getSharedPreferences("mypref",MODE_PRIVATE);
        String received = preferences.getString("cityName",null);
        String receivedTemp = preferences.getString("temp",null);
        String receivedHumid = preferences.getString("humid",null);
        String receivedSpeed = preferences.getString("ws",null);
        String receivedDesp = preferences.getString("desp",null);

        city.getEditText().setText(received);
        temp.setText(receivedTemp);
        humid.setText(receivedHumid);
        speed.setText(receivedSpeed);
        desp.setText(receivedDesp);

        topAnim = AnimationUtils.loadAnimation(this,R.anim.topanim);
        slogan.setAnimation(topAnim);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String cityValue = city.getEditText().getText().toString().trim();
                if (cityValue.isEmpty()) {
                    city.setError("City name cannot be empty");
                } else {
                    city.setError(null);
                    city.setErrorEnabled(false);
                    city.getEditText().setText(cityValue);

                    String apiKey = "b60713b7a32d9d2366989bf73365f9d7";
                    String url = "https://api.openweathermap.org/data/2.5/weather?q=" + cityValue + "&appid=b60713b7a32d9d2366989bf73365f9d7";

                    //For the loading indicator
                    Dialog dialog = new Dialog(MainActivity.this);
                    dialog.setContentView(R.layout.dialogactivity);
                    dialog.show();
                    dialog.setCancelable(false);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));

                    RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                    JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                JSONObject mainObject = response.getJSONObject("main");
                                JSONObject wind = response.getJSONObject("wind");
                                JSONObject weather = response.getJSONArray("weather").getJSONObject(0);

                                String description = weather.getString("description");
                                String windSpeed = wind.getString("speed");
                                String temperature = mainObject.getString("temp");
                                String humidity = mainObject.getString("humidity");

                                dialog.dismiss();   // Closing the loading indicator

                                // For Temperature
                                Double celcius = Double.parseDouble(temperature) - 273.15;
                                temp.setText("Temperature : " + celcius.toString().substring(0, 5) + " °C");

                                //For Humidity
                                humid.setText("Humidity : " + humidity + "%");

                                //For Wind speed
                                double ws = Double.parseDouble(windSpeed) * 3.6;
                                DecimalFormat decimalFormat = new DecimalFormat("#.###");
                                String fomattedValue = decimalFormat.format(ws);
                                speed.setText("Wind Speed : " + fomattedValue + " Km/H");

                                //For Weather Description
                                desp.setText("Weather Description : " + description);

                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putString("cityName",cityValue);
                                editor.putString("temp","Temperature : " + celcius.toString().substring(0, 5) + " °C");
                                editor.putString("humid","Humidity : " + humidity + "%");
                                editor.putString("ws","Wind Speed : " + fomattedValue + " Km/H");
                                editor.putString("desp","Weather Description : " + description);
                                editor.apply();

                            } catch (JSONException e) {
                                dialog.dismiss();
                                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                            }

                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            dialog.dismiss();
                            Toast.makeText(MainActivity.this, "Please check your city name . . ", Toast.LENGTH_SHORT).show();
                        }
                    });
                    queue.add(request);   //
                }
            }
        });

    }

}