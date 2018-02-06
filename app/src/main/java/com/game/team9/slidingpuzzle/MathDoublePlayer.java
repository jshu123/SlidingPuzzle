package com.game.team9.slidingpuzzle;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MathDoublePlayer extends AppCompatActivity {

    private Button basicButton;
    private Button cutthroatButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_double_player);

        basicButton = findViewById(R.id.button_basic);
        cutthroatButton = findViewById(R.id.button_cutthroat);

        basicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent x = new Intent(MathDoublePlayer.this, MathDoubleBasicActivity.class);
                startActivity(x);
            }
        });
        cutthroatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent y = new Intent(MathDoublePlayer.this,MathDoubleCuthroatActivity.class);
                startActivity(y);
            }
        });
    }

}
