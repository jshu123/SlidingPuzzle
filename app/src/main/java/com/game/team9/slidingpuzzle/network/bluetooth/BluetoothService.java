/*
 * Copyright (c) 2018. $user
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.game.team9.slidingpuzzle.network.bluetooth;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created on: 2/1/18
 * Author: David Hiatt - dhiatt89@gmail.com
 */

public class BluetoothService extends Service{

    private static final String MATH_NAME = "BluetoothMathMode";
    private static final String MATH_NAME_INSECURE = "BluetoothMathMode_Insecure";

    private static final UUID MATH_UUID =
            UUID.fromString("15672638-07d4-11e8-ba89-0ed5f89f718b");
    private static final UUID MATH_UUID_INSECURE =
            UUID.fromString("62ec9cfc-0825-11e8-ba89-0ed5f89f718b");


    private final Object m_Lock = new Object();
    public static final BluetoothAdapter m_Adapter;
    private static final AtomicReference<Handler> s_Handle = new AtomicReference<>();
    private int m_State;
    @Nullable
    private ConnectedThread m_ConnectedThread;
    @Nullable
    private ConnectThread m_ConnectThread;
    @Nullable
    private AcceptThread m_AcceptThread;

    private boolean m_Started = false;

    static {
        m_Adapter = BluetoothAdapter.getDefaultAdapter();
    }


