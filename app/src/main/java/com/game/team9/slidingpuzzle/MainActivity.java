package com.game.team9.slidingpuzzle;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.game.team9.slidingpuzzle.database.HighScoreDatabase;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final Map<BroadcastReceiver, IntentFilter> s_Receivers = new HashMap<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Thread.currentThread().setName("Main UI");
        HighScoreDatabase.Initialize(getApplicationContext());

        /*Intent intent = new Intent(this, WifiP2PService.class);
        bindService(intent, MathOnlineDiscoveryActivity.m_Conn, Context.BIND_AUTO_CREATE);*/
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





    public static void AddReceiver(BroadcastReceiver rec, IntentFilter fil)
    {
        s_Receivers.put(rec, fil);
    }

    public void RemoveReceiver(BroadcastReceiver rec)
    {
        s_Receivers.remove(rec);
        unregisterReceiver(rec);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onResume() {
        super.onResume();
        s_Receivers.forEach((a,b)-> registerReceiver(a,b));
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onPause() {
        super.onPause();
        s_Receivers.keySet().forEach(r->unregisterReceiver(r));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        HighScoreDatabase.DestroyInstance();
        for (BroadcastReceiver b : s_Receivers.keySet()) {
            unregisterReceiver(b);
        }
    }
}
