/*
 * Copyright (c) 2018. $user
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.game.team9.slidingpuzzle;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.game.team9.slidingpuzzle.network.MathClientActivity;
import com.game.team9.slidingpuzzle.network.MathServerAcitivity;
import com.game.team9.slidingpuzzle.network.bluetooth.Constants;
import com.game.team9.slidingpuzzle.network.bluetooth.MathBluetoothActivity;
import com.game.team9.slidingpuzzle.network.wifi.MathWifiActivity;
import com.game.team9.slidingpuzzle.network.wifi.PeerBroadcastReceiver;

import java.util.concurrent.Executors;

public class MathOnlineDiscoveryActivity extends AppCompatActivity {

    private static PeerBroadcastReceiver s_Receiver;
    private static WifiP2pManager.Channel s_Channel;
    private static WifiP2pManager s_Manager;

    private static final Object m_Lock = new Object();

    private static boolean m_Bound = false;/*
    @Nullable
    private static BluetoothService m_Service;
    @Nullable
    private final ServiceConnection m_Conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            // This is called when the connection with the service has been
            // established, giving us the object we can use to
            // interact with the service.  We are communicating with the
            // service using a Messenger, so here we get a client-side
            // representation of that from the raw IBinder object.
            Log.i("SERVICE", "Connected");
            // MathModeService serv = (MathModeService)iBinder;
            synchronized (m_Lock) {
                if(!m_Bound) {
                    BluetoothService.LocalBinder binder = (BluetoothService.LocalBinder)iBinder;

                    m_Service = binder.getService();
                    m_Service.setHandler(m_Bhandle);
                    m_Bound = true;
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.e("SERVICE", "Disconnect");
            synchronized (m_Lock) {
                if(m_Bound)
                {
                    m_Service.closeHandler();
                    m_Service = null;
                    m_Bound = false;
                }
            }
        }
    };


    private final BluetoothHandler m_Bhandle = new BluetoothHandler();
*/
    private static final IntentFilter s_WFilter = new IntentFilter();
    private static final IntentFilter s_BFilter = new IntentFilter();

    static {
        s_WFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        s_WFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        s_WFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        s_WFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        s_BFilter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        s_BFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        s_BFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        s_BFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        s_BFilter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        s_BFilter.addAction(BluetoothDevice.ACTION_FOUND);
        s_BFilter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
    }

    private boolean m_WifiEnabled;
    private boolean m_BlueEnabled;
    private TextView m_Status;
    private TextView m_BlueText;

    private int m_Mode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_math_online_discovery);
        m_Status = findViewById(R.id.statusText);
        Executors.defaultThreadFactory().newThread(() -> {

            setWifiStatus(s_Manager != null);
        }).start();
        Intent intent = getIntent();
        m_Mode = intent.getIntExtra("MODE", -1);
        WifiManager wifiMgr = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);


        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (BluetoothAdapter.getDefaultAdapter() == null) {
            m_Status.setText("No bluetooth service available.  ");

        } else
            findViewById(R.id.blueButton).setEnabled(m_BlueEnabled = true);
        if (!wifiMgr.isWifiEnabled())
            m_WifiEnabled = wifiMgr.setWifiEnabled(true);
        if (!mWifi.isAvailable()) {
            m_Status.setText(m_Status.getText() + "No wireless service available!  ");
        } else if (!wifiMgr.isP2pSupported()) {
            m_Status.setText(m_Status.getText() + "Wifi P2P not supported!  ");
        } else {
            findViewById(R.id.wifiButton).setEnabled(m_WifiEnabled = true);
            s_Manager = (WifiP2pManager) getSystemService(WIFI_P2P_SERVICE);
        }

        if (s_Manager == null) {
            m_Status.setText(m_Status.getText() + "  No p2p manager found!  ");
            m_WifiEnabled = wifiMgr.setWifiEnabled(false);
        } else
            s_Channel = s_Manager.initialize(this, getMainLooper(), null);
    }

    public void Wifi_OnClick(View view) {
        Intent intent = new Intent(MathOnlineDiscoveryActivity.this, MathWifiActivity.class);
        intent.putExtra("MODE", m_Mode);
        startActivity(intent);
    }

    public void Blue_OnClick(View view) {
        Intent intent = new Intent(MathOnlineDiscoveryActivity.this, MathBluetoothActivity.class);
        intent.putExtra("MODE", m_Mode);
        startActivity(intent);
    }
    
    private void setWifiStatus(boolean b)
    {
        m_WifiEnabled = b;
    }


    public void onWifiStatusChanged(boolean b)
    {
            Toast toast = new Toast(getApplicationContext());
            toast.setText(b ? "Wifi enabled" : "Wifi disabled");
            toast.setDuration(Toast.LENGTH_LONG);
            toast.show();
    }

    private void tryBluetooth()
    {
        endWifi();
    /*    if(!m_Bound)
        {
          //  m_Bound= true;
           // s_BlueMan.setHandler(m_Bhandle);
            Intent intent = new Intent(this, BluetoothService.class);
            bindService(intent, m_Conn, Context.BIND_AUTO_CREATE);
        }
        if(m_Service != null) {
            switch (m_Service.getBState()) {
                case Constants.STATE_CONNECTED:
                case Constants.STATE_CONNECTING:
                case Constants.STATE_LISTEN:
                    break;
                case Constants.STATE_NONE:
                    m_Service.Start();
                    break;
                case Constants.STATE_DISABLED:
                    Intent req = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(req, Constants.REQUEST_ENABLE_BT);
                    break;
                case Constants.STATE_UNSUPPORTED:
                    endBluetooth();
                    break;
            }
        }*/
    }

    private void endBluetooth()
    {
        if(m_Bound)
        {
            m_Bound = false;
      //      unbindService(m_Conn);
      //      m_Service.closeHandler();
        }

    }

    private void tryWifi()
    {
        endBluetooth();
        Handler handler = new Handler(Looper.getMainLooper());
        Context ctx = this;
        s_Manager.discoverPeers(s_Channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ctx, "Peer discovery started", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(int reason) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ctx, "Peer discovery Failed", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

    }

    private void endWifi()
    {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0] == PackageManager.PERMISSION_DENIED)
        {

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == Constants.REQUEST_ENABLE_BT)
        {
            if(resultCode == RESULT_OK)
            {
                tryBluetooth();
            }
            else
            {
               endBluetooth();
            }
        }
    }


    private class BluetoothHandler extends Handler
    {
        private BluetoothHandler(){}
        @Override
        public void handleMessage(@NonNull Message msg) {

            switch (msg.what)
            {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch(msg.arg1)
                    {
                        case Constants.STATE_CONNECTED:
                        case Constants.STATE_CONNECTING:
                        case Constants.STATE_LISTEN:
                        case Constants.STATE_NONE:
                    }
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                case Constants.MESSAGE_READ:
                case Constants.MESSAGE_TOAST:
                case Constants.MESSAGE_WRITE:
            }
        }

        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);
        }

        @Override
        public boolean sendMessageAtTime(Message msg, long uptimeMillis) {
            return super.sendMessageAtTime(msg, uptimeMillis);
        }
    }
}
