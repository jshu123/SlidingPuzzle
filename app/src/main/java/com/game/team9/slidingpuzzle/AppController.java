/*
 * Copyright (c) 2018. $user
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.game.team9.slidingpuzzle;

import android.app.Application;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.game.team9.slidingpuzzle.network.ConListener;
import com.game.team9.slidingpuzzle.network.IPacketHandler;
import com.game.team9.slidingpuzzle.network.NetworkHandler;
import com.game.team9.slidingpuzzle.network.NetworkReceiver;
import com.game.team9.slidingpuzzle.network.Packet;
import com.game.team9.slidingpuzzle.network.PeerInfo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

import static com.game.team9.slidingpuzzle.network.Constants.PREF;
import static com.game.team9.slidingpuzzle.network.Constants.PREF_LAST_ONLINE_MODE;
import static com.game.team9.slidingpuzzle.network.Constants.PREF_LAST_ROUNDS;

/**
 * Created on: 2/6/18
 * Author: David Hiatt - dhiatt89@gmail.com
 */

public class AppController extends Application implements NetworkReceiver.IDataInbound {

    public static final boolean DEBUG = true;
    private static final String TAG = "Controller";
    private static int PORT = 49152;
    private static AppController s_Instance;

    private ConListener m_Connection; //The main server for NSD

    private final HashMap<String, NetworkHandler> m_Listeners = new HashMap<>();

    private final SortedSet<IPacketHandler> m_PacketHandlers = new TreeSet<>();

    private final Packet[] m_Log = new Packet[256];
    private int m_Index = 0;
    private int m_LastHandled = 0;

    public static void addHandler(IPacketHandler context) {
        Log.i(TAG, "Adding handler " + context);
        s_Instance._setHandler(context);
    }

    public static void removeHandler(IPacketHandler c)
    {
        s_Instance.m_PacketHandlers.remove(c);
        Log.i(TAG,"Removing handler " + c);
    }

    private void _setHandler(IPacketHandler ctx)
    {
        m_PacketHandlers.add(ctx);
        if(m_LastHandled != m_Index)
        {
            while(m_LastHandled != m_Index && m_Log[m_LastHandled].Type == Packet.Header.FREE)
                m_LastHandled = (m_LastHandled+1) % 256;
            boolean handled = true;
            while(m_LastHandled != m_Index && handled)
            {
                handled = false;
                for (IPacketHandler packetHandler : m_PacketHandlers) {
                    if(packetHandler.handleData(m_Log[m_LastHandled]))
                    {
                        m_LastHandled = (m_LastHandled+1) % 256;
                        handled = true;
                        break;
                    }
                }
            }
        }
    }


    public static void endGame(String id)
    {
        s_Instance._endGame(id);
    }

    private void _endGame(String id)
    {
     /*   NetworkHandler h = m_Listeners.remove(id);
        if(h != null)
            h.Terminate();*/
    }

    public static void SendData(Packet data) {
        s_Instance._sendData(data);
    }

    private void _sendData(Packet p)
    {
        NetworkHandler h = m_Listeners.get(p.Source);
        if(h != null)
        {
            h.QueueMessage(p);
        }
        else
            Log.w(TAG, "Cannot find handler to send " + p);
    }

    public static int getRounds()
    {
        return s_Instance._getRounds();
    }

    private int _getRounds()
    {
        SharedPreferences pref = getSharedPreferences(PREF, MODE_PRIVATE);
        return pref.getInt(PREF_LAST_ROUNDS, 1);
    }

    public static int getGameMode()
    {
        return s_Instance._getGameMode();
    }

    private int _getGameMode()
    {
        SharedPreferences pref = getSharedPreferences(PREF, MODE_PRIVATE);
        return pref.getString(PREF_LAST_ONLINE_MODE, "BASIC").equals("BASIC") ? 0 : 1;
    }

    public static AppController getInstance(){return s_Instance;}

    public static int getPort(){return PORT;}

    public static void RemoveNetwork(String h)
    {
        s_Instance._RemoveNetwork(h);
    }

    private void _RemoveNetwork(String h)
    {
       NetworkHandler ha = m_Listeners.remove(h);
       if(ha != null)
       {
           PeerInfo peer = PeerInfo.Retrieve(h);
           peer.Update(PeerInfo.Status.INVALID);
           ha.Terminate();
       }
    }

