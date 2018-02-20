/*
 * Copyright (c) 2018. $user
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.game.team9.slidingpuzzle;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.game.team9.slidingpuzzle.database.HighScoreDatabase;
import com.game.team9.slidingpuzzle.database.User;


public class HighScoreActivity extends AppCompatActivity {

    private final TextView[] m_Scores = new TextView[5];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_high_score);
        m_Scores[4] = findViewById(R.id.score1Text);
        m_Scores[3] = findViewById(R.id.score2Text);
        m_Scores[2] = findViewById(R.id.score3Text);
        m_Scores[1] = findViewById(R.id.score4Text);
        m_Scores[0] = findViewById(R.id.score5Text);

        Intent intent = getIntent();
        boolean newscore = intent.getBooleanExtra("NewScore", false);
        if(newscore)
        {
            Toast.makeText(this, "Congradulations!", Toast.LENGTH_LONG);
        }
        int i = 0;
        for (User user : HighScoreDatabase.getTop()) {
            {
                m_Scores[i].setText(String.valueOf(i+1) + ".\t" + user.getName() + "\t\t\t\t\t" + user.getScore());
                ++i;
            }
        }
        for(; i < 5;++i)
        {
            m_Scores[i].setText(String.valueOf(i+1) + ".\t" +"---\t\t\t\t\t000");
        }
    }

    public void ok_OnClicked(View view)
    {
        finish();
    }
}
