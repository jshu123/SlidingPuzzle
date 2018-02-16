/*
 * Copyright (c) 2018. $user
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.game.team9.slidingpuzzle;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.game.team9.slidingpuzzle.network.DeviceObject;
import com.game.team9.slidingpuzzle.network.bluetooth.Constants;
import com.game.team9.slidingpuzzle.network.bluetooth.MathBluetoothActivity;
import com.game.team9.slidingpuzzle.network.wifi.DevDetailFragment;
import com.game.team9.slidingpuzzle.network.wifi.DevListFragment;
import com.game.team9.slidingpuzzle.network.wifi.MathWifiActivity;
import com.game.team9.slidingpuzzle.network.wifi.PeerBroadcastReceiver;
import com.game.team9.slidingpuzzle.network.wifi.WifiBroadcastReceiver;

public class MathOnlineDiscoveryActivity extends AppCompatActivity implements WifiBroadcastReceiver.IWifiStatusChanger, DevListFragment.IDeviceListener {

    private static PeerBroadcastReceiver s_Receiver;
    private static WifiP2pManager.Channel s_Channel;
    private WifiP2pManager m_Manager;

    private WifiP2pManager.Channel m_Channel;
    private WifiBroadcastReceiver m_WifiReceiver;
    private DevListFragment m_DevList;
    private DevDetailFragment m_DevDetail;
    private boolean m_WifiEnabled;
    private TextView m_Status;
    private boolean m_Bound;
    private Button m_Discover;
    private Class m_Class;

    private int m_Mode;
    private static final IntentFilter m_WifiFilter = new IntentFilter();

    static {
        m_WifiFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        m_WifiFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        m_WifiFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        m_WifiFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

    }
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_math_online_discovery);
        m_Status = findViewById(R.id.statusText);
        Intent intent = getIntent();
        m_Class = intent.getIntExtra("MODE", -1) == 1 ? MathDoubleCuthroatActivity.class : MathDoubleBasicActivity.class;
        WifiManager wifiMgr = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        m_Discover = findViewById(R.id.wifiButton);

        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (!wifiMgr.isWifiEnabled())
            m_WifiEnabled = wifiMgr.setWifiEnabled(true);
        if (!mWifi.isAvailable()) {
            m_Status.setText(m_Status.getText() + "No wireless service available!  ");
        } else if (!wifiMgr.isP2pSupported()) {
            m_Status.setText(m_Status.getText() + "Wifi P2P not supported!  ");
        } else {
            m_Manager = (WifiP2pManager) getSystemService(WIFI_P2P_SERVICE);
        }


        if (m_Manager == null) {
            m_Status.setText(m_Status.getText() + "  No p2p manager found!  ");
            wifiMgr.setWifiEnabled(false);
            setWifiStatus(false);
        } else
        {
            m_Channel = m_Manager.initialize(this, getMainLooper(), null);
            setWifiStatus(true);
        }
    }

    public void Wifi_OnClick(View view) {
        Start();
    }

    private void setWifiStatus(boolean b)
    {
        m_WifiEnabled = b;
        if(b && !m_Bound)
        {
            Start();
        }
        else if(!b && m_Bound)
        {
            LocalBroadcastManager.getInstance(MathOnlineDiscoveryActivity.this).unregisterReceiver(m_WifiReceiver);
            m_Bound = false;
        }
        m_Discover.setEnabled(b);
    }

    private void Start()
    {
        LocalBroadcastManager.getInstance(MathOnlineDiscoveryActivity.this).registerReceiver(m_WifiReceiver, m_WifiFilter);
        m_Bound = true;
    }





    @Override
    protected void onResume() {
        if(m_Bound) {
            m_WifiReceiver = new WifiBroadcastReceiver(m_Manager, m_Channel, this);
            LocalBroadcastManager.getInstance(MathOnlineDiscoveryActivity.this).registerReceiver(m_WifiReceiver, m_WifiFilter);
        }
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
        if(m_Bound)
            LocalBroadcastManager.getInstance(MathOnlineDiscoveryActivity.this).unregisterReceiver(m_WifiReceiver);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onWifiStatusChanged(boolean b)
    {
        Toast toast = new Toast(getApplicationContext());
        toast.setText(b ? "Wifi enabled" : "Wifi disabled");
        toast.setDuration(Toast.LENGTH_LONG);
        toast.show();
        m_WifiEnabled = b;
        if(b)
        {
            m_Manager.discoverPeers(s_Channel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    m_Status.setText("Discovering...");
                }

                @Override
                public void onFailure(int reason) {
                    m_Status.setText("Discovery failed.");
                }
            });
        }
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
                Intent intent = new Intent(MathOnlineDiscoveryActivity.this, m_Class);
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


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0] == PackageManager.PERMISSION_DENIED)
        {

        }
    }
}
