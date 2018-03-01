/*
 * Copyright (c) 2018. $user
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.game.team9.slidingpuzzle.network;

import android.util.Log;

import java.io.OutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created on: 2/18/18
 * Author: David Hiatt - dhiatt89@gmail.com
 */

public class NetworkSender extends Thread {

    private static final String TAG = "Network";
    private final String m_Addr;
    private final OutputStream m_Stream;
    private boolean m_Closed;

    public NetworkSender(String name, OutputStream o)
    {
        m_Addr = name;
        m_Stream = o;
    }


    public void Close() {m_Closed = true;
    m_SendQueue.add(Packet.AcquirePacket(m_Addr, Packet.Header.FREE)); //Add a dummy packet in case the thread is waiting
    }

    final BlockingQueue<Packet> m_SendQueue = new LinkedBlockingQueue<>();

    @Override
    public void run() {
        if(m_Stream == null)
        {
            Log.e(TAG,"Attempted to run sender without stream");
            return;
        }
        Thread.currentThread().setName("Sender for " + m_Addr);
        Log.i(TAG, "Starting on " + m_Addr);
        Packet p = null;
        while(!m_Closed)
        {
            try {
                p = m_SendQueue.take();
            } catch (InterruptedException e) {
                Log.e(TAG, "Error taking from message queue - " + e );
            }
            if(p != null && p.Type != Packet.Header.FREE)
            {
                p.sendPacket(m_Stream);
                p = null;
            }
        }
        Log.i(TAG, "Shutting down");
    }
}
