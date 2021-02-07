package com.example.yardassist.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.collection.ArraySet;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
import com.example.yardassist.activities.ui.main.popWarning_wrongDropOff;
import com.example.yardassist.classes.Cell;
import com.example.yardassist.classes.Coordinate;
import com.example.yardassist.classes.CustomMap;
import com.example.yardassist.classes.Taskitem;
import com.example.yardassist.classes.MapCells;
import com.example.yardassist.classes.Vehicle;
import com.example.yardassist.classes.docOfCells;
import com.example.yardassist.classes.rowOfCells;
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

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.nio.MappedByteBuffer;
import java.util.ArrayList;
import java.util.Date;

public class MapActivity extends AppCompatActivity implements popWarning_wrongDropOff.popDropOffWarningListener {

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

    boolean startedFromNewOperator = false;
    boolean startedFromNewManager = false;
    boolean startedFromNewTaskActivity = false;
    private Taskitem task;

    boolean showPath = false;
    boolean pickedUpVehicle = false;
    Vehicle movingVehicle;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        findViewById(R.id.pickupVehicle).setVisibility(View.INVISIBLE);
        findViewById(R.id.dropOffVehicle).setVisibility(View.INVISIBLE);
        mCustomMap = findViewById(R.id.customView);
        //TextView testTextView = findViewById(R.id.testText);

