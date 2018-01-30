package com.game.team9.slidingpuzzle;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class NumberMode extends AppCompatActivity {


    private Button aloneButton;
    private Button againstaiButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_number);

        aloneButton = (Button) findViewById(R.id.number);
        againstaiButton = (Button) findViewById(R.id.math);

        aloneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent x = new Intent(NumberMode.this, NumberAloneMode.class);
                startActivity(x);
            }
        });
        againstaiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent y = new Intent(NumberMode.this, NumberAIMode.class);
                startActivity(y);
            }
        });
    }
}
