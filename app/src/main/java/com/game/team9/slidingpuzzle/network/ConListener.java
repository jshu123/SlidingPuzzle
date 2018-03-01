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
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created on: 2/6/18
 * Author: David Hiatt - dhiatt89@gmail.com
 */

//A connection to this server indicates a request to play a game.
public class ConListener extends Thread {
    private static final String TAG = "Server";
    private int m_Port;
    private ServerSocket m_Server;

    private boolean m_Crash = false;

    public ConListener(int p) {
        Log.i(TAG, "Started on port " + p);
        m_Port = p;
    }

    public void Close()
    {
        m_Crash = true;
        Log.i(TAG, "Closing ");
    }


    @Override
    public void run() {
        Log.i(TAG, "Starting");
        try {
            m_Server = new ServerSocket(m_Port);
        } catch (IOException e) {
            Log.e(TAG, "Error opening server socket - " + e);
        }

        if(m_Server != null && !m_Server.isBound()) {
            try {
                m_Server.bind(new InetSocketAddress(m_Port));
            } catch (IOException e) {
                m_Crash = true;
                Log.e(TAG, "Error binding server socket - " + e);
            }
        }
        if(m_Server  != null) {
            Socket s = null;
            while (!m_Crash) {
                try {
                    s = m_Server.accept();
                    Log.i(TAG, "Inbound request! - " + s);

                    AppController.bindSocket(s);  //Receiver should either close socket or use it for a game.
                } catch (IOException e) {
                    Log.e(TAG, "Error accepting client data - " + e);
                    if(s != null)
                    {
                        try {
                            s.close();
                        } catch (IOException e1) {
                            Log.e(TAG, "Faield to close bad socket - " + e1);
                        }
                    }
                }

            }
            try {
                m_Server.close();
            } catch (IOException e) {
                Log.e(TAG, "Failed to close server - " + e);
            }
        }

        Log.i(TAG, "Closed");
    }
}
