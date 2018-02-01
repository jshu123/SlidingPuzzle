package com.game.team9.slidingpuzzle;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class NumberMode extends BaseGameMode implements NumberModeView.IBoardSolvedListener {

    private NumberModeView m_Player;
    private NumberModeView m_AI;

    private NumberModeAI m_AI_Bot;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);




        int tiles[] = new int[25];

        //Create new puzzle with random numbers
        do {
            for (int i = 0, swap = 0; i < 25; ++i) {
                int rand = s_Random.nextInt(swap + 1);
                if (rand != swap)
                    tiles[swap] = tiles[rand];
                tiles[rand] = i;
                ++swap;
            }
        }while(!isSolvable(tiles));

        Intent intent = getIntent();
        if(intent.getBooleanExtra("AI", false))
        {
            setContentView(R.layout.activity_number_aimode);
            m_AI = findViewById(R.id.aiView);
            m_AI_Bot = new NumberModeAI(m_AI);
            m_AI.Initialize(tiles, true);
            m_AI.AttachSolveListener(this);
        }
        else
            setContentView(R.layout.activity_number_alone_mode);

        m_Player = findViewById(R.id.playerView);
        m_Player.Initialize(tiles);
        m_Player.AttachSolveListener(this);
        startTimer((TextView)findViewById(R.id.timerText));
     /*   m_TimeView = findViewById(R.id.timerView);

        //start timer
        m_Timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ++m_Time;
                        String text = String.valueOf((int)Math.floor(m_Time / 60))+":"+String.valueOf(m_Time % 60);
                        m_TimeView.setText(text);
                    }
                });
            }
        },1000,1000);*/
    }


    @Override
    public void Solved(int id) {

       // if(m_AI != null)
         //   m_AI.Stop();
        String msg = id == m_Player.getId() ? "You win!" : "You lose.";
        AlertDialog alertDialog = new AlertDialog.Builder(NumberMode.this).create();
        alertDialog.setTitle("Game over");
        alertDialog.setMessage(msg);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        complete();
                    }
                });
        alertDialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(m_AI_Bot != null)
        if(m_AI_Bot != null)
            m_AI_Bot.Solved(0);
    }

    private void complete()
    {
        finish();
    }
    private static boolean isSolvable(int[] tiles)
    {
        int parity = 0;
        for(int i = 0; i < 24; ++i)
        {
            if(tiles[i] != 0)
            {
                for(int j = i + 1; j < 25; ++j)
                {
                    parity += (tiles[i] > tiles[j]) ? 1 : 0;
                }
            }
            else
                tiles[i] = BaseGameView.BLANK_VALUE;
        }
        return (parity % 2) == 0;
    }


}
