package com.game.team9.slidingpuzzle;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private Button numberButton;
    private Button mathButton;

    private TextView PlayerName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        numberButton = (Button) findViewById(R.id.number);
        mathButton = (Button) findViewById(R.id.math);

        numberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               Intent x = new Intent(MainActivity.this, NumberMode.class);
                startActivity(x);
            }
            });
        mathButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent y = new Intent(MainActivity.this, MathPlayerName.class);
                startActivity(y);
            }
        });
    }
}
