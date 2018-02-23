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
import com.game.team9.slidingpuzzle.BaseMathActivity;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created on: 2/18/18
 * Author: David Hiatt - dhiatt89@gmail.com
 */

public class TestNetwork extends Thread implements NetworkReceiver.IDataInbound {

    private static final String TAG = "Tester";
    private boolean m_Closed = false;
    private Socket m_Socket;
    private boolean m_Started;

    public void Close(){m_Closed = true;
        MessageQueue.add(Packet.AcquirePacket("127.0.0.1", Packet.Header.FREE));
    synchronized (m_Lock)
    {
        m_Waiting = false;
        m_Lock.notifyAll();
    }
    }

    public final BlockingQueue<Packet> MessageQueue = new LinkedBlockingQueue<>();

    private FakeReceiver m_Faker;


    public boolean TestRequest = true;
    public boolean TestInit = true;
    public boolean TestTime = false;
    public boolean TestMove = false;
    public boolean TestQuit = false;
    public boolean FakeAll = true;

    private final Queue<Packet.Header> m_Expected = new LinkedBlockingQueue<>();
    private final Object m_Lock = new Object();
    private boolean m_Waiting;

    @Override
    public void run() {
        Thread.currentThread().setName("TestNetwork");
        Packet test = null;
        if(TestRequest)
        {



        m_Expected.add(Packet.Header.ACCEPT);
            MessageQueue.add(Packet.AcquirePacket("127.0.0.1", Packet.Header.USER, "Test".getBytes()));
        m_Waiting = true;
            MessageQueue.add(Packet.AcquirePacket("127.0.0.1", Packet.Header.REQUEST, (byte)1));
        }

        if(TestInit)
        {
            byte[] b = BaseMathActivity.getBoard();
            MessageQueue.add(Packet.AcquirePacket("127.0.0.1", Packet.Header.INIT, b));
        }


        m_Socket= null;
        OutputStream o = null;
        try {
            m_Socket = new Socket("localhost", AppController.getPort());
        } catch (IOException e) {
            Log.e(TAG, "Failed to fake socket - " + e);
        }
        if(FakeAll)
        {
            try {
                m_Faker = new FakeReceiver(this);
            } catch (IOException e) {
                e.printStackTrace();
            }
            m_Faker.start();
        }

        try {
             o = m_Socket.getOutputStream();
        } catch (IOException e) {
            Log.e(TAG, "Failed to fake output stream - " + e);
        }
        while(!m_Closed) {

            try {


                test = MessageQueue.take();
                if(test.Type!= Packet.Header.FREE)
                {
                    Log.i(TAG, "Sending " + test);
                    test.sendPacket(o);
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(test.Type == Packet.Header.REQUEST) {
                synchronized (m_Lock) {
                    while (m_Waiting) {
                        try {
                            Log.i(TAG, "Waiting");
                            m_Lock.wait();
                        } catch (InterruptedException e) {
                            Log.e(TAG, "Failed to fake wait on lock - " + e);
                        }
                    }
                }
            }
            Log.i(TAG, "Next round!");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        m_Timer.cancel();
        if(m_Faker != null)
        {
            m_Faker.Close();
        }
        if(o != null)
        {
            try {
                o.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(m_Socket != null)
            try {
                m_Socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.i(TAG,"Tester closed");

    }

    private final Timer m_Timer = new Timer();

    @Override
    public void onReceive(Packet p) {
        boolean start = m_Started;
        Log.i(TAG, "Received " + p);
        if(p.Type == Packet.Header.QUIT)
        {
            Close();
            return;
        }
        synchronized (m_Lock)
        {
            if(m_Waiting)
            {
                if(p.Type == m_Expected.remove())
                {
                    m_Started = true;
                }
                m_Waiting = false;
                m_Lock.notifyAll();
            }
        }
        if(start != m_Started && TestTime)
        {
            Log.i(TAG,"Starting timer");
            m_Timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    byte a = (byte)System.currentTimeMillis();
                    Log.i(TAG,"Adding time");
                    MessageQueue.add(Packet.AcquirePacket("127.0.0.1", Packet.Header.TIME, a));
                }
            },2000, 2000);
        }
    }

    private static class FakeReceiver extends NetworkReceiver
    {
        public FakeReceiver(TestNetwork instance) throws IOException {super("127.0.0.1", instance.m_Socket.getInputStream());
        m_Name = "Fake " + TAG + ": " + m_Addr;
        m_Queue = instance;}

    }
}
