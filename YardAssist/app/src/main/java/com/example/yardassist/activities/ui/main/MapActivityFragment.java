package com.example.yardassist.activities.ui.main;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.collection.ArraySet;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;


import com.example.yardassist.R;
import com.example.yardassist.activities.MapActivity;
import com.example.yardassist.activities.NewTaskActivity;
import com.example.yardassist.classes.Cell;
import com.example.yardassist.classes.Coordinate;
import com.example.yardassist.classes.CustomMap;
import com.example.yardassist.classes.Taskitem;
import com.example.yardassist.classes.MapCells;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.nio.MappedByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.Executor;

public class MapActivityFragment extends Fragment {

    private CustomMap mCustomMap;
    docOfCells grid;
    private float startXOnTouch, startYOnTouch, viewStartX, viewStartY;
    boolean clicked = false;
    boolean cellPicked = false;

    private FusedLocationProviderClient fusedLocationClient;
    LocationRequest locationRequest;
    LocationCallback locationCallback;

    FirebaseFirestore dbRef;
    DocumentReference docRef;
    FirebaseAuth fAuth;
    Cell currentCell;
    String userId;

    boolean startedFromNewTaskActivity = false;
    private Taskitem task;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map_activity, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @androidx.annotation.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mCustomMap = getView().findViewById(R.id.customView);
        //TextView testTextView = getView().findViewById(R.id.testText);

