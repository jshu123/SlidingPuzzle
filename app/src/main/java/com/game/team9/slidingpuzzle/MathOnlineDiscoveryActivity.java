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
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Toast;

import com.game.team9.slidingpuzzle.network.Constants;
import com.game.team9.slidingpuzzle.network.IPacketHandler;
import com.game.team9.slidingpuzzle.network.Packet;
import com.game.team9.slidingpuzzle.network.PeerInfo;
import com.game.team9.slidingpuzzle.network.PeerListAdapter;

import static com.game.team9.slidingpuzzle.network.Constants.PREF;
import static com.game.team9.slidingpuzzle.network.Constants.PREF_LAST_ONLINE_MODE;
import static com.game.team9.slidingpuzzle.network.Constants.PREF_LAST_ROUNDS;

public class MathOnlineDiscoveryActivity extends AppCompatActivity implements IPacketHandler, CompoundButton.OnCheckedChangeListener {

    private final String BASIC = "BASIC";
    private PeerListAdapter m_Adapter;

    private RadioButton m_Basic;
    private RadioButton m_Cut;

    private SharedPreferences m_Pref;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_math_online_discovery);
        m_Adapter = new PeerListAdapter(this, R.layout.fragment_dev_detail);

        AppController.addHandler(this);
        m_Basic = findViewById(R.id.basicRadio);
        m_Cut = findViewById(R.id.cutRadio);
        EditText rounds = findViewById(R.id.roundsRum);
        ListView devlist = findViewById(R.id.listView);
        devlist.setAdapter(m_Adapter);
        devlist.setEmptyView(findViewById(R.id.empty));

        SharedPreferences pref = getSharedPreferences(PREF, MODE_PRIVATE);
        if(pref.getString(PREF_LAST_ONLINE_MODE, BASIC).equals(BASIC))
            m_Basic.setChecked(true);
        else
            m_Cut.setChecked(true);

        rounds.setText(String.valueOf(pref.getInt(PREF_LAST_ROUNDS, 1)));
        rounds.setOnEditorActionListener((v, actionId, event)->{
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    actionId == EditorInfo.IME_ACTION_DONE ||
                    event != null &&
                            event.getAction() == KeyEvent.ACTION_DOWN &&
                            event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                if (event == null || !event.isShiftPressed()) {
                    m_Pref.edit().putInt(PREF_LAST_ROUNDS, Integer.parseInt(v.getText().toString())).apply();
                }
            }
            return false; // pass on to other listeners.
        });

        m_Basic.setOnCheckedChangeListener(this);
        m_Cut.setOnCheckedChangeListener(this);
        m_Pref = getSharedPreferences(PREF, MODE_PRIVATE);
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
    {
        if(isChecked)
            m_Pref.edit().putString(PREF_LAST_ONLINE_MODE, buttonView.equals(m_Basic) ? BASIC : "CUT").apply();
    }


    @Override
    public int Priority() {
        return Constants.PRIORITY_DISCOVER_ACT;
    }

    @Override
    public int compareTo(@NonNull IPacketHandler o) {
        return Priority() - o.Priority();
    }

    @Override
    public boolean handleData(Packet p) {
        switch(p.Type) {
            case ACCEPT:
            {
                Intent intent = new Intent(MathOnlineDiscoveryActivity.this, p.Data[0] == 0 ? MathDoubleBasicActivity.class : MathDoubleCuthroatActivity.class);
                PeerInfo i = PeerInfo.Retrieve(p.Source);
                intent.putExtra(Constants.EXTRA_ID, i.Name);
                intent.putExtra(Constants.EXTRA_DEVICE, i.Address);
                intent.putExtra(Constants.EXTRA_IS_HOST, true);
                intent.putExtra(Constants.EXTRA_ROUNDS, p.Data[1]);
                startActivity(intent);

            }
            p.Free();
            return true;
            case QUIT:
            {
                PeerInfo i = PeerInfo.Retrieve(p.Source);
                if(i.Info == PeerInfo.Status.OUTBOUND_REQUEST)
                {
                    runOnUiThread(()-> Toast.makeText(this, i.Name + " has rejected your invite.", Toast.LENGTH_SHORT).show());
                    i.Update(PeerInfo.Status.AVAILABLE);
                    p.Free();
                    return true;
                }
            }
            break;
            case REQUEST:
            case FREE:
            case MOVE:
            case TIME:
            case INIT:
            default:
                break;
        }
        return false;
    }

    public void LaunchGame(Intent intent)
    {
        startActivity(intent);
        finish();
    }

    @Override
    protected synchronized void onDestroy() {
        super.onDestroy();
        AppController.removeHandler(this);
        m_Adapter.Teardown();
    }
}
