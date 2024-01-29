package com.example.water4u;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.water4u.databinding.ActivityMapsBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.android.PolyUtil;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.TravelMode;

import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Marker clickedMarker;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    LatLng userLocation;
    Polyline currentPolyline;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    boolean AddLocation;
    boolean flag = true;
    Button reviewButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        com.example.water4u.databinding.ActivityMapsBinding binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        AddLocation = getIntent().getBooleanExtra("Add Location",false);

        reviewButton = findViewById(R.id.reviewButton);
        reviewButton.setVisibility(View.GONE);

        reviewButton.setOnClickListener(view -> {
            Intent reviewActivity = new Intent(MapsActivity.this, ReviewActivity.class);
            startActivity(reviewActivity);
        });
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
    @SuppressLint("PotentialBehaviorOverride")
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getDeviceLocation();
        }

        fetchAndDisplayMarkers();

        mMap.setOnMarkerClickListener(marker -> {
            LatLng dest = marker.getPosition();

            if(currentPolyline != null){
                currentPolyline.remove();
            }

            reviewButton.setVisibility(View.VISIBLE);
            requestDirections(dest);
            return false;
        });

        // Adding a onClick Listener only when the user clicks on the add water location
        if(AddLocation){
            mMap.setOnMapClickListener(latLng -> {
                Log.d("MapClickListener","lat lng" + latLng);

                if(clickedMarker != null){
                    clickedMarker.remove();
                }

                clickedMarker = mMap.addMarker(new MarkerOptions().position(latLng).title("Clicked Marker"));
                Intent locationInfo = new Intent(MapsActivity.this, LocationInfo.class);
                locationInfo.putExtra("Marked Location",latLng);
                startActivity(locationInfo);
            });
        }
    }

    private void getDeviceLocation(){
        try {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if(location != null){
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();

                    userLocation = new LatLng(latitude, longitude);
                    MarkerOptions markerOptions = new MarkerOptions().position(userLocation).title("Your Location");
                    mMap.addMarker(markerOptions);

                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation,16));
                    flag = false;
                }
            });
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void fetchAndDisplayMarkers(){
        db.collection("water_source_locations").get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                for(QueryDocumentSnapshot documentSnapshot: task.getResult()){
                    String locationName = documentSnapshot.getString("location_name");
                    GeoPoint geoPoint = documentSnapshot.getGeoPoint("location");

                    if(geoPoint != null){
                        LatLng location = new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());

                        mMap.addMarker(new MarkerOptions()
                                .position(location)
                                .title(locationName)
                                .snippet("Available From 8am to 6pm")
                                .icon(BitmapFromVector(getApplicationContext(),R.drawable.water_droplet)));
//                        if(flag){
//                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location,16));
//                        }
                    }
                }
            }
        });
    }

    private void requestDirections(LatLng destination){
        String api = "AIzaSyAkcCYQRWR9NI97hPn3b-l5lGVHAFIm4tc";

        GeoApiContext context = new GeoApiContext.Builder().apiKey(api).build();

        DirectionsApi.newRequest(context)
                .mode(TravelMode.DRIVING)
                .origin(userLocation.latitude + "," + userLocation.longitude)
                .destination(destination.latitude + "," + destination.longitude)
                .setCallback(new PendingResult.Callback<DirectionsResult>() {
                    @Override
                    public void onResult(DirectionsResult result) {
                        Log.d("Direction API"," " + destination);
                        handleDirectionResults(result);
                    }

                    @Override
                    public void onFailure(Throwable e) {
                        Log.e("Directions", "Error fetching directions: " + e.getMessage());
                    }
                });
    }

    private void handleDirectionResults(DirectionsResult result){
        if(result.routes != null && result.routes.length > 0){
            com.google.maps.model.EncodedPolyline polyline = result.routes[0].overviewPolyline;
            List<LatLng> decodedPath = PolyUtil.decode(polyline.getEncodedPath());

            runOnUiThread(() -> currentPolyline = mMap.addPolyline(new PolylineOptions()
                    .addAll(decodedPath)
                    .color(Color.BLUE)
                    .width(5)));
        }
    }

    private BitmapDescriptor
    BitmapFromVector(Context context, int vectorResId){
        Drawable vectorDrawable = ContextCompat.getDrawable(context,vectorResId);

        vectorDrawable.setBounds(0,0,vectorDrawable.getIntrinsicWidth(),vectorDrawable.getIntrinsicHeight());

        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),vectorDrawable.getIntrinsicHeight(),Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);

        vectorDrawable.draw(canvas);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
}