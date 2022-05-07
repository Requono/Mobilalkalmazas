package com.example.concertticket;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    private static final String LOG_TAG = RegisterActivity.class.getName();
    private static final String PREF_KEY = MainActivity.class.getPackage().toString();
    private static final int SECRET_KEY = 123;

    EditText userNameEditText;
    EditText userEmailEditText;
    EditText userPasswordEditText;
    EditText userPasswordReEditText;

    private SharedPreferences preferences;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        int secret_key = getIntent().getIntExtra("SECRET_KEY", 0);

        if(secret_key != 123 ) finish();

        userNameEditText = findViewById(R.id.usernameEditText);
        userEmailEditText = findViewById(R.id.userEmailEditText);
        userPasswordEditText = findViewById(R.id.userPasswordEditText);
        userPasswordReEditText = findViewById(R.id.userPasswordReEditText);

        preferences = getSharedPreferences(PREF_KEY, MODE_PRIVATE);
        String userName = preferences.getString("userName", "");
        String password = preferences.getString("password", "");

        userNameEditText.setText(userName);
        userPasswordEditText.setText(password);
        userPasswordReEditText.setText(password);

        mAuth = FirebaseAuth.getInstance();
    }


    public void register(View view) {

        String userNameStr = userNameEditText.getText().toString();
        String userEmailStr = userEmailEditText.getText().toString();
        String passwordStr = userPasswordEditText.getText().toString();
        String passwordReStr = userPasswordReEditText.getText().toString();

        if(!passwordStr.equals(passwordReStr)) {
            Log.e(LOG_TAG, "Not the same password!");
            return;
        }

        Log.i(LOG_TAG, "Registered: " + userNameStr + ", Email: " + userEmailStr);

        //startListing();

        mAuth.createUserWithEmailAndPassword(userEmailStr, passwordStr).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Log.d(LOG_TAG, "User created successfully");
                    startListing();
                } else {
                    Log.d(LOG_TAG, "User creation failed");
                    Toast.makeText(RegisterActivity.this,
                            "User creation failed" +  task.getException().getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    public void cancel(View view) {
        finish();
    }

    public void startListing(/* registered user data soon */) {
        Intent intent = new Intent(this, ConcertTicketListActivity.class);

        startActivity(intent);
    }

}