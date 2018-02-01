package com.game.team9.slidingpuzzle;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MathPlayerName extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_math_player_name);
    }


    public void onStartClicked(View view)
    {
        Intent intent = new Intent(MathPlayerName.this, MathModeStart.class);
        startActivity(intent);
    }


}
