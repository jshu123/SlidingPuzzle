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
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.game.team9.slidingpuzzle.network.bluetooth.Constants;
import com.game.team9.slidingpuzzle.network.bluetooth.BluetoothService;
import com.game.team9.slidingpuzzle.network.wifi.PeerBroadcastReceiver;

public class MathOnlineDiscoveryActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    private static PeerBroadcastReceiver s_Receiver;
    private static WifiP2pManager.Channel s_Channel;
    private static WifiP2pManager s_Manager;

    private static final Object m_Lock = new Object();

    private static boolean m_Bound = false;
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
            // WifiP2PService serv = (WifiP2PService)iBinder;
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

    private static final IntentFilter s_WFilter = new IntentFilter();
    private static final IntentFilter s_BFilter = new IntentFilter();

    static{
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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_math_online_discovery);

        if(savedInstanceState == null)
        {

        }
       // s_Manager = (WifiP2pManager)getSystemService(Context.WIFI_P2P_SERVICE);
       // s_Channel = s_Manager.initialize(this, null, null);
        //s_Receiver = new PeerBroadcastReceiver(s_Manager, s_Channel, this);

        Switch net = findViewById(R.id.networkSwitch);
        net.setOnCheckedChangeListener(this);
        tryBluetooth();
    }


    @Override
    protected void onStart() {
        super.onStart();
        // Bind to the service
        bindService(new Intent(this, BluetoothService.class), m_Conn,
                Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        synchronized (m_Lock)
        {
            if(m_Bound)
            {
                unbindService(m_Conn);
                m_Bound = false;
            }
        }
    }

    private void replaceFragment(android.support.v4.app.Fragment frag, int layout)
    {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(layout, frag);
        transaction.commit();
    }

    public void onWifiStatusChanged(boolean b)
    {

    }

    private void tryBluetooth()
    {
        endWifi();
        if(!m_Bound)
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
        }
    }

    private void endBluetooth()
    {
        if(m_Bound)
        {
            m_Bound = false;
            unbindService(m_Conn);
            m_Service.closeHandler();
        }

    }

    private void tryWifi()
    {
        endBluetooth();
    }

    private void endWifi()
    {

    }

    private static void checkWifi(@NonNull Context context)
    {
        WifiManager wifiMgr = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if(wifiMgr.isWifiEnabled())
        {
            if(wifiMgr.isP2pSupported())
            {

            }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

        if(b)
        {
            tryBluetooth();
            //replaceFragment(new BluetoothDeviceFragment(), R.id.fragmentNet);
        }
        else
        {
            tryWifi();
            //replaceFragment(new DevListFragment(), R.id.fragmentNet);
        }
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
