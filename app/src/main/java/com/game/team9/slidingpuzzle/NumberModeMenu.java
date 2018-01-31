package com.game.team9.slidingpuzzle;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class NumberModeMenu extends AppCompatActivity {


    private Button aloneButton;
    private Button againstaiButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_number);

        aloneButton = (Button) findViewById(R.id.alone);
        againstaiButton = (Button) findViewById(R.id.againstai);

        aloneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent x = new Intent(NumberModeMenu.this, NumberMode.class);
                startActivity(x);
            }
        });
        againstaiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent y = new Intent(NumberModeMenu.this, NumberMode.class);
                y.putExtra("AI",true);
                startActivity(y);
            }
        });
    }
}
