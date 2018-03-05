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

import com.game.team9.slidingpuzzle.AppController;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created on: 2/18/18
 * Author: David Hiatt - dhiatt89@gmail.com
 */

public class NetworkReceiver extends Thread {
    protected static final String TAG = "NetworkReceiver";

    private InputStream m_Stream;
    protected final String m_Addr;
    protected IDataInbound m_Queue;
    protected String m_Name;

    private boolean m_Closed;

    NetworkReceiver(String name, InputStream i)
    {
        m_Addr = name;
        m_Name = TAG + ": " + m_Addr;
        m_Stream = i;

        m_Queue = AppController.getInstance();
    }

    void Close(){m_Closed = true;}

    @Override
    public void run() {

        Thread.currentThread().setName("Listener for " + m_Addr);
        Log.i(m_Name, "Started listener for " + m_Addr);
        byte[] buffer = new byte[256];
        while(!m_Closed) {

            int header= -1;
            int length = -1;
            try {
                header =  m_Stream.read();
            } catch (IOException e) {
                Log.e(m_Name, "Error reading 1 byte header - " + e);
            }

            Packet.Header h = Packet.intToHeader(header);
            if(h == Packet.Header.FREE)
            {
                m_Queue.onReceive(Packet.AcquirePacket(m_Addr, Packet.Header.QUIT));
                m_Closed = true;
            }

            Log.i(m_Name, "Header - " + header + " (" + h + ")" );
            if(m_Closed)
                break;
            int sanity = -1;
            try {
                length = m_Stream.read();
            } catch (IOException e) {
                Log.e(TAG, "Error reading 1 byte length - " + e);
            }
            if(m_Closed)
                break;
            try {
                sanity = m_Stream.read(buffer,0,length);
            } catch (IOException e) {
                Log.e(m_Name, "Error reading " + length + " bytes, only found " + sanity + " - " + e);
            }
            if(m_Closed)
                break;
            m_Queue.onReceive(Packet.AcquirePacket(m_Addr, h, length, buffer));
        }
        try {
            m_Stream.close();
        } catch (IOException e) {
            Log.e(TAG, "Failed to close input stream", e);
        }
        finally {
            Log.i(m_Name, "Closed listener for " + m_Addr);
            AppController.RemoveNetwork(m_Addr);
        }
    }

    public interface IDataInbound
    {
        void onReceive(Packet p);
    }
}
