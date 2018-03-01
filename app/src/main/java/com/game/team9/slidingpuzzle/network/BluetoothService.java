/*
 * Copyright (c) 2018. $user
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.game.team9.slidingpuzzle.network;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.game.team9.slidingpuzzle.AppController;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import static android.bluetooth.BluetoothDevice.BOND_BONDED;
import static android.bluetooth.BluetoothDevice.BOND_BONDING;
import static android.bluetooth.BluetoothDevice.BOND_NONE;
import static android.bluetooth.BluetoothDevice.EXTRA_BOND_STATE;
import static android.net.ConnectivityManager.EXTRA_REASON;
import static com.game.team9.slidingpuzzle.network.Constants.ACTION_BLUETOOTH_CON;
import static com.game.team9.slidingpuzzle.network.Constants.ACTION_BLUE_UNSUPPORTED;
import static com.game.team9.slidingpuzzle.network.Constants.EXTRA_DEVICE;

/**
 * Created on: 2/1/18
 * Author: David Hiatt - dhiatt89@gmail.com
 */

public class BluetoothService extends Service {

    private static final String TAG = "BTService";
    private static final String MATH_NAME = "BluetoothMathMode";
    private static final String MATH_NAME_INSECURE = "BluetoothMathMode_Insecure";

    private static final UUID MATH_UUID =
            UUID.fromString("15672638-07d4-11e8-ba89-0ed5f89f718b");
    private static final UUID MATH_UUID_INSECURE =
            UUID.fromString("62ec9cfc-0825-11e8-ba89-0ed5f89f718b");


    private final Object m_Lock = new Object();
    public final BluetoothAdapter m_Adapter;
    private final IntentFilter m_Filter = new IntentFilter();
    private STATE m_State = STATE.UNKNOWN;

    @Nullable
    private ConnectThread m_ConnectThread;
    @Nullable
    private AcceptThread m_AcceptThread;

    private int m_BlueIcon;
    private int m_DisabledIcon;
    private int m_SearchingIcon;
    private int m_ConnectedIcon;

    public BluetoothService()
    {
        super();

        m_Filter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
        m_Filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        m_Filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        m_Filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        m_Filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        m_Filter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);

