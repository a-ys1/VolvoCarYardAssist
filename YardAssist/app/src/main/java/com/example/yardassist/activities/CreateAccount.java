package com.example.yardassist.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.example.yardassist.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;


import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class CreateAccount extends AppCompatActivity {

    Button mCreateAccount;
    EditText mEmail, mPassword, mName, mSurname;
    String userID;
    RadioButton mOperator, mManager, mAdmin;
    RadioGroup mUserType;

    FirebaseAuth fAuth;
    FirebaseFirestore fStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        mCreateAccount = findViewById(R.id.button_createAccount);
        mEmail = findViewById(R.id.email_createAccount);
        mPassword = findViewById(R.id.password_createAccount);
        mName = findViewById(R.id.name_createAccount);
        mSurname = findViewById(R.id.surname_createAccount);
        mUserType = findViewById(R.id.radioGroup);
        mOperator = findViewById(R.id.radioButton_operator);
        mManager = findViewById(R.id.radioButton_manager);
        mAdmin = findViewById(R.id.radioButton_admin);



        mCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEmail.getText().toString().trim();
                String password = mPassword.getText().toString().trim();
                String name = mName.getText().toString().trim();
                String surname = mSurname.getText().toString().trim();
                int selectedId = mUserType.getCheckedRadioButtonId();
                int userType;

                if(selectedId == mOperator.getId()) {
                    userType = 0;
                } else if(selectedId == mManager.getId()) {
                    userType = 1;
                } else {
                    userType = 2;
                }

                if (email.isEmpty()){
                    mEmail.setError("Please enter a valid email address");
                }
                else if(password.isEmpty()){
                    mPassword.setError("Please enter a password");
                }
                else if(name.isEmpty()){
                    mName.setError("Please enter a name");
                }
                else if(surname.isEmpty()){
                    mName.setError("Please enter a name");
                }
                else {
                    createUser(email, password, name, surname, userType);
                }
            }
        });
    }


    public void createUser(String email, String password, String name, String surname, int userType){

        fAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {
                    userID = fAuth.getCurrentUser().getUid();
                    DocumentReference documentReference = fStore.collection("users").document(userID);

                    // Create a Map to store the data we want to set
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("email", email);
                    userData.put("name", name);
                    userData.put("surname", surname);
                    userData.put("usertype", userType);

                    documentReference.set(userData).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("Debug", "Success - account created");
                        }
                    });

                } else {
                    mCreateAccount.setError("Error, try again");
                }
            }
        });
    }
}