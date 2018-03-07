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
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.game.team9.slidingpuzzle.database.HighScoreDatabase;
import com.game.team9.slidingpuzzle.database.User;

import java.util.Collections;
import java.util.List;


public class HighScoreActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_high_score);
        ListView hsView = findViewById(R.id.listView);
        List<User> list = HighScoreDatabase.getTop();
        Collections.reverse(list);
        hsListAdapter adapter = new hsListAdapter(this, R.layout.high_score_adapter, list);
        hsView.setAdapter(adapter);
        hsView.setEmptyView(findViewById(R.id.emptyView));

        Intent intent = getIntent();
        if(intent.getBooleanExtra("NewScore", false))
        {
            Toast.makeText(this, "Congradulations!", Toast.LENGTH_LONG).show();
        }

    }

    public void ok_OnClicked(View view)
    {
        finish();
    }
}