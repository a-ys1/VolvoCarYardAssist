package com.example.yardassist.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.example.yardassist.R;
import com.example.yardassist.classes.Cell;
import com.example.yardassist.classes.Coordinate;
import com.example.yardassist.classes.Vehicle;
import com.example.yardassist.classes.docOfCells;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


public class OperatorActivity extends AppCompatActivity {

    FirebaseFirestore dbRef;
    private FusedLocationProviderClient fusedLocationClient;
    LocationRequest locationRequest;
    LocationCallback locationCallback;
    Double latitude, longitude;
    Button locationButton, registerButton, searchButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_vehicle);

        locationButton = findViewById(R.id.getLocation);
        registerButton = findViewById(R.id.registerButton);
        searchButton = findViewById(R.id.searchButton);
        dbRef = FirebaseFirestore.getInstance();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {

                }
            }
        };

        startUpdateLocation();

        /*locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLocation();
            }
        });*/

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLocation();
                /*EditText regNumber = findViewById(R.id.regNumber);
                EditText vehicleColor = findViewById(R.id.vehicleColor);
                EditText vehicleModel = findViewById(R.id.vehicleModel);
                EditText vehicleYear = findViewById(R.id.vehicleYear);

                boolean badInputField = false;

                String regNr = regNumber.getText().toString().trim();           //Check reg number
                if (TextUtils.isEmpty(regNr)) {
                    regNumber.setError("This field cannot be empty");
                    badInputField = true;
                }
                String color = vehicleColor.getText().toString().trim();        //Check color
                if (TextUtils.isEmpty(color) || TextUtils.isDigitsOnly(color)) {
                    vehicleColor.setError("This field cannot be empty and cannot be numbers");
                    badInputField = true;
                }
                String model = vehicleModel.getText().toString().trim();        //Check model
                if (TextUtils.isEmpty(model)) {
                    vehicleModel.setError("This field cannot be empty");
                    badInputField = true;
                }
                String year = vehicleYear.getText().toString().trim();          //Check year
                if (TextUtils.isEmpty(year)) {
                    vehicleYear.setError("This field cannot be empty");
                    badInputField = true;
                }
                if (badInputField) {

                    return;
                }

                locationButton.performClick();
                getLocation();
                TextView showLocation = findViewById(R.id.showLocation);
                showLocation.setText("Location: " + latitude + ", " + longitude);
                Toast.makeText(OperatorActivity.this, "Location success!", Toast.LENGTH_SHORT).show();
                registerVehicle(color, regNr, model, year, latitude, longitude);*/
            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vehicle vehicle = new Vehicle("red", "III111", "Volvo", "1333", 59.6522, 17.0747);
                dropOffVehicle(vehicle);
                //searchVehicle();
            }
        });

        final Button testArea = findViewById(R.id.testAreaButton);
        testArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OperatorActivity.this, SetAreaActivity.class);
                startActivity(intent);
            }
        });
    }

    private void registerVehicle(String color, String regNr, String model, String year, Double latitude, Double longitude) {

        EditText regNumber = findViewById(R.id.regNumber);
        EditText vehicleColor = findViewById(R.id.vehicleColor);
        EditText vehicleModel = findViewById(R.id.vehicleModel);
        EditText vehicleYear = findViewById(R.id.vehicleYear);

        regNr = regNumber.getText().toString().trim();
        color = vehicleColor.getText().toString().trim();
        model = vehicleModel.getText().toString().trim();
        year = vehicleYear.getText().toString().trim();

        Vehicle vehicle = new Vehicle(color, regNr, model, year, latitude, longitude);

        dropOffVehicle(vehicle);

        dbRef.collection("vehicles").document(regNr).set(vehicle).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("Project3", "DocumentSnapshot successfully written!");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w("ERROR", "Error writing document", e);
            }
        });
    }


    private void searchVehicle() {
        dbRef = FirebaseFirestore.getInstance();

        EditText search = findViewById(R.id.search);

        String searchID = search.getText().toString().trim();
        DocumentReference docRef = dbRef.collection("vehicles").document(searchID);
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Vehicle vehicle = documentSnapshot.toObject(Vehicle.class);
                EditText model = findViewById(R.id.getModel);
                model.setText(vehicle.getModel());
            }
        });
    }

    private void startUpdateLocation() {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(1000);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        SettingsClient client = LocationServices.getSettingsClient(OperatorActivity.this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                if (ContextCompat.checkSelfPermission(OperatorActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                } else {
                    ActivityCompat.requestPermissions(OperatorActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                }
            }
        });
        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    try {
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(OperatorActivity.this, 1001);
                    } catch (IntentSender.SendIntentException sendEx) {
                        sendEx.printStackTrace();
                    }
                }
            }
        });
    }

    private void stopUpdateLocation() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    private void getLocation() {
        if (ContextCompat.checkSelfPermission(OperatorActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        Long time = location.getTime();
                        TextView showLocation = findViewById(R.id.showLocation);
                        EditText regNumber = findViewById(R.id.regNumber);
                        EditText vehicleColor = findViewById(R.id.vehicleColor);
                        EditText vehicleModel = findViewById(R.id.vehicleModel);
                        EditText vehicleYear = findViewById(R.id.vehicleYear);

                        boolean badInputField = false;

                        String regNr = regNumber.getText().toString().trim();           //Check reg number
                        if (TextUtils.isEmpty(regNr)) {
                            regNumber.setError("This field cannot be empty");
                            badInputField = true;
                        }
                        String color = vehicleColor.getText().toString().trim();        //Check color
                        if (TextUtils.isEmpty(color) || TextUtils.isDigitsOnly(color)) {
                            vehicleColor.setError("This field cannot be empty and cannot be numbers");
                            badInputField = true;
                        }
                        String model = vehicleModel.getText().toString().trim();        //Check model
                        if (TextUtils.isEmpty(model)) {
                            vehicleModel.setError("This field cannot be empty");
                            badInputField = true;
                        }
                        String year = vehicleYear.getText().toString().trim();          //Check year
                        if (TextUtils.isEmpty(year)) {
                            vehicleYear.setError("This field cannot be empty");
                            badInputField = true;
                        }
                        if (badInputField) {
                            return;
                        }

                        showLocation.setText("Location: " + latitude + ", " + longitude);
                        Toast.makeText(OperatorActivity.this, "Location success!", Toast.LENGTH_SHORT).show();
                        registerVehicle(color, regNr, model, year, latitude, longitude);
                    } else {
                        Toast.makeText(OperatorActivity.this, "Location == null!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            ActivityCompat.requestPermissions(OperatorActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    private void dropOffVehicle(Vehicle vehicle) {
        if (ContextCompat.checkSelfPermission(OperatorActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        Coordinate coord = new Coordinate(59.651381, 17.0747);//(location.getLatitude(), location.getLongitude());
                        //Get Grid from firebase
                        DocumentReference docRef = dbRef.collection("map").document("grid");
                        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                docOfCells grid = documentSnapshot.toObject(docOfCells.class);
                                boolean breakLoop = false;
                                for (int i = 0; i < grid.rows; ++i) {
                                    for (int j = 0; j < grid.columns; ++j) {
                                        if (checkIfVehicleIsInCell(grid.cells.get(i).cells.get(j), coord)) {
                                            grid.cells.get(i).cells.get(j).occupiedByVehicle = true;
                                            updateGrid(grid);
                                            breakLoop = true;
                                            break;
                                        }
                                    }
                                    if(breakLoop) break;
                                }
                                updateVehicleLocation(vehicle, coord);
                            }
                        });
                    } else {
                        Toast.makeText(OperatorActivity.this, "Location == null!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private boolean checkIfVehicleIsInCell(Cell cell, Coordinate coord) {
        double right = Math.max(cell.topLeft.lon, cell.bottomRight.lon);
        double left = Math.min(cell.topLeft.lon, cell.bottomRight.lon);
        double top = Math.max(cell.topLeft.lat, cell.bottomRight.lat);
        double bottom = Math.min(cell.topLeft.lat, cell.bottomRight.lat);

        if(left <= coord.lon && coord.lon <= right)
            if(top >= coord.lat && coord.lat >= bottom)
                return true;
        /*if(top >= coord.lat && coord.lat >= bottom){
            if(left <= right && left <= coord.lon && coord.lon <= right){
                return true;
            } else if(left > right && (left <= coord.lon || coord.lon <= right)) {
                return true;
            }
        }*/
        return false;

    }


    private void updateVehicleLocation(Vehicle vehicle, Coordinate coord) {
        vehicle.setLatitude(coord.lat);
        vehicle.setLongitude(coord.lon);
        dbRef.collection("vehicles").document(vehicle.getRegNumber()).set(vehicle).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("Project3", "DocumentSnapshot successfully written!");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w("ERROR", "Error writing document", e);
            }
        });
    }


    private void updateGrid(docOfCells grid) {
        dbRef.collection("map").document("grid").set(grid).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("Project3", "DocumentSnapshot successfully written!");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w("ERROR", "Error writing document", e);
            }
        });
    }

    //To get permission for location
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(OperatorActivity.this, "Permission granted", Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(OperatorActivity.this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }

    }
}
