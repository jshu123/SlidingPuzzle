package com.game.team9.slidingpuzzle;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by x on 1/30/18.
 */

public abstract class BaseGameMode extends AppCompatActivity {

    protected static final Random s_Random = new Random();

    private final Timer m_Timer = new Timer();
    private int m_Time = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    protected void startTimer(final TextView time)
    {
        m_Timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ++m_Time;
                        String text = String.valueOf((int)Math.floor(m_Time / 60))+":"+String.valueOf(m_Time % 60);
                        time.setText(text);
                    }
                });
            }
        },1000,1000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
            m_Timer.cancel();
    }
}
