package com.game.team9.slidingpuzzle;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;

import com.game.team9.slidingpuzzle.database.HighScoreDatabase;
import com.game.team9.slidingpuzzle.database.User;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MathSinglePlayerActivity extends BaseMathActivity {

    private TextView m_ScoreView;
    private int m_Score;
    private Button m_Pause;
    private MathModeView m_Game;
    private Chronometer m_Timer;
    private long lastPause;

    private final Set<Equation> m_Hist = new HashSet<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_player);
        m_ScoreView = findViewById(R.id.hostScoreText);
        m_Game = findViewById(R.id.playerView);
        m_Pause = findViewById(R.id.pauseButton);
        m_Score = 0;
        m_ScoreView.setText("0");
        m_Pause.setText("Pause");

        m_Game.Initialize(getBoard(), this);
        m_Timer = findViewById(R.id.chronometer);
        m_Timer.start();

    }


    public void onGiveupClicked(View view)
    {
        onGameEnded();
    }
    public void onPauseClicked(View view)
    {

        String pstring = m_Pause.getText().toString();
        if(pstring.equals("Pause")){
            m_Pause.setText("Resume");
            lastPause = SystemClock.elapsedRealtime();
            m_Timer.stop();
            m_Game.Pause();

        }
        else{
            m_Pause.setText("Pause");
            m_Timer.setBase(m_Timer.getBase()+ SystemClock.elapsedRealtime()-lastPause);
            m_Timer.start();
            m_Game.UnPause();

        }

    }
    @Override
    protected void onGameEnded(String msg) {
        m_User.setScore(m_Score);
        List<User> top = HighScoreDatabase.getTop();
        HighScoreDatabase.updateUser(m_User);
        Intent intent = new Intent(MathSinglePlayerActivity.this, HighScoreActivity.class);
        for (User user : top) {
            if(m_Score > user.getScore())
            {
                msg += "You have set a new highscore!  ";
                intent.putExtra("NewScore", true);
                break;
            }
        }
        AlertDialog alertDialog = new AlertDialog.Builder(MathSinglePlayerActivity.this).create();
        alertDialog.setTitle("New highscore");
        alertDialog.setMessage(msg);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                (dialog, which) -> {
                    dialog.dismiss();
                    startActivity(intent);
                    finish();
                });
        alertDialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(m_Timer !=null)
            m_Timer.stop();
    }

    public void onHighscoreClicked(View view)
    {
      //  goodToast("YAYY");
        Intent intent = new Intent(MathSinglePlayerActivity.this, HighScoreActivity.class);
        startActivity(intent);
    }

    @Override
    public void onSwipeEvent(int[] indexes) {
        if(validSwipe(indexes))
        {
            byte[] tiles = m_Game.getTiles();
            for(int i = 0; i < 5; ++i)
            {
                indexes[i] = tiles[indexes[i]];
            }
            Equation eq = new Equation(indexes);
            if(eq.valid)
            {
                if(m_Hist.add(eq)) {
                    m_Score += eq.score;
                    m_ScoreView.setText(Integer.toString(m_Score));
                    goodToast(eq.score + (eq.score == 1 ? "point!" : " points!"));
                }
                else
                {
                    badToast(R.string.no_points);
                }
            }
            else
            {
                badToast(R.string.invalid_eq);
            }

        }
    }
}
