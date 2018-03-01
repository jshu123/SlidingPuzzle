package com.game.team9.slidingpuzzle;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class NumberModeMenuActivity extends AppCompatActivity {


    private Button aloneButton;
    private Button againstaiButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_number);

        aloneButton = findViewById(R.id.alone);
        againstaiButton = findViewById(R.id.againstai);

        aloneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent x = new Intent(NumberModeMenuActivity.this, NumberModeActivity.class);
                startActivity(x);
            }
        });
        againstaiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent y = new Intent(NumberModeMenuActivity.this, NumberModeActivity.class);
                y.putExtra("AI",true);
                startActivity(y);
            }
        });
    }
}
