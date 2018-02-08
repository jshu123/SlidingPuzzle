/*
 * Copyright (c) 2018. $user
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.game.team9.slidingpuzzle.network.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.ProgressBar;

import com.game.team9.slidingpuzzle.MathDoubleBasicActivity;
import com.game.team9.slidingpuzzle.MathDoubleCuthroatActivity;
import com.game.team9.slidingpuzzle.R;
import com.game.team9.slidingpuzzle.network.Constants;
import com.game.team9.slidingpuzzle.network.DeviceObject;

import java.io.ObjectOutputStream;

public class MathWifiActivity extends AppCompatActivity implements WifiBroadcastReceiver.IWifiStatusChanger, DevListFragment.IDeviceListener {


    private static final IntentFilter m_LocalFilter = new IntentFilter();
    private static final IntentFilter m_WifiFilter = new IntentFilter();

    static {
        m_LocalFilter.addAction(Constants.DEVICE_LIST_CHANGED);
        m_LocalFilter.addAction(Constants.PLAY_RESPONSE_RECEIVED);

        m_WifiFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        m_WifiFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        m_WifiFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        m_WifiFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

    }

    private WifiP2pManager m_Manager;
    private boolean m_Enabled;
    private WifiP2pManager.Channel m_Channel;
    private ProgressBar m_Progress;
    private WifiBroadcastReceiver m_WifiReceiver;
    private DevListFragment m_DevList;
    private DevDetailFragment m_DevDetail;
    private Class m_Class;
    private int m_PeerCount;
    private BroadcastReceiver m_LocalReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case Constants.PLAY_RESPONSE_RECEIVED:
                    boolean s = intent.getBooleanExtra("ACCEPT", false);

                case Constants.DEVICE_LIST_CHANGED:

            }
        }
    };

    public boolean isWifiP2pEnabled() {
        return m_Enabled;
    }

    public void Reset() {
        if (m_DevList != null)
            m_DevList.clearPeers();

        if (m_DevDetail != null)
            m_DevDetail.Reset();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_math_wifi);
        Intent intent = getIntent();
        m_Class = intent.getIntExtra("MODE", -1) == 1 ? MathDoubleCuthroatActivity.class : MathDoubleBasicActivity.class;
        Object o = getSystemService(Context.WIFI_P2P_SERVICE);
        m_Manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        m_Channel = m_Manager.initialize(this, getMainLooper(), null);
        m_Progress = findViewById(R.id.progressBar);
    }

    @Override
    protected void onResume() {
        m_WifiReceiver = new WifiBroadcastReceiver(m_Manager, m_Channel, this);
        LocalBroadcastManager.getInstance(MathWifiActivity.this).registerReceiver(m_WifiReceiver, m_WifiFilter);
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(MathWifiActivity.this).unregisterReceiver(m_WifiReceiver);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onWifiStatusChanged(boolean state) {
        m_Enabled = state;
    }

    @Override
    public void showDetails(DeviceObject device) {
        m_DevDetail.devInfo(device);
    }

    @Override
    public void cancelDisconnect() {

    }

    @Override
    public void connect(Parcelable config) {
        m_Manager.connect(m_Channel, (WifiP2pConfig) config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Intent intent = new Intent(MathWifiActivity.this, m_Class);
                startActivity(intent);
            }

            @Override
            public void onFailure(int reason) {

            }
        });
    }

    @Override
    public void disconnect() {

    }
}