    public static void AddNetwork(NetworkHandler h)
    {
        s_Instance._AddNetwork(h);
    }

    private void _AddNetwork(NetworkHandler h){
        if(m_Listeners.containsKey(h.Id))
        {
            Log.i(TAG, "Duplicate handler for " + h.Id);
            h.Terminate();
        }
        else
        {
            Log.i(TAG,"Registering listener for " + h.Id);
            m_Listeners.put(h.Id, h);
            h.Start();
            PeerInfo info = PeerInfo.Retrieve(h.Id);
            info.Update(PeerInfo.Status.AVAILABLE);
        }
    }

    public static void bindSocket(String host, int port)
    {
        if(s_Instance.m_Listeners.containsKey(host))
        {
            PeerInfo peer = PeerInfo.Retrieve(host);
            peer.Update(PeerInfo.Status.AVAILABLE);
            return;
        }
        try {
            s_Instance._bindSocket(new Socket(host, port));
        } catch (IOException e) {
            Log.e(TAG, "Failed to bind to " + host + ":" + port + " - " + e);

        }
    }

    public static void bindSocket(Socket s)
    {
        s_Instance._bindSocket(s);
    }

    private void _bindSocket(Socket s)
    {
        OutputStream o = null;
        InputStream i = null;
        String addr = s.getInetAddress().getHostAddress();
        try {
            o = s.getOutputStream();
        } catch (IOException e) {
            Log.e(TAG, "Failed to open output stream - " + e);
        }
        try {
            i = s.getInputStream();
        } catch (IOException e) {
            Log.e(TAG, "Failed to open input stream - " + e);
        }
        if(i == null || o == null)
        {
            Log.e(TAG,"Failed to handle socket for " + addr);
            try {
                s.close();
            } catch (IOException e) {
                Log.e(TAG,"Failed to close socket - " + e);
            }
            return;
        }
        _AddNetwork(new NetworkHandler(s, addr, i, o));
    }

    @Override
    public void onCreate() {
        super.onCreate();
        s_Instance = this;
        PeerInfo.Broadcaster = LocalBroadcastManager.getInstance(getApplicationContext());
        m_Connection = new ConListener(PORT = findPort());
        m_Connection.start();
    }

    private static int findPort()
    {
        int p = -1;
            try (ServerSocket s = new ServerSocket(0)) {
                p = s.getLocalPort();
            }
            catch (IOException e) {
            Log.e(TAG,"Failed to find port - " + e);
        }
        return p;
    }


    @Override
    public void onTerminate() {
        for (NetworkHandler networkHandler : m_Listeners.values()) {
            networkHandler.Terminate();
        }
        if(m_Connection.isAlive())
        {
            m_Connection.Close();
            try {
                m_Connection.join();
            } catch (InterruptedException e) {
                Log.e(TAG, "Failed to join server thread");
            }
        }
        super.onTerminate();
    }

    @Override
    public void onReceive(Packet p) {

        m_Log[m_Index++] = p;
        switch (p.Type)
        {
            case FREE:
                break;
            case REQUEST:
            {
                PeerInfo i = PeerInfo.Retrieve(p.Source);
                i.Data = p.Data[1];
                i.Update(p.Data[0] == 0 ? PeerInfo.Status.INBOUND_REQUEST_BAS : PeerInfo.Status.INBOUND_REQUEST_CUT);
                m_LastHandled++;
            }
            p.Free();
                return;

            case QUIT:
                break;
            case MOVE:
                break;
            case TIME:
                break;
            case INIT:
                break;
            case USER:
            {
                PeerInfo i = PeerInfo.Retrieve(p.Source);
                int len = p.Length;
                i.Update(new String(p.Data, 0, len));
            }
                m_LastHandled++;
            p.Free();
                return;
        }

        boolean handled = false;
        for (IPacketHandler packetHandler : m_PacketHandlers) {
            if (packetHandler.handleData(p))
            {
                handled = true;
                ++m_LastHandled;
                break;
            }
        }
        if(!handled)
        {
            Log.w(TAG, "No game handler, ignoring packet: " + p);
            if(p.Type == Packet.Header.QUIT)
            {
                PeerInfo i = PeerInfo.Retrieve(p.Source);
                i.Update(PeerInfo.Status.AVAILABLE);
                ++m_LastHandled;
            }
        }
    }
}
