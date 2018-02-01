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
        Button button_mathstart = (Button)findViewById(R.id.mathstart);
        button_mathstart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent_math_player_name = new Intent(MathPlayerName.this, MathModeStart.class);
                startActivity(intent_math_player_name);
            }
        });
    }


}
