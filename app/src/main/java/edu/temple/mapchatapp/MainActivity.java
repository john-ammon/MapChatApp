package edu.temple.mapchatapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements PartnerListFragment.OnFragmentInteractionListener, MapFragment.OnFragmentInteractionListener {

    FragmentManager fragmentManager;
    PartnerListFragment plf;
    MapFragment mf;

    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    EditText usernameText;

    LocationManager lm;
    LocationListener ll;

    String username;
    double lat;
    double lon;
    Location mLocation;
    ArrayList<Partner> partners;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fragmentManager = getSupportFragmentManager();
        preferences = getApplicationContext().getSharedPreferences("MyPrefs", 0);
        editor = preferences.edit();
        
        //display fragments
        getPartners();
        fragmentManager
                .beginTransaction()
                .addToBackStack(null)
                .replace(R.id.listFragment, plf.newInstance(partners))
                .replace(R.id.mapFragment, mf.newInstance())
                .commit();

        //update fragments every 30 seconds
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                getPartners();
                fragmentManager
                        .beginTransaction()
                        .replace(R.id.listFragment, plf.newInstance(partners))
                        .replace(R.id.mapFragment, mf.newInstance())
                        .commitAllowingStateLoss();
                handler.postDelayed(this, 30000);
            }
        }, 1000);

        //get username
        usernameText = findViewById(R.id.usernameText);
        if(preferences.contains("username")) {
            username = preferences.getString("username", null);
            usernameText.setText(username);
        } else {
            username = usernameText.getText().toString();
        }

        //button to change username
        final Button usernameButton = findViewById(R.id.usernameButton);
        usernameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                usernameText = findViewById(R.id.usernameText);
                username = usernameText.getText().toString();
                editor.putString("username", username);
                editor.commit();
            }
        });

        //get user location
        lm = getSystemService(LocationManager.class);
        ll = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                lat = location.getLatitude();
                lon = location.getLongitude();

                mLocation = new Location("myLocation");
                mLocation.setLatitude(lat);
                mLocation.setLongitude(lon);

                Log.v("LOCATION", String.valueOf(lat));

                postLocation();

            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {
                stopLocationUpdates();
            }
        };

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 111);
        } else {
            showLocationUpdates();
        }

        postLocation();
    }

    private void showLocationUpdates() {
        if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_DENIED) {
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 10, ll);
        }
    }

    private void stopLocationUpdates() {
        lm.removeUpdates(ll);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            showLocationUpdates();
        }
    }

    public void getPartners() {
        final Thread t = new Thread(){
            @Override
            public void run(){
                partners = new ArrayList<>();

                try {
                    URL url = new URL("https://kamorris.com/lab/get_locations.php");
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(
                                    url.openStream()));

                    String response = "", tmpResponse;

                    tmpResponse = reader.readLine();
                    while (tmpResponse != null) {
                        response = response + tmpResponse;
                        tmpResponse = reader.readLine();
                    }

                    Location myLocation = new Location("Me");
                    myLocation.setLatitude(lat);
                    myLocation.setLongitude(lon);

                    JSONArray jsonArray = new JSONArray(response);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonobject = jsonArray.getJSONObject(i);
                        String partnerName = jsonobject.getString("username");
                        String latitude = jsonobject.getString("latitude");
                        String longitude = jsonobject.getString("longitude");
                        double partnerLat = Double.valueOf(latitude);
                        double partnerLon = Double.valueOf(longitude);
                        Location partnerLocation = new Location("partner");
                        partnerLocation.setLatitude(partnerLat);
                        partnerLocation.setLongitude(partnerLon);
                        double distance = myLocation.distanceTo(partnerLocation);
                        Partner newPartner = new Partner(partnerName, partnerLat, partnerLon, distance);
                        partners.add(newPartner);
                    }
                    Collections.sort(partners);
                    Log.v("test", "LIST UPDATED");
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        };
        t.start();
        try { t.join(); }
        catch (Exception e) { System.out.println(e); }
    }

    public void postLocation() {
        RequestQueue rq = Volley.newRequestQueue(this);
        String url = "https://kamorris.com/lab/register_location.php";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        // response
                        Log.d("Response", "OK");
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("Response", "ERROR");
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<>();
                params.put("user", username);
                params.put("latitude", String.valueOf(lat));
                params.put("longitude", String.valueOf(lon));

                return params;
            }
        };
        rq.add(postRequest);
    }

    public void onFragmentInteraction(int position) {

    }

    @Override
    public void onMapFragmentInteraction() {

    }
}


