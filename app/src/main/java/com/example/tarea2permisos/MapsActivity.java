package com.example.tarea2permisos;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.tarea2permisos.databinding.ActivityMapsBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private EditText search;
    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private Marker searchMarker;
    private Marker touchMarker;
    private Marker locationMarker;
    LatLng aLatLng = new LatLng(4.65, -74.05);
    boolean isNewPos = false;

    String locationPermission = Manifest.permission.ACCESS_FINE_LOCATION;
    public static final int PERMISSION_LOCATION = 2;

    // Light Sensor
    SensorManager sensorManager;
    Sensor lightSensor;
    SensorEventListener lightSensorListener;

    //Geocoder
    Geocoder mGeocoder;

    Location newLocation = new Location("");

    //Simple location Atributtes
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    static final int REQUEST_CHECK_SETTINGS = 6;
    boolean isGPSEnabled = false;
    /****************************      ON CREATE  ***********************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = createLocationRequest();
        locationCallback = createLocationCallback();
        myRequestPermission(this, locationPermission, "Access to GPS", PERMISSION_LOCATION);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

       //


        mGeocoder = new Geocoder(this);

        // Light Sensor
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        lightSensorListener = createSensorEventListener();

        // Location


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        //React to the send button in the keyboard
        search = findViewById(R.id.searchMap);
        search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if(i == EditorInfo.IME_ACTION_SEARCH) {
                    String address = search.getText().toString();
                    LatLng position = searchByName(address);
                    if (position != null & mMap != null) {
                        if (searchMarker != null) searchMarker.remove();
                        searchMarker = mMap.addMarker(new MarkerOptions()
                                .position(position).title(address)
                                .icon(BitmapDescriptorFactory
                                        .defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(position));

                    }
                }
                return true;
            }
        });
        mapFragment.getMapAsync(this);
    }

    /***********************************************************************************************
     *                                          Location Request
     **********************************************************************************************/

    private LocationRequest createLocationRequest(){
        LocationRequest locationRequest = LocationRequest.create()
                .setInterval(10000)
                .setFastestInterval(5000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }
    /***********************************************************************************************
     *                                          LocationCallback
     **********************************************************************************************/

    private LocationCallback createLocationCallback(){

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Location location = locationResult.getLastLocation();

                if(location != null){
                    Log.i ( "TAG" , "location" + location.toString());
                    // Add a marker in Sydney and move the camera
                    LatLng bogota = new LatLng(location.getLatitude(), location.getLongitude()); // Guardamos la posición
                    /************************  JSON***********************************************/
                    if (locationMarker == null){
                        locationMarker = mMap.addMarker(new MarkerOptions().position(bogota).title("Usted esta aqui"));
                    }


                    JSONObject obj = new JSONObject();
                    try {
                        obj.put("latitud", location.getLatitude());
                        obj.put("longitud", location.getLongitude());
                        obj.put("date", location.getTime());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Writer output = null;
                    String filename= "locations.json";
                    try {
                        File file = new File(getBaseContext().getExternalFilesDir(null), filename);
                        FileWriter fileWriter = new FileWriter(file);
                        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                        bufferedWriter.write(obj.toString());
                        bufferedWriter.close();

                        Log.i("LOCATION", "Ubicacion de archivo: "+file);
                        // output = new BufferedWriter(new FileWriter(file));
                        // output.write(obj.toString());
                        // output.close();
                        Toast.makeText(getApplicationContext(), "Location saved",
                                Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        //Log error
                    }


                    /********************* Comparar con el anterior*******************************/
                    Location locationOne = new Location("");
                    locationOne.setLatitude(aLatLng.latitude);
                    locationOne.setLongitude(aLatLng.longitude);
                    /********************* Guardar la localización ******************************/
                    newLocation = location;
                    /***********************Inicializar con un puntero *************************/

                    float distanceInMetersOne = location.distanceTo(locationOne);
                    if( distanceInMetersOne > 5){
                        locationMarker.remove();
                        locationMarker = mMap.addMarker(new MarkerOptions().position(bogota).title("Usted esta aqui"));
                        mMap.moveCamera(CameraUpdateFactory.zoomTo(15));
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(bogota));
                        //Habilitar los "gestures" como pitch to zoom"
                        mMap.getUiSettings().setZoomGesturesEnabled(true);
                        //Habilitar botones de zoom
                        mMap.getUiSettings().setZoomControlsEnabled(true);
                        aLatLng = bogota;
                    }
                }

            }
        };

        return locationCallback;
    }





    private SensorEventListener createSensorEventListener(){
        SensorEventListener lightSensorListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                if (mMap != null) {
                    if (sensorEvent.values[0] <5000) {
                        Log.i("MAPS", "DARK MAP " + sensorEvent.values[0]);
                        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(MapsActivity.this, R.raw.dark_style_map));
                    } else {
                        Log.i("MAPS", "LIGHT MAP " + sensorEvent.values[0]);
                        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(MapsActivity.this, R.raw.light_style_map));
                    }
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {}
        };
        return lightSensorListener;
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
        mMap = googleMap;


        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override

            public void onMapLongClick(@NonNull LatLng latLng) {

                //String name = "Prueba";
                String name = searchByLocation(latLng.latitude , latLng.longitude);

                if(!"".equals(name)){
                    if(touchMarker!=null) touchMarker.remove();
                    touchMarker = mMap.addMarker(new MarkerOptions()
                            .position(latLng).title(name)
                            .icon(BitmapDescriptorFactory
                                    .defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));

                    }
            }
        });

