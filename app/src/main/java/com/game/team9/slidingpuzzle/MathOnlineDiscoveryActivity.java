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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.game.team9.slidingpuzzle.network.Constants;
import com.game.team9.slidingpuzzle.network.IPacketHandler;
import com.game.team9.slidingpuzzle.network.Packet;
import com.game.team9.slidingpuzzle.network.PeerInfo;
import com.game.team9.slidingpuzzle.network.PeerListAdapter;

public class MathOnlineDiscoveryActivity extends AppCompatActivity implements IPacketHandler {

    private PeerListAdapter m_Adapter;
    private ListView m_DevList;

    private TextView m_Status;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_math_online_discovery);
        m_Adapter = new PeerListAdapter(this, R.layout.fragment_dev_detail);

        AppController.addHandler(this);
        m_Status = findViewById(R.id.statusText);

        m_DevList = findViewById(R.id.listView);
        m_DevList.setAdapter(m_Adapter);
        m_DevList.setEmptyView(findViewById(R.id.empty));
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
                Intent intent = new Intent(MathOnlineDiscoveryActivity.this, p.Data[0] == 1 ? MathDoubleBasicActivity.class : MathDoubleCuthroatActivity.class);
                PeerInfo i = PeerInfo.Retrieve(p.Source);
                intent.putExtra(Constants.EXTRA_ID, i.Name);
                intent.putExtra(Constants.EXTRA_DEVICE, i.Address);
                intent.putExtra(Constants.EXTRA_IS_HOST, true);
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
    protected void onDestroy() {
        super.onDestroy();
        AppController.removeHandler(this);
        m_Adapter.Teardown();
    }
}
