package com.game.team9.slidingpuzzle;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.util.Collection;
import java.util.Collections;
import java.util.Random;
import java.util.Stack;
import java.util.Timer;

public class MathSingePlayer extends BaseGameMode implements MathModeView.IScored{

    private static Random s_Random = new Random();

    private TextView m_ScoreView;
    private int m_Score;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_singe_player);
        m_ScoreView = findViewById(R.id.scoreText);
        m_Score = 0;
        m_ScoreView.setText("0");
        int tiles[] = new int[25];
        Stack<Integer> stack = new Stack<>();
        for(int i = 0; i < 5; ++i)
        {
            stack.push(13);
            stack.push(11 + s_Random.nextInt(2));
        }


        Collections.shuffle(stack);
        Stack<Integer> num = new Stack<>();
        for(int i = 0; i < 15; ++i)
            num.push(s_Random.nextInt(10));
        num.set(s_Random.nextInt(15), -1);
        for(int i = 0; i < 25; ++i)
        {
           // if((2 + (i / 5)) % 2 == 0 && (i % 5 == 0 || (i % 5 == 2) || (i % 5 == 4 )))
                //tiles[i] = num.pop();
            if(!stack.empty() && (i == 7 || i == 11 || i == 13 || i == 17))
                tiles[i] = stack.pop();
            else if(stack.empty())
                tiles[i] = num.pop();
            else if(num.empty())
                tiles[i] = stack.pop();
            else
                tiles[i] = s_Random.nextBoolean() ? num.pop() : stack.pop();
        }

        MathModeView view = findViewById(R.id.playerView);
        view.AttachScoreListener(this);
        view.Initialize(tiles);
        startTimer((TextView)findViewById(R.id.timerText));

    }

    @Override
    public void Score(int score, MathModeView.ScoreType type) {
        String msg = "";
        switch(type) {
            case Valid:
            m_Score += score;
            m_ScoreView.setText(Integer.toString(m_Score));
            msg = Integer.toString(score) + " points!";
            break;
            case Invalid:
            case Taken:
                msg = "No points.";
        }

        AlertDialog alertDialog = new AlertDialog.Builder(MathSingePlayer.this).create();
        alertDialog.setTitle(type.toString());
        alertDialog.setMessage(msg);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }
}
