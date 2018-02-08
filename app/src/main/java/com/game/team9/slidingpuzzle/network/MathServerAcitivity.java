/*
 * Copyright (c) 2018. $user
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.game.team9.slidingpuzzle.network;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.game.team9.slidingpuzzle.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class MathServerAcitivity extends AppCompatActivity {

    private static final int INFO_NEW = 1;
    private static final int INFO_UPDATE = 2;
    private static final int SUCCESS = 11111;

    private ServerSocket m_SSocket;
    private ConnThread m_ConnThread;
    private int m_Port = 5000;
    private String m_TestMsg;
    private TextView m_Status;
    private TextView m_Test;
    private TextView m_PortText;
    private Handler m_Handler = new Handler(){
        @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case SUCCESS:
                        m_Test.setText("Update: " + msg.obj);
                        break;
                    case INFO_UPDATE: m_Status.setText(m_Status.getText() + "\n" + msg.obj);
                        break;
                    case INFO_NEW:
                        m_Status.setText(msg.obj + " on " + msg.arg1);
                        break;
                }
                super.handleMessage(msg);
            }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_math_server_acitivity);
        m_PortText = findViewById(R.id.portText);

        m_Status= findViewById(R.id.infoText);
        m_Test = findViewById(R.id.testText);
        m_Status.setText("IP: ");
        m_ConnThread = new ConnThread(m_Port);
        m_ConnThread.start();

    }

    public void reset_OnClick(View view)
    {
        String p = m_PortText.getText().toString();
        if(p != null && p.length() > 0)
            m_Port = Integer.parseInt(p);
        if(m_ConnThread != null)
            m_ConnThread.Close();
        m_ConnThread = new ConnThread(m_Port);
        m_ConnThread.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        m_ConnThread.Close();
        if(m_SSocket != null && m_SSocket.isBound())
        try
        {
            m_SSocket.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }


    class ConnThread  extends Thread
    {

        private boolean mm_Closed;
        private final int mm_Port;
        public ConnThread(int p)
        {
            mm_Port=p;
        }
        public void Close()
        {
            mm_Closed = true;
        }
        @Override
        public void run() {
            Log.i("ServerConn", "Start");
            Socket s = null;
            Message m = new Message();
            m.what = INFO_NEW;
            m.arg1 = mm_Port;
            m.obj = "Starting";
            m_Handler.sendMessage(m);
            m = new Message();
            try {
                m_SSocket = new ServerSocket(mm_Port );
            } catch (IOException e) {
                e.printStackTrace();
                m.what = INFO_UPDATE;
                m.obj = e.getMessage();
                m_Handler.sendMessage(m);
                m = new Message();

            }
            m.what = INFO_UPDATE;
            m.obj = new String("Waiting on " + m_SSocket.getInetAddress().getHostName() + ":" + mm_Port);
            m_Handler.sendMessage(m);
            m = new Message();
            while (!mm_Closed) {



                try {
                    if (s == null)
                        s = m_SSocket.accept();
                    BufferedReader input = new BufferedReader(
                            new InputStreamReader(s.getInputStream()));
                    m.obj = input.readLine();
                    m.what = SUCCESS;
                    m_Handler.sendMessage(m);
                    m = new Message();
                }
                catch(IllegalStateException e) {
                    e.printStackTrace();
                }
                catch (IOException e) {
                    e.printStackTrace();
                    m.what = INFO_UPDATE;
                    m.obj = e.getMessage();
                    m_Handler.sendMessage(m);
                    m = new Message();
                }
            }
            m.what = INFO_NEW;
            m.arg1 = mm_Port;
            m.obj = "Closed";
            m_Handler.sendMessage(m);
            Log.i("ServerConn", "Exit");
        }
    }
}
