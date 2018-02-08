/*
 * Copyright (c) 2018. $user
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.game.team9.slidingpuzzle.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created on: 2/6/18
 * Author: David Hiatt - dhiatt89@gmail.com
 */

public class ConListener extends Thread implements IDataReceiver.IChangeNotifier {
    private int m_Port;
    private ServerSocket m_Server;
    private final IDataReceiver m_Receiver;
    private final Object m_Lock = new Object();

    private boolean m_Crash = false;

    public ConListener(int p, IDataReceiver r) {
        m_Port = p;
        m_Receiver = r;
        r.attachNotifier(this);
    }


    public void onChange(boolean s)
    {
        synchronized (m_Lock)
        {
            m_Lock.notifyAll();
        }
    }

    public void Close()
    {
        m_Crash = true;
        synchronized (m_Lock)
        {
            m_Lock.notifyAll();
        }
    }

    @Override
    public void run() {
        try {
            m_Server = new ServerSocket(m_Port);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(m_Server != null && !m_Server.isBound()) {
            try {
                m_Server.bind(new InetSocketAddress(m_Port));
            } catch (IOException e) {
                m_Crash = true;
                e.printStackTrace();
            }
        }

        Socket s = null;
        while(!m_Crash)
        {
            s = null;
            synchronized (m_Lock) {
                while (!m_Crash && !m_Receiver.isActive()) {
                    try {
                        m_Lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (!m_Crash) {
                    try {
                        s = m_Server.accept();
                        m_Receiver.inboundData(s.getInetAddress().getHostAddress(), s.getInputStream());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    finally {
                        if(s != null) {
                            try {
                                s.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
    }
}