/*
        //Información adicional.
        mMap.addMarker(new MarkerOptions().position(bogota)
                .title("BOGOTÁ")
                .snippet("Población: 8.081.000") //Texto de información
                .alpha(0.5f)); //Transparencia

        // Simbolo del marcador
        Marker bogotaAzul = mMap.addMarker(new MarkerOptions()
                .position(bogota)
                .icon(BitmapDescriptorFactory
                        .defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

  */
    }


    @Override
    protected void onResume() {
        super.onResume();
        checkSettingsLocation();
        sensorManager.registerListener(lightSensorListener, lightSensor,
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
        sensorManager.unregisterListener(lightSensorListener);

    }

    private void checkSettingsLocation() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                isGPSEnabled = true;
                startLocationUpdate();
            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                int statusCode = ((ApiException) e).getStatusCode();
                switch (statusCode) {
                    case CommonStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed by showing the user a dialog.
                        try {// Show the dialog by calling startResolutionForResult(), and check the result in onActivityResult().
                            ResolvableApiException resolvable = (ResolvableApiException) e;
                            resolvable.startResolutionForResult(MapsActivity.this,
                                    REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException sendEx) {

                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. No way to fix the settings so we won't show the dialog.


                        break;
                }
            }
        });
    }

    private void startLocationUpdate(){
        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
            if(isGPSEnabled){
                fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
            }

        }
    }

    private void stopLocationUpdates(){
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == RESULT_OK) {
                isGPSEnabled = true;
                startLocationUpdate();
            } else {
                startActivity(new Intent(MapsActivity.this, MainActivity.class));
                Toast.makeText(this, "No se puede usar esta funcionalidad", Toast.LENGTH_LONG).show();
            }
        }
    }

    /***********************************************************************************************
     **                                 searchByName
     **********************************************************************************************/

    private  LatLng searchByName(String addressString){

        if (!addressString.isEmpty()) {
            try {
                List<Address> addresses = mGeocoder.getFromLocationName(addressString, 2);
                if (addresses != null && !addresses.isEmpty()) {
                    Address addressResult = addresses.get(0);
                    LatLng position = new LatLng(addressResult.getLatitude(), addressResult.getLongitude());
                    Location oldLocation = new Location("");
                    oldLocation.setLatitude(position.latitude);
                    oldLocation.setLongitude(position.longitude);

                    /**************************** Calcula distancia **********************************/
                    float distanceInMetersOne = oldLocation.distanceTo(newLocation);
                    //Distancia
                    Toast.makeText(this, "Distancia " + distanceInMetersOne + " m" , Toast.LENGTH_LONG).show();

                    return  position;

                } else {Toast.makeText(MapsActivity.this, "Dirección no encontrada", Toast.LENGTH_SHORT).show();}
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {Toast.makeText(MapsActivity.this, "La dirección esta vacía", Toast.LENGTH_SHORT).show();}
        return new LatLng(4.65, -74.05);
    }
    /***********************************************************************************************
     **                                 searchByLocation
     **********************************************************************************************/


    private String searchByLocation(double latitude , double longitude){
        try {
            List<Address> addresses = mGeocoder.getFromLocation(latitude, longitude,2);
            if (addresses != null && !addresses.isEmpty()) {
                Address addressResult = addresses.get(0);
                String namePos = new String(addressResult.getAddressLine(0).toString());
                Location oldLocation = new Location("");
                oldLocation.setLatitude(latitude);
                oldLocation.setLongitude(longitude);

                /**************************** Calcula distancia **********************************/
                float distanceInMetersOne = oldLocation.distanceTo(newLocation);
                //Distancia
                Toast.makeText(this, "Distancia " + distanceInMetersOne + " m" , Toast.LENGTH_LONG).show();
                return  namePos;

            } else {Toast.makeText(MapsActivity.this, "Nombre no encontrada", Toast.LENGTH_SHORT).show();}
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "**";

    }





    /***********************************************************************************************
     **                                 PERMISOS DE LOCALIZACION
     **********************************************************************************************/


    private void myRequestPermission(Activity context, String permission, String justification, int id){
        // Si no tengo permisos
        if(ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(context, permission)){
                Toast.makeText(context, justification, Toast.LENGTH_SHORT).show();
            }
            ActivityCompat.requestPermissions(context, new String[]{permission}, id);
        }
    }

//Log error

    //@Override
    //public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    //    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    //   if (requestCode == PERMISSION_LOCATION){
     //       accessLocation();
    ///    }
  //  }




}











