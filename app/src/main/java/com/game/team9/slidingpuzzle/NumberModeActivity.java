package com.game.team9.slidingpuzzle;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.Chronometer;

import java.util.Random;
import java.util.concurrent.Executors;

import static com.game.team9.slidingpuzzle.network.Constants.EXTRA_MODE;
import static com.game.team9.slidingpuzzle.network.Constants.PREF_LAST_MODE;

public class NumberModeActivity extends AppCompatActivity implements NumberModeView.IBoardSolvedListener {

    private NumberModeView m_Player;
    private NumberModeView m_AI;
    private Button m_Pause;
    private NumberModeAI m_AI_Bot;
    private Chronometer m_Timer;
    private long lastPause;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Random r = new Random();

        byte tiles[] = new byte[25];

        //Create new puzzle with random numbers
        do {
            for (byte i = 0, swap = 0; i < 25; ++i) {
                int rand = r.nextInt(swap + 1);
                if (rand != swap)
                    tiles[swap] = tiles[rand];
                tiles[rand] = i;
                ++swap;
            }
        }while(!isSolvable(tiles));

        Intent intent = getIntent();
        if(intent.getStringExtra(EXTRA_MODE).equals("AI"))
        {
            setContentView(R.layout.activity_number_aimode);
            m_AI = findViewById(R.id.aiView);
            m_AI_Bot = new NumberModeAI(m_AI);
            m_AI.AttachStartListener(() -> Executors.defaultThreadFactory().newThread(m_AI_Bot).start());
            m_AI.Initialize(tiles, indexes -> {

            }, true);
            m_AI.AttachSolveListener(this);
        }
        else
            setContentView(R.layout.activity_number_alone_mode);

        m_Player = findViewById(R.id.playerView);
        m_Player.Initialize(tiles, indexes -> {

        });
        m_Player.AttachSolveListener(this);
        m_Timer =findViewById(R.id.chronometer);
        m_Pause = findViewById(R.id.pauseButton);
        m_Pause.setText("Pause");
        m_Timer.start();
        m_Pause.setOnClickListener(v -> {
            String pstring = m_Pause.getText().toString();
            if(pstring.equals("Pause")){
                m_Pause.setText("Resume");
                lastPause = SystemClock.elapsedRealtime();
                m_Timer.stop();
                m_Player.Pause();
                if(m_AI_Bot != null)
                    m_AI_Bot.Pause();
               // if(m_AI != null)
                 //   m_AI.Pause();

            }
            else {
                m_Pause.setText("Pause");
                m_Timer.setBase(m_Timer.getBase() + SystemClock.elapsedRealtime() - lastPause);
                m_Timer.start();
                m_Player.UnPause();
                //if(m_AI != null)
                //    m_AI.UnPause();
                if(m_AI_Bot != null)
                    m_AI_Bot.UnPause();
            }
        });
    }


        //badToast(R.string.invalid_eq);

    @Override
    public void Solved(int id) {

       // if(m_AI != null)
         //   m_AI.Stop();
        String msg = id == m_Player.getId() ? "You win!" : "You lose.";
        AlertDialog alertDialog = new AlertDialog.Builder(NumberModeActivity.this).create();
        alertDialog.setTitle("Game over");
        alertDialog.setMessage(msg);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                (dialog, which) -> {
                    dialog.dismiss();
                    complete();
                });
        alertDialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(m_AI_Bot != null)
            m_AI_Bot.Solved(0);
        if(m_Timer != null)
            m_Timer.stop();
    }

    private void complete()
    {
        finish();
    }
    private static boolean isSolvable(byte[] tiles)
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
