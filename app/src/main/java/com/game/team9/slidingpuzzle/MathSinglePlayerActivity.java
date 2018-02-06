package com.game.team9.slidingpuzzle;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import com.game.team9.slidingpuzzle.database.HighScoreDatabase;
import com.game.team9.slidingpuzzle.database.User;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Stack;

public class MathSinglePlayerActivity extends AppCompatActivity implements MathModeView.IScored{

    private TextView m_ScoreView;
    private int m_Score;
    private final User m_User = new User();

    private Toast m_Toast;
    private TextView m_ToastText;

    private MathModeView m_Game;
    private Chronometer m_Timer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_player);
        m_ScoreView = findViewById(R.id.scoreText);
        m_Game = findViewById(R.id.playerView);
        m_Score = 0;
        m_ScoreView.setText("0");
        Intent intent = getIntent();
        m_User.setName(intent.getStringExtra("Player"));
        int tiles[] = new int[25];
        Stack<Integer> stack = new Stack<>();
        Random r = new Random();
        for(int i = 0; i < 5; ++i)
        {
            stack.push(13);
            stack.push(11 + r.nextInt(2));
        }
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast_layout,
                findViewById(R.id.toast_container));
        m_Toast= new Toast(getApplicationContext());
        m_Toast.setView(layout);
        m_ToastText = layout.findViewById(R.id.textView);

        Collections.shuffle(stack);
        Stack<Integer> num = new Stack<>();
        for(int i = 0; i < 15; ++i)
            num.push(r.nextInt(10));
        num.set(r.nextInt(15), -1);
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
                tiles[i] = r.nextBoolean() ? num.pop() : stack.pop();
        }

        m_Game.AttachScoreListener(this);
        m_Game.Initialize(tiles);
        m_Timer = findViewById(R.id.chronometer);
        m_Timer.start();

    }


    public void onGiveupClicked(View view)
    {
        m_User.setScore(m_Score);
        List<User> top = HighScoreDatabase.getTop();
        HighScoreDatabase.updateUser(m_User);
        for (User user : top) {
            if(m_Score > user.getScore())
            {
                HighScoreFragment fragment = new HighScoreFragment();
                Bundle b = new Bundle();
                b.putString("Player", m_User.getName());
                b.putInt("Score", m_Score);
                fragment.setArguments(b);
                //getSupportFragmentManager().beginTransaction().add(R.id.)
                AlertDialog alertDialog = new AlertDialog.Builder(MathSinglePlayerActivity.this).create();
                alertDialog.setTitle("New highscore");
                alertDialog.setMessage("You have set a new highscore!");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(@NonNull DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
                break;
            }
        }
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(m_Game != null)
            m_Game.Destroy();
        if(m_Timer !=null)
            m_Timer.stop();
    }

    public void onHighscoreClicked(View view)
    {

    }

    @Override
    public void Score(int score, @NonNull MathModeView.ScoreType type) {
        String msg = "";
        switch(type) {
            case Valid:
            m_Score += score;
            m_ScoreView.setText(Integer.toString(m_Score));
                m_ToastText.setTextColor(Color.GREEN);
                m_Toast.setDuration(Toast.LENGTH_LONG);
            msg = Integer.toString(score) + " points!";
            break;
            case Invalid:
            case Taken:
                m_Toast.setDuration(Toast.LENGTH_SHORT);
                m_ToastText.setTextColor(Color.RED);
                msg = "No points.";
        }
        m_ToastText.setText(msg);
        m_Toast.show();

        /*
        AlertDialog alertDialog = new AlertDialog.Builder(MathSinglePlayerActivity.this).create();
        alertDialog.setTitle(type.toString());
        alertDialog.setMessage(msg);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();*/
    }
}