        dbRef = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();
        userId = fAuth.getInstance().getCurrentUser().getUid();



        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        //If sctivity started from NewTaskActivity class
        Intent intent = this.getIntent();
        if (intent != null) {
            String ID = intent.getExtras().getString("activity_ID");
            if (ID.equals("NewTaskActivity")) {
                startedFromNewTaskActivity = true;
                task = (Taskitem) intent.getSerializableExtra("task");

            } else {
                findViewById(R.id.setCoordinates).setVisibility(View.INVISIBLE);
            }
            if (ID.equals("activities.NewManager")) {
                startedFromNewManager = true;
            } else if (ID.equals("activities.NewOperator")) {
                startedFromNewOperator = true;
            }
        }


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        //Get grid form Database
        //Check if the pathFinding algorithm should run
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
                Intent i = getIntent();
                Taskitem tmp = (Taskitem) i.getSerializableExtra("item");
                if (tmp != null) {
                    task = tmp;
                    //Find path to vehicle - Niklas
                    DocumentReference dbVehicle = dbRef.collection("vehicles").document(task.getId().toString());
                    dbVehicle.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            movingVehicle = documentSnapshot.toObject(Vehicle.class);
                            if (movingVehicle == null) {
                                Toast.makeText(MapActivity.this, "Could not find vehicle in database", Toast.LENGTH_SHORT).show();
                                mCustomMap.setGrid(grid);
                                mCustomMap.drawMap();
                                return;
                            }
                            if (movingVehicle.getCol() != -1 && movingVehicle.getRow() != -1) {
                                showPath = true;
                                findViewById(R.id.pickupVehicle).setVisibility(View.VISIBLE);
                            } else {
                                Toast.makeText(MapActivity.this, "Vehicle has no  known location", Toast.LENGTH_SHORT).show();
                                mCustomMap.setGrid(grid);
                                mCustomMap.drawMap();
                            }
                        }
                    });

                } else {
                    mCustomMap.setGrid(grid);
                    mCustomMap.drawMap();
                }

            }


        });



        /*Updates the users location about every second*/
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    String currentDateTimeString = java.text.DateFormat.getDateTimeInstance().format(new Date());
                   // testTextView.setText("Current Location" + "Latitude:" + location.getLatitude() + "\n" + "Longitude:" + location.getLongitude() + "\n" + currentDateTimeString);

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
                        grid.cells.get(currentCell.mRowIndex).cells.get(currentCell.mColumnIndex).isPath = false;
                        for (int i = 0; i < grid.rows; ++i) {
                            for (int j = 0; j < grid.columns; ++j) {
                                if (checkIfUserIsInCell(grid.cells.get(i).cells.get(j), new Coordinate(location.getLatitude(), location.getLongitude()))) {
                                    grid.cells.get(i).cells.get(j).occupiedByOperator = true;
                                    grid.cells.get(i).cells.get(j).users.add(userId);
                                    currentCell = grid.cells.get(i).cells.get(j);
                                    updateGrid(grid);
                                    if(showPath)
                                    {
                                        int endCol;
                                        int endRow;
                                        if(pickedUpVehicle != true){
                                             endCol = movingVehicle.getCol();
                                             endRow = movingVehicle.getRow();

                                        }
                                        else{
                                             endCol = task.getCol();
                                             endRow = task.getRow();
                                        }
                                        pathFinding(endCol, endRow);
                                    }
                                    breakLoop = true;
                                    break;
                                }
                            }
                            if (breakLoop) break;
                        }
                        int t = 5;
                    }
                }
            }
        };

        /*findViewById(R.id.swap_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Coord to check if in cell
                Coordinate check = new Coordinate(59.60146821372726, 16.51372135255151);
                //Cell to check if contains
                // Cell cellContains = new Cell(new Coordinate(59.601577, 16.513728), new Coordinate(59.601371, 16.514013));

               /* if(checkIfUserIsInCell(cellContains, check)){
                    int k = 5;
                } */


                //Get coordinates of end cell
                //Coordinate end = new Coordinate();

                //pathFinding(end);
          //  }
       // });

        findViewById(R.id.setCoordinates).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCustomMap.taskMap) {
                    if (cellPicked) {
                        Intent intent = new Intent(MapActivity.this, NewTaskActivity.class);
                        intent.putExtra("activity_ID", "MapActivity");
                        intent.putExtra("task", task);
                        startActivity(intent);
                    }
                } else if (startedFromNewTaskActivity) {
                    //Toast some message, no cell is picked or it's a invalid one
                    Toast.makeText(MapActivity.this, "No cell has been picked", Toast.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.pickupVehicle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCustomMap.removePath();
                if (task.getCol() == -1 || task.getRow() == -1) {
                    Toast.makeText(MapActivity.this, "No given drop off location", Toast.LENGTH_SHORT).show();
                    mCustomMap.drawMap();
                } else {
                    pickedUpVehicle = true;
                    pathFinding(task.getCol(), task.getRow());
                }
                findViewById(R.id.pickupVehicle).setVisibility(View.INVISIBLE);
                findViewById(R.id.dropOffVehicle).setVisibility(View.VISIBLE);
            }
        });

        findViewById(R.id.dropOffVehicle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentCell.mRowIndex != task.getRow() || currentCell.mColumnIndex != task.getCol()) {
                    popWarning_wrongDropOff popup = new popWarning_wrongDropOff(movingVehicle);
                    popup.show(MapActivity.this.getSupportFragmentManager(), "hello");
                } else {
                    DocumentReference dbTask = dbRef.collection("tasks").document(task.getId());
                    onDropOff(movingVehicle);
                    dbTask.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            return;
                        }
                    });
                    Toast.makeText(MapActivity.this, "Task Completed", Toast.LENGTH_SHORT).show();
                    if (startedFromNewManager) {
                        Intent intent = new Intent(MapActivity.this, NewManager.class);
                        startActivity(intent);
                    } else if (startedFromNewOperator) {
                        Intent intent = new Intent(MapActivity.this, NewOperator.class);
                        startActivity(intent);
                    }

                }

            }
        });


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

                        float maxScreenWidth = findViewById(R.id.parentRelative).getWidth();    //Get width of the RelativeLayout that contains mCustomMap
                        float maxScreenHeight = findViewById(R.id.parentRelative).getHeight();  //Get height of the RelativeLayout that contains mCustomMap
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
        SettingsClient client = LocationServices.getSettingsClient(MapActivity.this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                if (ContextCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                } else {
                    ActivityCompat.requestPermissions(MapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                }
            }
        });
        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    try {
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(MapActivity.this, 1001);
                    } catch (IntentSender.SendIntentException sendEx) {
                        sendEx.printStackTrace();
                    }
                }
            }
        });
    }

    private void getLocation() {
        if (ContextCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        Toast.makeText(MapActivity.this, "Location fetched", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MapActivity.this, "Location == null!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            ActivityCompat.requestPermissions(MapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
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

    private docOfCells copyGrid(docOfCells from){
        docOfCells to = new docOfCells();
        for(int i = 0; i < from.rows; i++){
            rowOfCells row = new rowOfCells();
            for(int j = 0; j < from.columns; j++){


                row.cells.add(new Cell(from.cells.get(i).cells.get(j).mRowIndex, from.cells.get(i).cells.get(j).mColumnIndex, from.cells.get(i).cells.get(j).occupiedByOperator, from.cells.get(i).cells.get(j).occupiedByVehicle  ));

            }
            row.row = i;
            to.cells.add(row);
        }
        to.rows = from.rows;
        to.columns = from.columns;
        return to;
    }


    public void pathFinding(int endCol, int endRow) {

        docOfCells pathFindingGrid = copyGrid(grid);



        ArrayList<Cell> openSet = new ArrayList<Cell>();
        ArrayList<Cell> closedSet = new ArrayList<Cell>();

        Cell start = pathFindingGrid.cells.get(currentCell.mRowIndex).cells.get(currentCell.mColumnIndex);
        Cell end = pathFindingGrid.cells.get(endRow).cells.get(endCol);        //The cell we want to find a path to

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
            current.addNeighbor(pathFindingGrid, end);
            if (current == end) {
                //Find path
                pathFindingGrid.cells.get(current.mRowIndex).cells.get(current.mColumnIndex).isPath = true;
                Cell tmp = current;
                while (tmp.previous != null) {
                    tmp = tmp.previous;
                    pathFindingGrid.cells.get(tmp.mRowIndex).cells.get(tmp.mColumnIndex).isPath = true;
                }
                mCustomMap.setPathGrid(pathFindingGrid);
               // mCustomMap.pathActive = true;
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

    @Override
    public void onDropOff(Vehicle vehicle) {
        vehicle.setCol(currentCell.mColumnIndex);
        vehicle.setRow(currentCell.mRowIndex);
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
        Toast.makeText(MapActivity.this, "Vehicle location changed", Toast.LENGTH_SHORT).show();
        if (startedFromNewManager) {
            Intent intent = new Intent(MapActivity.this, NewManager.class);
            startActivity(intent);
        } else if (startedFromNewOperator) {
            Intent intent = new Intent(MapActivity.this, NewOperator.class);
            startActivity(intent);
        }
    }

    //To get permission for location
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MapActivity.this, "Permission granted", Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(MapActivity.this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

}


