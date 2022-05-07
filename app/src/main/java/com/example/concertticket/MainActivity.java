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

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInApi;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.class.getName();
    private static final String PREF_KEY = MainActivity.class.getPackage().toString();
    private static final int GL_SIGN_IN = 555;
    private static final int SECRET_KEY = 123;

    private SharedPreferences sharedPreferences;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleClient;

    EditText userName;
    EditText password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userName = findViewById(R.id.editTextUsername);
        password = findViewById(R.id.editTextPassword);

        sharedPreferences = getSharedPreferences(PREF_KEY, MODE_PRIVATE);

        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleClient = GoogleSignIn.getClient(this, googleSignInOptions);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GL_SIGN_IN){
           Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

           try {
               GoogleSignInAccount acc = task.getResult(ApiException.class);
               Log.d(LOG_TAG, "ID: " + acc.getId());
               firebaseAuthWithGoogle(acc.getIdToken());
           } catch (ApiException e) {
               Log.w(LOG_TAG, "Google sign in failed", e);

           }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential authCredential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(authCredential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Log.d(LOG_TAG, "Google Credential success");
                    startListing();
                } else {
                    Log.d(LOG_TAG, "UGoogle Credential failed");
                }
            }
        });
    }

    public void login(View view){
        String userNameStr = userName.getText().toString();
        String passwordStr = password.getText().toString();

        Log.i(LOG_TAG, "Logged in: " + userNameStr + ", password: "+ passwordStr);

        mAuth.signInWithEmailAndPassword(userNameStr, passwordStr).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Log.d(LOG_TAG, "User logged in successfully");
                    startListing();
                } else {
                    Log.d(LOG_TAG, "User login failed");
                    Toast.makeText(MainActivity.this,
                            "User login failed" +  task.getException().getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void startListing(/* registered user data soon */) {
        Intent intent = new Intent(this, ConcertTicketListActivity.class);
        startActivity(intent);
    }

    public void loginWithGoogle(View view){
        Intent intent = mGoogleClient.getSignInIntent();
        startActivityForResult(intent, GL_SIGN_IN);
    }

    public void register(View view){
        Intent intent = new Intent(this, RegisterActivity.class);
        intent.putExtra("SECRET_KEY", SECRET_KEY);
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("userName", userName.getText().toString());
        editor.putString("password", password.getText().toString());
        editor.apply();
    }
}