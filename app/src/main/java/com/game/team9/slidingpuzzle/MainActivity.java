package com.game.team9.slidingpuzzle;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import com.game.team9.slidingpuzzle.database.HighScoreDatabase;
import com.game.team9.slidingpuzzle.network.MathModeService;
import com.game.team9.slidingpuzzle.network.WifiService;

public class MainActivity extends AppCompatActivity {


    private static final Object m_Lock = new Object();
    private static boolean m_Registered;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar(findViewById(R.id.toolbar));
        Thread.currentThread().setName("Main UI");
        synchronized (m_Lock) {
            if(!m_Registered) {
                HighScoreDatabase.Initialize(getApplicationContext());
                startService(new Intent(this, MathModeService.class));
              //  startService(new Intent(this, WifiService.class));
                m_Registered = true;
            }
        }
    }

    public void numberOnClick(View view) throws Exception {
        startActivity(new Intent(MainActivity.this, NumberModeMenuActivity.class));
    }

    public void mathOnClick(View view)
    {
        startActivity(new Intent(MainActivity.this, MathNameActivity.class));
    }




    @Override
    protected void onDestroy() {
        super.onDestroy();
        synchronized (m_Lock) {
            if(m_Registered) {
                stopService(new Intent(this, MathModeService.class));
              //  stopService(new Intent(this, WifiService.class));
                HighScoreDatabase.DestroyInstance();
                Log.i("Main", "Killing database");
                m_Registered = false;
            }
        }
    }
}
