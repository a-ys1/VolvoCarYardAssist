package com.example.yardassist.activities.ui.main;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yardassist.R;
import com.example.yardassist.activities.NewTaskActivity;
import com.example.yardassist.activities.OperatorActivity;
import com.example.yardassist.classes.Cell;
import com.example.yardassist.classes.Coordinate;
import com.example.yardassist.classes.MapCells;
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


public class
FragmentRegisterVehicle extends Fragment {

    FirebaseFirestore dbRef;
    private FusedLocationProviderClient fusedLocationClient;
    LocationRequest locationRequest;
    LocationCallback locationCallback;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbRef = FirebaseFirestore.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this.getActivity());
        startUpdateLocation();
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

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_register_vehicle, container, false);


    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        final Button registerVehicleFrag = getView().findViewById(R.id.registerButtonFrag);
        registerVehicleFrag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getVehicleInformation();
            }
        });

        super.onViewCreated(view, savedInstanceState);
    }

    private void registerVehicle(Vehicle vehicle) {

        dbRef.collection("vehicles").document(vehicle.getRegNumber()).set(vehicle).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("Project3", "DocumentSnapshot successfully written!");
                EditText regNumber = getView().findViewById(R.id.regNumberFrag);
                EditText vehicleColor = getView().findViewById(R.id.vehicleColorFrag);
                EditText vehicleModel = getView().findViewById(R.id.vehicleModelFrag);
                EditText vehicleYear = getView().findViewById(R.id.vehicleYearFrag);
                regNumber.setText("");
                vehicleColor.setText("");
                vehicleModel.setText("");
                vehicleYear.setText("");
                Toast.makeText(getActivity(), "Vehicle registered", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w("ERROR", "Error writing document", e);
            }
        });
    }

    private void getVehicleInformation() {

        EditText regNumber = getView().findViewById(R.id.regNumberFrag);
        EditText vehicleColor = getView().findViewById(R.id.vehicleColorFrag);
        EditText vehicleModel = getView().findViewById(R.id.vehicleModelFrag);
        EditText vehicleYear = getView().findViewById(R.id.vehicleYearFrag);

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

        getCurrentLocationOfVehicle(new Vehicle(color, regNr, model, year));

    }

    private double getTArea(double a, double b, double c) {
        double s = (a + b + c) / 2;
        double t = s * (s - a) * (s - b) * (s - c);
        double tA = Math.sqrt(t);
        if (Double.isNaN(tA)) {
            return 0;
        } else {
            return tA;
        }

    }

    private boolean checkIfVehicleIsInCell(Cell cell, Coordinate coord) {
        double triArea;
        double h = MapCells.getDistanceM(cell.topLeft.lat, cell.topLeft.lon, cell.bottomLeft.lat, cell.bottomLeft.lon); //side top-left to bottom-left
        double b = MapCells.getDistanceM(cell.bottomLeft.lat, cell.bottomLeft.lon, cell.bottomRight.lat, cell.bottomRight.lon); //side bottom-left to bottom-right
        double d = MapCells.getDistanceM(cell.topLeft.lat, cell.topLeft.lon, cell.bottomRight.lat, cell.bottomRight.lon); //diagonal
        triArea = (h * b);

        double p1 = MapCells.getDistanceM(cell.topLeft.lat, cell.topLeft.lon, coord.lat, coord.lon); //distance from point to top-left
        double p2 = MapCells.getDistanceM(cell.bottomLeft.lat, cell.bottomLeft.lon, coord.lat, coord.lon); //distance from point to bottom-left
        double p3 = MapCells.getDistanceM(cell.bottomRight.lat, cell.bottomRight.lon, coord.lat, coord.lon); //distance form point to bottom-right
        double p4 = MapCells.getDistanceM(cell.topRight.lat, cell.topRight.lon, coord.lat, coord.lon); //distance from point to top-right

        double tA1 = getTArea(p1, p2, h);
        double tA2 = getTArea(p2, p3, b);
        double tA3 = getTArea(p3, p4, h);
        double tA4 = getTArea(p1, p4, b);

        double tA = tA1 + tA2 + tA3 + tA4;
        triArea = (double) Math.floor(triArea * 1d) / 1d;
        tA = (double) Math.floor(tA * 1d) / 1d;
        if (triArea == tA && triArea != 0.0) {
            return true;
        } else {
            return false;
        }


    }

    private void getCurrentLocationOfVehicle(Vehicle vehicle) {
        if (ContextCompat.checkSelfPermission(this.getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this.getActivity(), new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        Coordinate coord = new Coordinate(location.getLatitude(), location.getLongitude());
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
                                            vehicle.setCol(j);
                                            vehicle.setRow(i);
                                            breakLoop = true;
                                            break;
                                        }
                                    }
                                    if(breakLoop) break;
                                }

                                vehicle.setLatitude(coord.lat);
                                vehicle.setLongitude(coord.lon);
                                registerVehicle(vehicle);

                            }
                        });
                    } else {
                        Toast.makeText(getActivity(), "Location == null!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
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

    private void startUpdateLocation() {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(1000);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        SettingsClient client = LocationServices.getSettingsClient(this.getActivity());
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this.getActivity(), new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                } else {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                }
            }
        });
        task.addOnFailureListener(getActivity(), new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    try {
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(getActivity(), 1001);
                    } catch (IntentSender.SendIntentException sendEx) {
                        sendEx.printStackTrace();
                    }
                }
            }
        });
    }

    //To get permission for location
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this.getActivity(), "Permission granted", Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(this.getActivity(), "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }

    }

}