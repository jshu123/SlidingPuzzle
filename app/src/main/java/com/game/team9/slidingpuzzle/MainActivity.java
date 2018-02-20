package com.game.team9.slidingpuzzle;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.game.team9.slidingpuzzle.database.HighScoreDatabase;
import com.game.team9.slidingpuzzle.network.MathModeService;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Thread.currentThread().setName("Main UI");
        HighScoreDatabase.Initialize(getApplicationContext());
        Intent intent = new Intent(this, MathModeService.class);
        startService(intent);
        //bindService(intent, MathOnlineDiscoveryActivity.m_Conn, Context.BIND_AUTO_CREATE);
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
