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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created on: 2/6/18
 * Author: David Hiatt - dhiatt89@gmail.com
 */

public class DataHandler implements  IDataReceiver {

    private IOnlineGameManager m_Manager;
    private final List<IChangeNotifier> m_Subscribers = new ArrayList<>();

    public void setManager(IOnlineGameManager m)
    {
        m_Manager = m;
        for (IChangeNotifier subscriber : m_Subscribers) {
            subscriber.onChange(m!=null);
        }
    }

    @Override
    public boolean isActive() {
        return m_Manager != null;
    }

    @Override
    public void inboundData(String ip, InputStream stream) {
        if(m_Manager == null)
            return;
        byte[] buffer = new byte[5];
        try {
            stream.read(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int header = (buffer[0]) | (buffer[1] << 7) | (buffer[2] << 14) | (buffer[3] << 21) | (buffer[4] << 28);

        switch(header)
        {
            case Constants.HEADER_CLIENT_MOVE:
                byte a, b, op, eq;
                a = b= op = eq = -1;
                try {
                    a = (byte)stream.read();
                    b = (byte)stream.read();
                    op = (byte)stream.read();
                    eq = (byte)stream.read();
                }
                catch(IOException e)
                {
                    e.printStackTrace();
                }
                m_Manager.receiveMove(a,b,op,eq);
                break;
            case Constants.HEADER_CLIENT_QUIT:
                m_Manager.receiveQuit();
                break;
            case Constants.HEADER_CLIENT_TIME:
                byte[] bytes = null;
                try {
                    byte len = (byte)stream.read();
                    bytes = new byte[len];
                    for(int i = 0; i < len; ++i)
                        bytes[i] = (byte)stream.read();
                } catch (IOException e) {
                    e.printStackTrace();
                }finally
                {
                    if(bytes != null)
                        m_Manager.receiveTime(new String(bytes));
                }
                break;
        }
    }

    @Override
    public void attachNotifier(IChangeNotifier c) {
        m_Subscribers.add(c);
    }
}
