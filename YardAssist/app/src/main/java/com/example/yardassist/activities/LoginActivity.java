package com.example.yardassist.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.example.yardassist.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


public class LoginActivity extends AppCompatActivity {

    EditText mEmail, mPassword;
    Button mLogin;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userID;

    String userEmail;
    String userName;
    String userSurname;
    int userType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);
        mLogin = findViewById(R.id.login);

        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProgressBar pgsBar = (ProgressBar) findViewById(R.id.pBar);
                pgsBar.setVisibility(View.VISIBLE);

                String email = mEmail.getText().toString().trim();
                String password = mPassword.getText().toString().trim();

                if (email.isEmpty()){
                    mEmail.setError("Please enter a valid email address");
                }
                else if(password.isEmpty()){
                    mPassword.setError("Please enter a password");
                }
                else {
                    login(email, password);
                }
            }
        });

        //Register vehicle test stuff
        final Button registerButton = findViewById(R.id.testRegVehicle);
        registerButton.setVisibility(View.INVISIBLE);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, OperatorActivity.class);
                startActivity(intent);
            }
        });

        final Button map = findViewById(R.id.map_button);
        map.setVisibility(View.INVISIBLE);
        map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, MapActivity.class);
                intent.putExtra("activity_ID", "LoginActivity");
                startActivity(intent);
            }
        });

        final Button manager = findViewById(R.id.managerButton);
        manager.setVisibility(View.INVISIBLE);
        manager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, NewManager.class);
                startActivity(intent);
            }
        });
        final Button operator = findViewById(R.id.Operator);
        operator.setVisibility(View.INVISIBLE);
        operator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, NewOperator.class);
                startActivity(intent);
            }
        });

    }

    private void login(String email, String password){
        fAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    fAuth = FirebaseAuth.getInstance();
                    userID = fAuth.getCurrentUser().getUid();
                    DocumentReference documentReference = fStore.collection("users").document(userID);


                    documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(task.isSuccessful()){
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    //User user = new User(document.getId(),(String) document.get("email"), (String) document.get("name"), (String) document.get("surname"), (String) document.get("usertype"));
                                    userType = document.getLong("usertype").intValue();

                                    //Login as operator
                                    if (userType == 0){
                                        Intent intent = new Intent(LoginActivity.this, NewOperator.class);
                                        intent.putExtra("activity_ID", "LoginActivity");
                                        startActivity(intent);
                                        finish();
                                    }
                                    //Login as manager
                                    else if(userType == 1){
                                        startActivity(new Intent(getApplicationContext(), NewManager.class));
                                        finish();
                                    }
                                    //Login as admin
                                    else{
                                        startActivity(new Intent(getApplicationContext(), CreateAccount.class));
                                        finish();
                                    }
                                    Log.d("Login -", "DocumentSnapshot data: " + document.getData());

                                } else {
                                    Log.d("Login -", "No such document");
                                }
                            }
                        }
                    });
                } else {
                    mPassword.setError("Wrong username or password");
                    mPassword.setError("Wrong username or password");
                }
            }
        });
    }
}