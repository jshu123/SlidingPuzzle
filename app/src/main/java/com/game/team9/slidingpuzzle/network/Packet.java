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

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created on: 2/18/18
 * Author: David Hiatt - dhiatt89@gmail.com
 */

    public class Packet {

        private static final String TAG = "Packet";
        private static final BlockingQueue<Packet> m_Pool = new LinkedBlockingQueue<>();

        public String Source;
        public Header Type = Header.FREE;
        public int Length;
        public final byte[] Data = new byte[256];

        private Packet(String from, Header t, byte... data){
            Source = from;
            Type = t;
            Length = data.length;
            System.arraycopy(data, 0, Data, 0, data.length);
            Log.i(TAG, "Created new packet for " + this);
        }

    private Packet(String from, Header t, int len, byte... data){
        Source = from;
        Type = t;
        Length = len;
        System.arraycopy(data, 0, Data, 0, len);
    }

    public static Packet AcquirePacket(String from, Header t, int len, byte[] data)
    {
        Packet p = m_Pool.poll();
        if(p == null)
            return new Packet(from, t, data);
        p.Type = t;
        p.Source = from;
        p.Length = len;
        System.arraycopy(data, 0, p.Data, 0, len);
        Log.i(TAG, "Reused new packet for " + p);
        return p;
    }

        public static Packet AcquirePacket(String from, Header t, byte... data)
        {
            Packet p = m_Pool.poll();
            if(p == null)
                return new Packet(from, t, data);
            p.Type = t;
            p.Source = from;
            p.Length = data.length;
            System.arraycopy(data, 0, p.Data, 0, data.length);
            Log.i(TAG, "Reused new packet for " + p);
            return p;
        }

    boolean sendPacket(OutputStream stream)
        {
            boolean result = false;
            if(Type == Header.FREE)
                Log.w(TAG, "Sending blank packet");
            try {
                stream.write(headerToByte(Type));

                stream.write((byte)Length);
                stream.write(Data, 0, Length);
            } catch (IOException e) {
                Log.e(TAG, "Error sending data - " + this + " - " + e);
                result = true;
            }
           Free();
            return result;
        }

        public void Free()
        {
            Length = 0;
            Type = Header.FREE;
            m_Pool.add(this);
        }

        private static byte headerToByte(Header h)
        {
            switch (h)
            {

                default:
                case FREE:return Constants.HEADER_NULL;
                case REQUEST:return Constants.HEADER_REQUEST;
                case ACCEPT:return Constants.HEADER_ACCEPT;
                case QUIT:return Constants.HEADER_CLIENT_QUIT;
                case MOVE:return Constants.HEADER_CLIENT_MOVE;
                case TIME:return Constants.HEADER_CLIENT_TIME;
                case INIT:return Constants.HEADER_INIT;
                case USER: return Constants.HEADER_USER;
            }
        }

        static Header intToHeader(int c)
        {
            switch(c)
            {
                case 7: return Header.USER;
                case 6:return Header.INIT;
                case 5:return Header.QUIT;
                case 4:return Header.MOVE;
                case 3:return Header.TIME;
                case 2:return Header.REQUEST;
                case 1:return Header.ACCEPT;
                default: return Header.FREE;
            }
        }
    public enum Header
    {
        FREE,
        REQUEST,
        ACCEPT,
        QUIT,
        MOVE,
        TIME,
        INIT,
        USER,
    }

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder(Type.toString());
        if(Length > 0)
        {
            ret.append(":").append(Length).append(":");
            for(int i = 0; i < Length; ++i)
            {
                ret.append(" ").append(Data[i]);
            }
        }
        return ret.toString();
    }
}

