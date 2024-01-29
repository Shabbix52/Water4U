package com.example.water4u;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.HashMap;
import java.util.Map;

public class LocationInfo extends AppCompatActivity {

    LatLng Destination;
    EditText locationName,from,to;
    Button submitButton;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_info);

        Map<String,Object> pendingLocation = new HashMap<>();

        Destination = getIntent().getParcelableExtra("Marked Location");

        locationName = findViewById(R.id.inputLocationName);
        from = findViewById(R.id.inputFrom);
        to = findViewById(R.id.inputTo);
        submitButton = findViewById(R.id.submitButton2);

        Intent mainMenu = new Intent(LocationInfo.this, MainMenu.class);

        submitButton.setOnClickListener(view -> {
            pendingLocation.put("location_name",locationName.getText().toString());
            pendingLocation.put("from",from.getText().toString());
            pendingLocation.put("to",to.getText().toString());
            pendingLocation.put("location", new GeoPoint(Destination.latitude,Destination.longitude));
//            Log.d("HashMap", " " + pendingLocation.get("location_name"));

            db.collection("pending_locations").add(pendingLocation).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                @Override
                public void onSuccess(DocumentReference documentReference) {
                    Log.d("Document", "DocumentSnapshot added with ID: " + documentReference.getId());
                    startActivity(mainMenu);
                }
            });
        });
    }
}