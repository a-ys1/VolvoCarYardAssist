package com.example.yardassist.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import com.example.yardassist.R;
import com.example.yardassist.activities.ui.main.FragmentTaskList;
import com.example.yardassist.activities.ui.main.popWarning_noEndCell;
import com.example.yardassist.activities.ui.main.poptask;
import com.example.yardassist.classes.Taskitem;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import com.google.firebase.firestore.FirebaseFirestore;

import java.sql.Timestamp;


public class NewTaskActivity extends AppCompatActivity implements popWarning_noEndCell.popWarningListener {

    FirebaseFirestore dbRef;
    Button registerTask;


    int endCellCol = -1, endCellRow = -1;
    String id, comment;
    double latitude, longitude;
    Timestamp timestamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_task);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        //If activity started from MapActivity class
        Intent intent = this.getIntent();
        if (intent != null) {
            String ID = intent.getExtras().getString("activity_ID");
            if (ID.equals("MapActivity")) {
                EditText vehicleID = findViewById(R.id.vehicleID);
                EditText additionalComment = findViewById(R.id.additionalComment);

                Taskitem task = (Taskitem) intent.getSerializableExtra("task");
                vehicleID.setText(task.getId());
                additionalComment.setText(task.getComment());
                endCellCol = task.getCol();
                endCellRow = task.getRow();


            }
        }


        findViewById(R.id.MapCoordinates).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText vehicleID = (EditText) findViewById(R.id.vehicleID);
                final EditText comments = (EditText) findViewById(R.id.additionalComment);

                String id = vehicleID.getText().toString().trim();
                String comment = comments.getText().toString().trim();
                double latitude, longitude;
                Taskitem sendTask;
                timestamp = new Timestamp(System.currentTimeMillis());
                String time = timestamp.toString();
                sendTask = new Taskitem(id, comment, time);



                Intent intent = new Intent(NewTaskActivity.this, MapActivity.class);
                intent.putExtra("activity_ID", "NewTaskActivity");
                intent.putExtra("task", sendTask);
                startActivity(intent);
            }
        });

        registerTask = findViewById(R.id.registerTask);
        registerTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText vehicleID = findViewById(R.id.vehicleID);
                EditText additionalComment = findViewById(R.id.additionalComment);

                boolean badInputField = false;

                id = vehicleID.getText().toString().trim();           //Check the vehicle ID
                if (TextUtils.isEmpty(id)) {
                    vehicleID.setError("This field cannot be empty");
                    badInputField = true;
                }
                comment = additionalComment.getText().toString().trim();        //Check color

                latitude = 0;
                longitude = 0;



                if (badInputField) {
                    return;
                }

                if (endCellCol == -1 || endCellRow == -1) { // Toast "you need to pick a drop off cell"
                    popWarning_noEndCell popup = new popWarning_noEndCell(id);
                    popup.show(NewTaskActivity.this.getSupportFragmentManager(), "hello");
                } else {
                    timestamp = new Timestamp(System.currentTimeMillis());
                    String time = timestamp.toString();
                    createTask(id, latitude, longitude, comment, endCellCol, endCellRow, time);
                }

            }
        });
    }

    public void createTask(String id, double latitude, double longitude, String comment, int col, int row, String time) {


        Taskitem taskitem = new Taskitem(id, time, comment, col, row);

        dbRef = FirebaseFirestore.getInstance();

        dbRef.collection("tasks").document(id).set(taskitem).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("Project3", "DocumentSnapshot successfully written!");
                EditText vehicleID = findViewById(R.id.vehicleID);
                EditText additionalComment = findViewById(R.id.additionalComment);
                vehicleID.setText("");
                additionalComment.setText("");
                endCellCol = -1;
                endCellRow = -1;
                Toast.makeText(NewTaskActivity.this, "Task added", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w("ERROR", "Error writing document", e);
            }
        });
    }


    @Override
    public void onRegisterClicked() {
        timestamp = new Timestamp(System.currentTimeMillis());
        String time = timestamp.toString();
        createTask(id, latitude, longitude, comment, endCellCol, endCellRow, time);
    }
}