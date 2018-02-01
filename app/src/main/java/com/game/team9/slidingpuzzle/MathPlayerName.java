package com.game.team9.slidingpuzzle;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class MathPlayerName extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_math_player_name);
    }


    public void onStartClicked(View view)
    {
        Intent intent = new Intent(this, MathModeStart.class);

        TextView a = findViewById(R.id.oldplayerText);
        intent.putExtra("Old Player", a.getText().toString());
        a = findViewById(R.id.newnameText);
        intent.putExtra("New Player", a.getText().toString());
        a = findViewById(R.id.newuserText);
        User u = new User();
        u.setName(a.getText().toString());
        u.setScore(0);
        HighScoreDatabase.addUser(HighScoreDatabase.getDatabase(this), u);
        intent.putExtra("New User", a.getText().toString());
        startActivity(intent);
    }
}
