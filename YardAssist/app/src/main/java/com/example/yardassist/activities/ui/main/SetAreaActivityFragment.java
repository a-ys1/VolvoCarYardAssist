package com.example.yardassist.activities.ui.main;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yardassist.R;
import com.example.yardassist.activities.SetAreaActivity;
import com.example.yardassist.classes.Coordinate;
import com.example.yardassist.classes.MapCells;
import com.example.yardassist.classes.docOfCells;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.Executor;


public class SetAreaActivityFragment extends Fragment {

    int mode = 0;
    FirebaseFirestore dbRef;
    MapCells map = new MapCells();
    Coordinate topL = new Coordinate();
    Coordinate topR = new Coordinate();
    Coordinate botL = new Coordinate();
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_set_area_activity, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this.getActivity());

        final Button button = getView().findViewById(R.id.btn_setCoord);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                getLocation();

            }
        });

        final Button upload = getView().findViewById(R.id.upload_button);
        upload.setVisibility(View.INVISIBLE);
        upload.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Coordinate bottomRight = new Coordinate();
                bottomRight.lat = 59.6513;
                bottomRight.lon = 17.0766;
                Coordinate topLeft = new Coordinate();
                topLeft.lat = 59.6522;
                topLeft.lon =  17.0747;
                // uploadToFb(topLeft,bottomRight);
            }
        });

    }

    private double getArea(){
        double a = 0;
        Coordinate d = new Coordinate();
        d.lat = topL.lat;
        d.lon = botL.lon;
        double h = map.getDistanceM(topL.lat, topL.lon, d.lat, d.lon);
        TextView dh = getView().findViewById(R.id.textDistanceH);
        dh.setText("H: " + (Math.round(h * 1000.0) / 1000.0));
        double b = map.getDistanceM(botL.lat, botL.lon, d.lat, d.lon);
        TextView db = getView().findViewById(R.id.textDistanceB);
        db.setText("B: " + (Math.round(b * 1000.0) / 1000.0));
        a = h * b;
        a = Math.round(a * 10000.0) / 10000.0;
        return a;
    }

    private void uploadToFb(Coordinate a, Coordinate b, Coordinate c){
        //GridMap grid;
        /*Coordinate testT = new Coordinate();
        testT.lat = 59.6513;
        testT.lon = 17.0766;
        Coordinate testT2 = new Coordinate();
        testT2.lat = 59.6522;
        testT2.lon =  17.0747; */
        docOfCells sendThis = map.mapCells2(a, b, c);

        dbRef = FirebaseFirestore.getInstance();

        dbRef.collection("map").document("grid").set(sendThis).addOnSuccessListener(new OnSuccessListener<Void>() {
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

        DocumentReference docRef = dbRef.collection("map").document("grid");
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                docOfCells grid = documentSnapshot.toObject(docOfCells.class);
            }
        });


    }

    private void getLocation() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        if(mode == 2){
                            double area = 0;
                            botL.lat = location.getLatitude();
                            botL.lon = location.getLongitude();
                            Toast.makeText(getActivity(), "Bottom Left coordinates received:" + botL.lat + "," + botL.lon, Toast.LENGTH_SHORT).show();
                            TextView show2coord = getView().findViewById(R.id.textBLC);
                            show2coord.setText("Bottom Left: " + botL.lat + ", " + botL.lon);
                            TextView cGuide = getView().findViewById(R.id.textCornerG);
                            cGuide.setText("Done!");
                            Button setBtn = getView().findViewById(R.id.btn_setCoord);
                            setBtn.setClickable(false);

                            uploadToFb(topL, topR, botL);
                            area = getArea();
                            TextView showArea = getView().findViewById(R.id.showArea);
                            showArea.setText(area + " m2");
                            mode = 0;
                        }
                        else if(mode == 1){
                            topR.lat = location.getLatitude();
                            topR.lon = location.getLongitude();
                            TextView show1coord = getView().findViewById(R.id.textTRC);
                            show1coord.setText("Top Right: " + topR.lat + ", " + topR.lon );
                            Toast.makeText(getActivity(), "Top Right Coordinates received:" + topR.lat + "," + topR.lon, Toast.LENGTH_SHORT).show();
                            TextView cGuide = getView().findViewById(R.id.textCornerG);
                            cGuide.setText("Bottom Left");
                            mode = 2;
                        }
                        else if(mode == 0){
                            topL.lat = location.getLatitude();
                            topL.lon = location.getLongitude();
                            TextView show1coord = getView().findViewById(R.id.textTLC);
                            show1coord.setText("Top Left: " + topL.lat + ", " + topL.lon );
                            Toast.makeText(getActivity(), "Top Left Coordinates received:" + topL.lat + "," + topL.lon, Toast.LENGTH_SHORT).show();
                            TextView cGuide = getView().findViewById(R.id.textCornerG);
                            cGuide.setText("Top Right");
                            mode = 1;
                        }
                    } else {

                        Toast.makeText(getActivity(), "There is no location data! (Location == null)", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        else {

            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

    }

    //To get permission for location
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getActivity(), "Permission granted", Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(getActivity(), "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }

    }

}