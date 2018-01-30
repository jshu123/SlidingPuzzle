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

        basicButton = (Button) findViewById(R.id.basic);
        cutthroatButton = (Button) findViewById(R.id.cutthroat);

        basicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent x = new Intent(MathDoublePlayer.this, MathDoubleBasic.class);
                startActivity(x);
            }
        });
        cutthroatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent y = new Intent(MathDoublePlayer.this,MathDoubleCuthoroat.class);
                startActivity(y);
            }
        });
    }

}