        m_Filter.addAction(ACTION_BLUETOOTH_CON);
        m_Filter.addAction(BluetoothDevice.ACTION_FOUND);
        m_Adapter = BluetoothAdapter.getDefaultAdapter();
        if(m_Adapter == null)
        {
            synchronized (m_Lock)
            {
                m_State = STATE.UNSUPPORTED;
            }
        }
        else
        {
            synchronized (m_Lock)
            {
                if(m_State != STATE.REGISTERED)
                {
                    registerReceiver(m_LocalReceiver, m_Filter);
                    m_State = STATE.REGISTERED;
                }

            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Resources res = getResources();
        final String draw = "drawable";
        m_ConnectedIcon = res.getIdentifier("ic_bluetooth_connected_black_24dp", draw, this.getPackageName());
        m_SearchingIcon = res.getIdentifier("ic_bluetooth_searching_black_24dp", draw, this.getPackageName());
        m_BlueIcon = res.getIdentifier("ic_bluetooth_black_24px", draw, this.getPackageName());
        m_DisabledIcon = res.getIdentifier("ic_bluetooth_disabled_black_24px", draw, this.getPackageName());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return m_Binder;
    }


    public class LocalBinder extends Binder
    {
        public BluetoothService getService()
        {
            return BluetoothService.this;
        }
    }

    private final IBinder m_Binder = new LocalBinder();

    private enum STATE
    {
        UNKNOWN,
        UNSUPPORTED,
        REGISTERED,
        UNREGISTERED,
        CONNECTED
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(m_Adapter != null)
        {
            if(m_Adapter.isEnabled() || m_Adapter.enable())
            {
                Log.i(TAG,"Starting discovery");
                if (m_Adapter.isDiscovering())
                    m_Adapter.cancelDiscovery();
                m_Adapter.startDiscovery();
                if (m_ConnectThread != null) {
                    m_ConnectThread.cancel();
                }

                if (m_AcceptThread == null) {
                    m_AcceptThread = new AcceptThread();
                    m_AcceptThread.start();
                }
            }
            else
            {
                stopSelf();
            }
        }
        else
        {
            Log.w(TAG,"Bluetooth not available");
            sendBroadcast(new Intent(ACTION_BLUE_UNSUPPORTED));
            stopSelf();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public boolean stopService(Intent name) {
        Stop();
        return super.stopService(name);
    }


    @Override
    public void onDestroy() {
        Stop();
        super.onDestroy();
    }
    public void Stop()
    {
        if(m_ConnectThread != null)
        {
            m_ConnectThread.cancel();
        }
        if(m_AcceptThread != null)
        {
            m_AcceptThread.cancel();
            m_AcceptThread = null;
        }

        if(m_Adapter != null)
            m_Adapter.cancelDiscovery();
        synchronized (m_Lock)
        {
            if(m_State == STATE.REGISTERED)
            {
                unregisterReceiver(m_LocalReceiver);
                m_State = STATE.UNREGISTERED;
            }
        }
    }

    public synchronized void Connect(@NonNull PeerInfo dev)
    {
        Log.i(TAG,"Attempting to connected to " + dev);
        if(m_ConnectThread != null)
        {
            m_ConnectThread.cancel();
            m_ConnectThread = null;
        }
        dev.Icon = m_BlueIcon;
        dev.Update(PeerInfo.Status.CONNECTING, info -> {m_ConnectThread.cancel(); dev.Data = null;});
        m_ConnectThread = new ConnectThread((BluetoothDevice)dev.Data);
        m_ConnectThread.start();
    }

    private synchronized void Connected(@NonNull BluetoothSocket sock, @NonNull BluetoothDevice dev)
    {
       // Stop();
        Log.i(TAG,"Connected to " + dev);
        OutputStream o = null;
        InputStream i = null;
        try {
            o = sock.getOutputStream();
        } catch (IOException e) {
            Log.e(TAG, "Faield to open output stream - " + e);
            try {
                sock.close();
            } catch (IOException eb) {
                Log.e(TAG, "Faield to close socket - " + eb);
            }
        }
        try {
            i = sock.getInputStream();
        } catch (IOException e) {
            Log.e(TAG, "Faield to open input stream - " + e);
            try {
                sock.close();
            } catch (IOException eb) {
                Log.e(TAG, "Faield to close socket - " + eb);
            }
        }

        if(i != null && o != null) {
            PeerInfo peer = PeerInfo.Retrieve(dev.getAddress());
            peer.Data = null;
            peer.Icon=m_ConnectedIcon;
            AppController.AddNetwork(new NetworkHandler(sock, dev.getAddress(), i, o));
        }
    }


    private void conFailed(BluetoothDevice dev)
    {
        synchronized (m_Lock)
        {
            m_ConnectThread = null;
        }
        PeerInfo p = PeerInfo.Retrieve(dev.getAddress());
        p.Icon = m_DisabledIcon;
        p.Update(PeerInfo.Status.UNSUPPORTED);
    }

    private class AcceptThread extends Thread {
        @Nullable
        private final BluetoothServerSocket mm_ServerSocket;

        public AcceptThread() {
            // Use a temporary object that is later assigned to mm_ServerSocket
            // because mm_ServerSocket is final.
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code.
                tmp = m_Adapter.listenUsingRfcommWithServiceRecord(MATH_NAME, MATH_UUID);
            } catch (IOException e) {
                Log.e(TAG, "Socket's listen() method failed", e);
            }
            mm_ServerSocket = tmp;
            Log.i(TAG, "Server started");
        }

        @Override
        public void run() {
            setName("Bluetooth server");
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned.
            while (m_State != STATE.CONNECTED) {
                try {
                    socket = mm_ServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "Socket's accept() method failed", e);
                    break;
                }

                if (socket != null) {
                    synchronized (m_Lock)
                    {
                        switch(m_State)
                        {
                            case UNKNOWN:
                            case UNSUPPORTED:
                                break;
                            case REGISTERED:
                                break;
                            case UNREGISTERED:
                            case CONNECTED:
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    Log.e(TAG, "Could not close unwanted socket", e);
                                }
                                break;
                        }
                    }
                    break;
                }
            }
            setName("Bluetooth: Accept end");
        }

        // Closes the connect socket and causes the thread to finish.
        public void cancel() {
            try {
                mm_ServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }

    private class ConnectThread extends Thread {
        @Nullable
        private final BluetoothSocket mm_Socket;
        @NonNull
        private final BluetoothDevice mm_Device;

        public ConnectThread(@NonNull BluetoothDevice device) {
            // Use a temporary object that is later assigned to mm_Socket
            // because mm_Socket is final.
            BluetoothSocket tmp = null;
            mm_Device = device;

            try {
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // MY_UUID is the app's UUID string, also used in the server code.
                tmp = device.createRfcommSocketToServiceRecord(MATH_UUID);
            } catch (IOException e) {
                Log.e("BLUETOOTH", "Socket's create() method failed", e);
            }
            finally {
                mm_Socket = tmp;
            }


        }

        @Override
        public void run() {
            setName("Bluetooth: Connect");
            // Cancel discovery because it otherwise slows down the connection.
            m_Adapter.cancelDiscovery();

            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mm_Socket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    mm_Socket.close();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }finally {
                    conFailed(mm_Device);
                }
            }

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.

            if(mm_Socket.isConnected()) {
                synchronized (m_Lock)
                {
                    m_ConnectThread = null;
                }
                PeerInfo info = PeerInfo.Retrieve(mm_Device.getAddress());
                info.Data = mm_Socket;
                sendBroadcast(new Intent(ACTION_BLUETOOTH_CON).putExtra(EXTRA_DEVICE, mm_Device));
            }
            else
                conFailed(mm_Device);
            //Connected(mm_Socket, mm_Device);
        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mm_Socket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }finally {
                conFailed(mm_Device);
            }
        }
    }


    private final BroadcastReceiver m_LocalReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action == null)
                return;
            switch (action)
            {
                case BluetoothDevice.ACTION_FOUND:
                {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                    PeerInfo i  = PeerInfo.Retrieve(device.getAddress()); // MAC address
                    i.Icon = m_SearchingIcon;
                    i.Data = device;
                    i.Update(device.getName(), PeerInfo.Status.DISCOVERED, BluetoothService.this::Connect);
                }
                break;
                case ACTION_BLUETOOTH_CON:
                {
                    BluetoothDevice dev = intent.getParcelableExtra(EXTRA_DEVICE);
                    PeerInfo peer = PeerInfo.Retrieve(dev.getAddress());
                    Connected((BluetoothSocket)peer.Data, dev);
                }
                break;
                case BluetoothDevice.ACTION_PAIRING_REQUEST:
                {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    device.createBond();
                }
                break;
                case BluetoothDevice.ACTION_BOND_STATE_CHANGED:
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    String deviceName = device.getName();
                    switch(intent.getIntExtra(EXTRA_BOND_STATE, -1))
                    {
                        case BOND_NONE:
                        {
                            Log.w(TAG, "Faield to bond to " + device + ": " + intent.getStringExtra(EXTRA_REASON));
                            PeerInfo info = PeerInfo.Retrieve(device.getAddress());
                            info.Icon = m_DisabledIcon;
                            info.Update(deviceName, PeerInfo.Status.UNSUPPORTED);
                        }
                        break;
                        case BOND_BONDING:
                        {
                            PeerInfo info = PeerInfo.Retrieve(device.getAddress());
                            info.Icon = m_ConnectedIcon;
                            info.Update(deviceName, PeerInfo.Status.CONNECTING);
                        }
                            break;
                        case BOND_BONDED:
                        {
                            PeerInfo info = PeerInfo.Retrieve(device.getAddress());
                            info.Data = device;
                            Connect(info);
                        }
                    }

                    break;

                case BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED:
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                case BluetoothAdapter.ACTION_SCAN_MODE_CHANGED:
                    break;
            }
        }
    };

}
