/*
 * Copyright (c) 2018. $user
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.game.team9.slidingpuzzle.network;

import android.support.annotation.NonNull;
import android.util.Log;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created on: 2/18/18
 * Author: David Hiatt - dhiatt89@gmail.com
 */

public class NetworkHandler implements IPacketHandler
{
    private static final String TAG = "Network";
    private final Closeable m_Socket;

    private final NetworkReceiver m_Receiver;
    private final NetworkSender m_Sender;

    public final String Id;

    private final Object m_Lock = new Object();


    public NetworkHandler(Closeable s, String id, InputStream i, OutputStream o)
    {
        Id = id;
        m_Socket = s;
        m_Receiver = new NetworkReceiver(Id, i);
        m_Sender = new NetworkSender(Id, o);

    }

    public void Start(){
        synchronized (m_Lock)
        {
            if(!m_Receiver.isAlive())
                m_Receiver.start();
        }

    }

    public void Activate()
    {
        synchronized (m_Lock)
        {
            if(m_Sender.isAlive())
                Log.e(TAG,"Multiple activation attempts on " + m_Socket);
            else
            {
                m_Sender.start();
            }
        }
    }

    public void QueueMessage(Packet p)
    {
        Log.i(TAG,"Sending data - " + p);
        if(m_Sender == null)
            Activate();
            m_Sender.m_SendQueue.add(p);
    }

    public void Terminate()
    {
        synchronized (m_Lock) {
                m_Receiver.Close();
                m_Sender.Close();
            try {
                m_Socket.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing socket - " + e);
            }
            finally {
                PeerInfo peer = PeerInfo.Retrieve(Id);
                peer.Update(PeerInfo.Status.INVALID);
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof String)
            return Id.equals(obj);
        if(obj instanceof NetworkHandler)
            return Id.equals(((NetworkHandler)obj).Id);
        return super.equals(obj);
    }

    @Override
    public int Priority() {
        return Constants.PRIORITY_NETHANDLER;
    }

    @Override
    public boolean handleData(Packet p) {
        if(p.Type == Packet.Header.USER && p.Source.equals(Id))
        {
        }
        return false;
    }

    @Override
    public int compareTo(@NonNull IPacketHandler o) {
        return Priority() - o.Priority();
    }
}
