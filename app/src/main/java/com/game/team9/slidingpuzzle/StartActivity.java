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
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.game.team9.slidingpuzzle.database.HighScoreDatabase;
import com.game.team9.slidingpuzzle.network.MathNSDService;
import com.game.team9.slidingpuzzle.network.PeerListAdapter;

import static com.game.team9.slidingpuzzle.network.Constants.ACTION_NAME_CHANGE;
import static com.game.team9.slidingpuzzle.network.Constants.EXTRA_ID;
import static com.game.team9.slidingpuzzle.network.Constants.EXTRA_MODE;
import static com.game.team9.slidingpuzzle.network.Constants.PREF;
import static com.game.team9.slidingpuzzle.network.Constants.PREF_LAST_GAME;
import static com.game.team9.slidingpuzzle.network.Constants.PREF_LAST_MODE;
import static com.game.team9.slidingpuzzle.network.Constants.PREF_USER;

public class StartActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener {

    private static final String TAG = "Tile_START";
    private static final String DEFAULT_NAME = "Player";
    private static final Object m_Lock = new Object();
    private static boolean m_Registered;

    private EditText m_Name;

    private RadioGroup m_ModeGroup;
    private RadioButton m_MathMode;
    private RadioButton m_NumberMode;

    private RadioGroup m_NumberGroup;
    private RadioButton m_NumSingle;
    private RadioButton m_NumAI;

    private RadioGroup m_MathGroup;
    private RadioButton m_MathSingle;
    private RadioButton m_MathDouble;

    private Button m_Button;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        setSupportActionBar(findViewById(R.id.toolbar));
        Thread.currentThread().setName("Main UI");
        synchronized (m_Lock) {
            if(!m_Registered) {
                HighScoreDatabase.Initialize(getApplicationContext());
                startService(new Intent(this, MathNSDService.class));
                m_Registered = true;
                Resources res = getResources();
                final String draw = "drawable";
                PeerListAdapter.m_ConnectIcon = res.getIdentifier("ic_play_arrow_black_24dp", draw, this.getPackageName());
                PeerListAdapter.m_WaitingIcon = res.getIdentifier("ic_hourglass_full_black_24dp", draw, this.getPackageName());
                PeerListAdapter.m_ConnectingIcon = res.getIdentifier("ic_sync_black_24dp", draw, this.getPackageName());
                PeerListAdapter.m_AcceptIcon = res.getIdentifier("ic_check_black_24dp", draw, this.getPackageName());
                PeerListAdapter.m_RejectIcon = res.getIdentifier("ic_close_black_24dp", draw, this.getPackageName());
                PeerListAdapter.m_CancelIcon = res.getIdentifier("ic_cancel_black_24dp", draw, this.getPackageName());
                PeerListAdapter.ActiveIcon = res.getIdentifier("ic_alarm_on_black_24dp", draw, this.getPackageName());

                MathNSDService.NSDIcon = res.getIdentifier("ic_phone_android_black_24dp", draw, this.getPackageName());

            }
        }
        SharedPreferences pref = getSharedPreferences(PREF, MODE_PRIVATE);
        String name = pref.getString(PREF_USER, DEFAULT_NAME);
        m_Name = findViewById(R.id.nameEdit);
        m_Name.setText(name);

        m_ModeGroup = findViewById(R.id.modeGroup);
        m_MathMode = findViewById(R.id.mathRadio);
        m_NumberMode = findViewById(R.id.numberRadio);

        m_NumberGroup = findViewById(R.id.numberGroup);
        m_NumSingle = findViewById(R.id.num_singleRadio);
        m_NumAI = findViewById(R.id.num_aiRadio);

        m_MathGroup = findViewById(R.id.mathGroup);
        m_MathSingle = findViewById(R.id.math_singleRadio);
        m_MathDouble = findViewById(R.id.math_doubleRadio);

        m_Button = findViewById(R.id.startButton);

        String game;
        try{
           game = pref.getString(PREF_LAST_GAME, "MATH");
        }
        catch(ClassCastException e)
        {
            SharedPreferences.Editor edit = pref.edit();
            edit.clear();
            edit.apply();
            game = "MATH";
        }