    public BluetoothService()
    {
        if(m_Adapter == null)
            setState(Constants.STATE_UNSUPPORTED);
        else
            setState(Constants.STATE_NONE);
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

    public void setHandler(Handler handle)
    {
        synchronized (m_Lock) {
            s_Handle.set(handle);
        }
        if(m_Started)
            Start();
    }

    public Handler transferHandle(Handler handle)
    {
        synchronized (m_Lock) {
            return s_Handle.getAndSet(handle);
        }
    }

    public void closeHandler()
    {
        Stop();
        synchronized (m_Lock) {
            s_Handle.set(null);
        }
    }

    public void Discover()
    {
            if (m_Adapter.isDiscovering())
                m_Adapter.cancelDiscovery();
            m_Adapter.startDiscovery();
    }

    public synchronized int getBState()
    {
        return m_State;
    }


    private void setState(int state)
    {
        synchronized (m_Lock) {
            Handler handle = s_Handle.get();
            if(handle!=null && state != m_State) {
                handle.obtainMessage(Constants.MESSAGE_STATE_CHANGE, state).sendToTarget();
            }
            m_State = state;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(m_Adapter == null)
        {
            String stop = intent.getStringExtra("Stop");
            if(stop != null && stop.length() > 0)
                Stop();
            else
                Start();
        }
        else
            Stop();

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

    public synchronized void Start()
    {
        m_Started = true;
        if(s_Handle.get() != null) {
            if (m_ConnectThread != null) {
                m_ConnectThread.cancel();
                m_ConnectThread = null;
            }
            if (m_ConnectedThread != null) {
                m_ConnectedThread.cancel();
                m_ConnectedThread = null;
            }
            if (m_AcceptThread == null) {
                m_AcceptThread = new AcceptThread();
                m_AcceptThread.start();
            }
        }
    }

    public synchronized void Stop()
    {
        m_Started = false;
        if(m_ConnectThread != null)
        {
            m_ConnectThread.cancel();
            m_ConnectThread = null;
        }
        if(m_AcceptThread != null)
        {
            m_AcceptThread.cancel();
            m_AcceptThread = null;
        }
        if(m_ConnectedThread != null)
        {
            m_ConnectedThread.cancel();
            m_ConnectedThread = null;
        }
        if(m_Adapter != null)
            m_Adapter.cancelDiscovery();
        setState(Constants.STATE_NONE);
    }

    public synchronized void Connect(@NonNull BluetoothDevice dev)
    {
        if(m_ConnectedThread != null)
        {
            m_ConnectedThread.cancel();
            m_ConnectedThread = null;
        }
        if(m_ConnectThread != null)
        {
            m_ConnectThread.cancel();
            m_ConnectThread = null;
        }

        m_ConnectThread = new ConnectThread(dev);
        m_ConnectThread.start();
    }

    public void Write(@NonNull byte[] buffer, int len)
    {
        synchronized (m_Lock)
        {
            if(m_State != Constants.STATE_CONNECTED)
                return;
        }
        m_ConnectedThread.write(buffer, len);
    }


    private synchronized void Connected(@NonNull BluetoothSocket sock, @NonNull BluetoothDevice dev)
    {
        Stop();
        m_ConnectedThread = new ConnectedThread(sock);
        m_ConnectedThread.start();

        Message m = s_Handle.get().obtainMessage(Constants.MESSAGE_DEVICE_NAME);
        Bundle b = new Bundle();
        b.putString("Device Name", dev.getName());
        m.setData(b);
        s_Handle.get().sendMessage(m);
    }


    private void conFailed()
    {
        setState(Constants.STATE_NONE);

        Message m = s_Handle.get().obtainMessage(Constants.MESSAGE_TOAST);
        Bundle b = new Bundle();
        b.putString("Status", "Unable to connect");
        m.setData(b);
        s_Handle.get().sendMessage(m);

    }

    private void conLost()
    {
        setState(Constants.STATE_NONE);

        Message m = s_Handle.get().obtainMessage(Constants.MESSAGE_TOAST);
        Bundle b = new Bundle();
        b.putString("Status", "Connection lost");
        m.setData(b);
        s_Handle.get().sendMessage(m);
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
                Log.e("BLUETOOTH", "Socket's listen() method failed", e);
            }
            mm_ServerSocket = tmp;
            setState(Constants.STATE_LISTEN);
        }

        public void run() {
            setName("Bluetooth: Accept start");
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned.
            while (getBState() != Constants.STATE_CONNECTED) {
                try {
                    socket = mm_ServerSocket.accept();
                } catch (IOException e) {
                    Log.e("BLUETOOTH", "Socket's accept() method failed", e);
                    break;
                }

                if (socket != null) {
                    synchronized (m_Lock)
                    {
                        switch(m_State)
                        {
                            case Constants.STATE_LISTEN:
                            case Constants.STATE_CONNECTING:
                                Connected(socket, socket.getRemoteDevice());
                                break;
                            case Constants.STATE_NONE:
                            case Constants.STATE_CONNECTED:
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    Log.e("BLUETOOTH", "Could not close unwanted socket", e);
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
                Log.e("BLUETOOTH", "Could not close the connect socket", e);
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
            setState(Constants.STATE_CONNECTING);
        }

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
                    Log.e("BLUETOOTH", "Could not close the client socket", closeException);
                }finally {
                    conFailed();
                    return;
                }
            }

            synchronized (m_Lock)
            {
                m_ConnectThread = null;
            }
            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            Connected(mm_Socket, mm_Device);
        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mm_Socket.close();
            } catch (IOException e) {
                Log.e("BLUETOOTH", "Could not close the client socket", e);
            }
        }
    }


    private class ConnectedThread extends Thread{
        @NonNull
        private final BluetoothSocket m_Socket;
        @Nullable
        private final InputStream m_In;
        @Nullable
        private final OutputStream m_Out;
        private boolean m_Connected;

        public ConnectedThread(@NonNull BluetoothSocket socket)
        {
            m_Socket = socket;
            InputStream ti = null;
            OutputStream to = null;

            try {
                ti = socket.getInputStream();
                to = socket.getOutputStream();
                m_Connected = true;
            } catch (IOException e) {
                e.printStackTrace();

            }
            finally
            {
                m_In = ti;
                m_Out = to;
            }

            if (m_Connected) {
                setState(Constants.STATE_CONNECTED);
            }
        }

        public void run()
        {
            byte[] buffer = new byte[1024];
            int length;
            while(getBState() == Constants.STATE_CONNECTED)
            {
                try {
                    length = m_In.read(buffer);
                    //Handler
                    s_Handle.get().obtainMessage(Constants.MESSAGE_READ, length, -1,buffer).sendToTarget();
                } catch (IOException e) {
                    Log.e("BLUETOOTH", "Connection lost", e);
                    //Con lost
                    conLost();
                    break;
                }
            }
        }

        public void write(@NonNull byte[] buffer, int len)
        {
            try {
                m_Out.write(buffer, 0, len);
                s_Handle.get().obtainMessage(Constants.MESSAGE_WRITE,buffer.length, -1, buffer).sendToTarget();
            } catch (IOException e) {
                Log.e("BLUETOOTH", "Exception during write", e);
            }
        }

        public void cancel()
        {
            try {
                m_Socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            finally
            {
                m_Connected =  false;
            }
        }
    }
}