        dbRef = FirebaseFirestore.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this.getActivity());
        fAuth = FirebaseAuth.getInstance();
        userId = fAuth.getInstance().getCurrentUser().getUid();

        docRef = dbRef.collection("map").document("grid");
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                grid = documentSnapshot.toObject(docOfCells.class);

                for (int i = 0; i < grid.rows; i++) {
                    for (int j = 0; j < grid.columns; j++) {
                        if (grid.cells.get(i).cells.get(j).users.contains(userId)) {
                            grid.cells.get(i).cells.get(j).users.remove(userId);
                            if (grid.cells.get(i).cells.get(j).users.isEmpty()) {
                                grid.cells.get(i).cells.get(j).occupiedByOperator = false;
                            }
                        }
                    }
                }

                //Get current cell for the operator
                Coordinate placeholderCoordinate = new Coordinate(0.0, 0.0);
                currentCell = new Cell(placeholderCoordinate, placeholderCoordinate, placeholderCoordinate, placeholderCoordinate);
                startUpdateLocation();

                //Get task item location and start path finding algorithm
                Intent i = getActivity().getIntent();
                Taskitem task = (Taskitem) i.getSerializableExtra("item");
                if (task != null) {
                    //Coordinate end = new Coordinate(task.getLatitude(), task.getLongitude());
                    int endCol = task.getCol();
                    int endRow = task.getRow();
                    pathFinding(endCol, endRow);
                } else {
                    mCustomMap.setGrid(grid);
                    mCustomMap.drawMap();
                }

            }


        });

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    String currentDateTimeString = java.text.DateFormat.getDateTimeInstance().format(new Date());
                    //testTextView.setText("Current Location" + "Latitude:" + location.getLatitude() + "\n" + "Longitude:" + location.getLongitude() + "\n" + currentDateTimeString);

                    /*Check first if user is in current cell in order to not loop through map every time, will be false first time and every time user has moved from one cell*/
                    if (checkIfUserIsInCell(currentCell, new Coordinate(location.getLatitude(), location.getLongitude()))) {
                        return;
                    }

                    /*If user is not in current cell -> reset "current" cell and find the cell that the user is in.*/
                    else {
                        /*Reset current cell if user is in another cell which is not the placeholder cell*/
                        if (currentCell.topLeft.lat != 0.0 && currentCell.bottomRight.lat != 0.0 && currentCell.topLeft.lon != 0.0 && currentCell.bottomRight.lon != 0.0) {
                            grid.cells.get(currentCell.mRowIndex).cells.get(currentCell.mColumnIndex).users.remove(userId);
                            if (grid.cells.get(currentCell.mRowIndex).cells.get(currentCell.mColumnIndex).users.isEmpty()) {
                                grid.cells.get(currentCell.mRowIndex).cells.get(currentCell.mColumnIndex).occupiedByOperator = false;
                            }
                        }
                        boolean breakLoop = false;
                        for (int i = 0; i < grid.rows; ++i) {
                            for (int j = 0; j < grid.columns; ++j) {
                                if (checkIfUserIsInCell(grid.cells.get(i).cells.get(j), new Coordinate(location.getLatitude(), location.getLongitude()))) {
                                    grid.cells.get(i).cells.get(j).occupiedByOperator = true;
                                    grid.cells.get(i).cells.get(j).users.add(userId);
                                    currentCell = grid.cells.get(i).cells.get(j);
                                    updateGrid(grid);
                                    breakLoop = true;
                                    break;
                                }
                            }
                            if (breakLoop) break;
                        }
                    }
                }
            }
        };






        mCustomMap.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        clicked = true;
                        startXOnTouch = event.getRawX();        //get the x value of the first "touch" on the screen
                        startYOnTouch = event.getRawY();        //get the y value of the first "touch" on the screen
                        viewStartX = v.getX();                  //get mCustomMap starting x coordinate
                        viewStartY = v.getY();                  //get mCustomMap starting y coordinate
                        break;
                    case MotionEvent.ACTION_MOVE:
                        clicked = false;
                        float moveToX = viewStartX + (event.getRawX() - startXOnTouch);         //Calc the new x coordinate for mCustomMap
                        float moveToY = viewStartY + (event.getRawY() - startYOnTouch);         //Calc the new y coordinate for mCustomMap

                        float maxScreenWidth = getView().findViewById(R.id.parentRelative).getWidth();    //Get width of the RelativeLayout that contains mCustomMap
                        float maxScreenHeight = getView().findViewById(R.id.parentRelative).getHeight();  //Get height of the RelativeLayout that contains mCustomMap
                        //Make sure the mCustomMap covers the hole "screen"
                        if (moveToX >= 0.0)
                            moveToX = 0;
                        if (moveToY >= 0)
                            moveToY = 0;
                        float w = ((CustomMap) v).viewWidth;
                        float h = ((CustomMap) v).viewHeight;
                        if (moveToX + w <= maxScreenWidth)
                            moveToX = maxScreenWidth - w;
                        if (moveToY + h <= maxScreenHeight)
                            moveToY = maxScreenHeight - h;

                        v.animate().x(moveToX).y(moveToY).setDuration(0).start();           //move the mCustomMap to new (x,y) location
                        break;
                    case MotionEvent.ACTION_UP:
                        if (!clicked || !startedFromNewTaskActivity)
                            break;
                        double x = startXOnTouch - viewStartX;
                        double y = startYOnTouch - viewStartY;

                        int col = (int) Math.ceil(x / mCustomMap.getRectWidth()) - 1;
                        int row = (int) Math.ceil(y / mCustomMap.getRectWidth()) - 1;
                        mCustomMap.taskMap = true;
                        mCustomMap.pickedCol = col;
                        mCustomMap.pickedRow = row;
                        task.setCol(col);
                        task.setRow(row);
                        mCustomMap.drawMap();
                        clicked = false;
                        cellPicked = true;
                        break;
                    default:
                        return false;
                }
                return true;
            }
        });

        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("FAILED1", "Listen failed.", e);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    docOfCells grid = snapshot.toObject(docOfCells.class);
                    mCustomMap.setGrid(grid);
                    mCustomMap.drawMap();
                } else {
                    Log.d("Success2", "Current data: null");
                }
            }
        });
    }

    private void startUpdateLocation() {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(500);
        locationRequest.setFastestInterval(500);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        SettingsClient client = LocationServices.getSettingsClient(getActivity());
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(getActivity(), new OnSuccessListener<LocationSettingsResponse>() {
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

    private void getLocation() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener((Executor) this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        Toast.makeText(getActivity(), "Location fetched", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), "Location == null!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
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

    private double calcTriArea(Coordinate A, Coordinate B, Coordinate C) {
        return Math.abs((A.lon * (B.lat - C.lat) + B.lon * (C.lat - A.lat) + C.lon * (A.lat - B.lat)) / 2.0);
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

    private boolean checkIfUserIsInCell(Cell cell, Coordinate coord) {
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

    private float heuristicCal(Cell current, Cell end) {
        return Math.abs(current.mRowIndex - end.mRowIndex) + Math.abs(current.mColumnIndex - end.mColumnIndex); //estimated distance to end cell
    }

    public void pathFinding(int endCol, int endRow) {
        ArrayList<Cell> openSet = new ArrayList<Cell>();
        ArrayList<Cell> closedSet = new ArrayList<Cell>();

        /*Cell start, end;
        docOfCells grid = mCustomMap.getGrid();
        for (int i = 0; i < grid.rows; ++i) {
            for (int j = 0; j < grid.columns; ++j) {
                if (checkIfUserIsInCell(grid.cells.get(i).cells.get(j), endCell)) {
                    end = grid.cells.get(i).cells.get(j);
                }
            }
        }*/

        Cell start = grid.cells.get(1).cells.get(2);//currentCell;
        Cell end = grid.cells.get(endRow).cells.get(endCol);        //The cell we want to find a path to

        //Start Cell
        openSet.add(start);
        while (!openSet.isEmpty()) {
            int index = 0;
            for (int i = 0; i < openSet.size(); i++)        //loop through openSet and find the cell with the best f value
            {
                if (openSet.get(i).mF < openSet.get(index).mF) {
                    index = i;
                }
            }
            Cell current = openSet.get(index);      //set current to be the cell with best f value
            current.addNeighbor(grid, end);
            if (current == end) {
                //Find path
                grid.cells.get(current.mRowIndex).cells.get(current.mColumnIndex).isPath = true;
                Cell tmp = current;
                while (tmp.previous != null) {
                    tmp = tmp.previous;
                    grid.cells.get(tmp.mRowIndex).cells.get(tmp.mColumnIndex).isPath = true;
                }
                mCustomMap.setGrid(grid);
                //mCustomMap.pathActive = true;
                mCustomMap.drawMap();
            }
            openSet.remove(current);                    //remove current cell from openSet
            closedSet.add(current);                     //add it to closedSet

            ArrayList<Cell> neighbors = current.getNeighbors(); //Get collection of neighbors of current cell
            for (int i = 0; i < neighbors.size(); ++i) {       //Go through them
                Cell neighbor = neighbors.get(i);              //Current neighbor
                if (!closedSet.contains(neighbor)) {             //If the neighbor is not in the closedSet
                    //G cost calc
                    float tempG = current.mG + 1;              //Calc neighbor g
                    if (openSet.contains(neighbor)) {            //If neighbor cell exists in openSet
                        if (tempG < neighbor.mG)                //then check if tempG is lower than current g
                            neighbor.mG = tempG;               //and change the cell's g
                    } else {
                        neighbor.mG = tempG;                   //If not in the openSet, set the g value
                        openSet.add(neighbor);                 //and push to the openSet
                    }
                    neighbor.mH = heuristicCal(neighbor, end); //estimated distance to end cell
                    neighbor.mF = neighbor.mG + neighbor.mH;
                    neighbor.previous = current;
                }
            }
        }

    }



}