        String mode;
        try{
            mode = pref.getString(PREF_LAST_MODE, "SINGLE");
        }
        catch(ClassCastException e)
        {
            SharedPreferences.Editor edit = pref.edit();
            edit.clear();
            edit.apply();
            mode = "SINGLE";
        }

        if(game.equals("MATH"))
        {
            m_MathMode.setChecked(true);
            if(mode.equals("SINGLE"))
                m_MathSingle.setChecked(true);
            else
                m_MathDouble.setChecked(true);
        }
        else
        {
            m_NumberMode.setChecked(true);
            if(mode.equals("SINGLE"))
                m_NumSingle.setChecked(true);
            else
                m_NumAI.setChecked(true);
        }
        m_ModeGroup.setOnCheckedChangeListener(this);
        m_NumberGroup.setOnCheckedChangeListener(this);
        m_ModeGroup.setOnCheckedChangeListener(this);
        m_Name.setOnEditorActionListener((v,id,event)->{
            if (id == EditorInfo.IME_ACTION_SEARCH ||
                    id == EditorInfo.IME_ACTION_DONE ||
                    event != null &&
                            event.getAction() == KeyEvent.ACTION_DOWN &&
                            event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                if (event == null || !event.isShiftPressed()) {
                    // the user is done typing.
                    sendBroadcast(new Intent(ACTION_NAME_CHANGE).putExtra(EXTRA_ID, ((TextView)v).getText().toString()));
                }
            }
            return false;
        });
        resetView();
    }

    private synchronized void resetView()
    {
        m_ModeGroup.setOnCheckedChangeListener(null);
        m_NumberGroup.setOnCheckedChangeListener(null);
        m_MathGroup.setOnCheckedChangeListener(null);
        if(m_MathMode.isChecked())
        {
            m_NumberGroup.clearCheck();
            m_NumberGroup.setVisibility(View.GONE);
            m_MathGroup.setVisibility(View.VISIBLE);
            m_Button.setEnabled(m_Name.getText().length() != 0 && (m_MathSingle.isChecked() || m_MathDouble.isChecked()));
        }
        else
        {
            m_MathGroup.clearCheck();
            m_MathGroup.setVisibility(View.GONE);
            m_NumberGroup.setVisibility(View.VISIBLE);
            m_Button.setEnabled(m_Name.getText().length() != 0 && (m_NumSingle.isChecked() || m_NumAI.isChecked()));
        }
        m_ModeGroup.setOnCheckedChangeListener(this);
        m_NumberGroup.setOnCheckedChangeListener(this);
        m_MathGroup.setOnCheckedChangeListener(this);
    }


    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        resetView();
    }

    public void onStartClicked(View view)
    {

        SharedPreferences.Editor pref = getSharedPreferences(PREF, MODE_PRIVATE).edit();
        String name = m_Name.getText().toString();
        pref.putString(PREF_USER, name);
        Intent intent;
        if(m_MathMode.isChecked())
        {
            pref.putString(PREF_LAST_GAME, "MATH");
            if(m_MathSingle.isChecked())
            {
                pref.putString(PREF_LAST_MODE, "SINGLE");
                intent = new Intent(StartActivity.this, MathSinglePlayerActivity.class);
            }
            else
            {
                pref.putString(PREF_LAST_MODE, "ONLINE");
                intent = new Intent(StartActivity.this, MathOnlineDiscoveryActivity.class);
            }
        }
        else
        {
            pref.putString(PREF_LAST_GAME, "NUMBER");
            intent = new Intent(StartActivity.this, NumberModeActivity.class);
            if(m_NumSingle.isChecked())
            {
                pref.putString(PREF_LAST_MODE, "SINGLE");
                intent.putExtra(EXTRA_MODE, "SINGLE" );
            }
            else
            {
                pref.putString(PREF_LAST_MODE, "AI");
                intent.putExtra(EXTRA_MODE, "AI");
            }
        }
        pref.apply();
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        synchronized (m_Lock) {
            if(m_Registered) {
                stopService(new Intent(this, MathNSDService.class));
                HighScoreDatabase.DestroyInstance();
                Log.i("Main", "Killing database");
                m_Registered = false;
            }
        }
    }
}
