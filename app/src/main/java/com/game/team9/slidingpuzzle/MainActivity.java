package com.game.team9.slidingpuzzle;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.game.team9.slidingpuzzle.database.HighScoreDatabase;
import com.game.team9.slidingpuzzle.network.MathModeService;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private Button SignnOut;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private GoogleApiClient mGoogleApiClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Thread.currentThread().setName("Main UI");
        HighScoreDatabase.Initialize(getApplicationContext());
        Intent intent = new Intent(this, MathModeService.class);
        startService(intent);
        //bindService(intent, MathOnlineDiscoveryActivity.m_Conn, Context.BIND_AUTO_CREATE);



        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener(){
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                if (firebaseAuth.getCurrentUser() == null){
                    startActivity(new Intent(MainActivity.this, WelcomeActivity.class));
                }
            }


        };
        SignnOut = (Button) findViewById(R.id.SignOut);

        SignnOut.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {

          mAuth.signOut();
      }
  });



    }

   /* private void signOut()
    {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                mAuth.signOut();
            }
        });
    } */

    @Override
     protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }


    public void numberOnClick(View view)
    {
        Intent x = new Intent(MainActivity.this, NumberModeMenuActivity.class);
        startActivity(x);
    }

    public void mathOnClick(View view)
    {
        Intent y = new Intent(MainActivity.this, MathNameActivity.class);
        startActivity(y);
    }




    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent intent = new Intent(this, MathModeService.class);
        stopService(intent);
        HighScoreDatabase.DestroyInstance();
    }
}
