package com.game.team9.slidingpuzzle;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.game.team9.slidingpuzzle.database.HighScoreDatabase;
import com.game.team9.slidingpuzzle.network.MathModeService;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            AlertDialog.Builder alert = new AlertDialog.Builder(AppController.getInstance());
            alert.setMessage(e.getMessage()).setNeutralButton("OK",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            System.exit(0);
                            android.os.Process.killProcess(android.os.Process.myPid());
                        }
                    });
            alert.create().show();
        });

        Thread.currentThread().setName("Main UI");
        HighScoreDatabase.Initialize(getApplicationContext());
        Intent intent = new Intent(this, MathModeService.class);
        startService(intent);
    }

    public void numberOnClick(View view) throws Exception {
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
        Log.i("Main", "Killing database");
        HighScoreDatabase.DestroyInstance();
    }
}
