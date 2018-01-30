package com.game.team9.slidingpuzzle;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MathModeStart extends AppCompatActivity {


    private Button singleButton;
    private Button doubleButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_math);


        singleButton = (Button) findViewById(R.id.single);
        doubleButton = (Button) findViewById(R.id.twoplayer);

        singleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent x = new Intent(MathModeStart.this, MathSingePlayer.class);
                startActivity(x);
            }
        });
        doubleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent y = new Intent(MathModeStart.this, MathDoublePlayer.class);
                startActivity(y);
            }
        });


    }
}
