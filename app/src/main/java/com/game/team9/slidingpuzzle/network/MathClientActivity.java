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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicBoolean;

public class MathClientActivity extends AppCompatActivity {

    private static final int INFO_NEW = 1;
    private static final int INFO_UPDATE = 2;
    private static final int RESPONSE_NEW = 3;
    private static final int RESPONSE_CLEAR = 4;
    private static final int RESPONSE_UPDATE = 5;
    private static final int SENDING = 6;

    private Socket m_Socket;
    private String m_ServerIP = "192.168.2.3";
    private int m_Port = 5000;
    private TextView m_ToSend;
    private TextView m_Response;
    private TextView m_OverPort;
    private TextView m_OverIP;
    private TextView m_Info;
    private ConnThread m_Thread;

    private void ResetSocket(String ip, int p)
    {
            if(m_Thread != null && m_Thread.Alive())
                m_Thread.Close();
            m_Port = p;
            m_ServerIP = ip;
            m_OverPort.setText("");
            m_OverIP.setText("");
            m_Thread = new ConnThread(ip,p);
            m_Thread.start();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_math_generic_server);
        m_OverIP = findViewById(R.id.overIpText);
        m_OverPort = findViewById(R.id.overPortText);
        m_Info = findViewById(R.id.infoText);
        m_Response = findViewById(R.id.textResponse);
        m_ToSend = findViewById(R.id.testText);
        ResetSocket(m_ServerIP, m_Port);

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String sport = m_OverPort.getText().toString();
                int port = m_Port;
                if(sport.length() > 0)
                    port = Integer.parseInt(sport);
                String ip = m_OverIP.getText().toString();
                if(ip.length() < 1)
                    ip = m_ServerIP;
                if(port > 0 && (port != m_Port || !ip.equals(m_ServerIP)))
                {
                    ResetSocket(ip, port);
                }
                else {
                    if (m_Thread != null && m_Thread.Alive())
                        m_Thread.Send();
                }

            }
        });
    }

    private Handler m_Handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what)
            {
                case RESPONSE_CLEAR: m_Response.setText("");
                break;
                case RESPONSE_NEW:m_Response.setText((String)msg.obj);
                break;
                case INFO_NEW:
                    if(msg.arg2 == 0)
                        m_Info.setText("Connecting to " + msg.obj + ":" + msg.arg1) ;
                    else
                        m_Info.setText("Disconnected from " + msg.obj + ":" + msg.arg1) ;
                break;
                case INFO_UPDATE:m_Info.setText(m_Info.getText() + "\n" + msg.obj);
                break;
                case RESPONSE_UPDATE:
                    m_Response.setText(m_Response.getText() + msg.toString());
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        m_Thread.Close();
    }


    class ConnThread extends Thread
    {
        private boolean mm_Close;
        private final String mm_IP;
        private final int mm_Port;
        private final AtomicBoolean mm_Send = new AtomicBoolean(false);
        public boolean Alive(){return !mm_Close; }
        public void Close(){
            mm_Close = true;}

        public void Send()
        {
            mm_Send.set(true);
        }
        public ConnThread(String ip, int p)
        {
            mm_IP = ip;
            mm_Port = p;
        }
        @Override
        public void run() {

            Log.i("ConnThread", "Start with " + mm_IP);
            Message msg = new Message();
            msg.what = INFO_NEW;
            msg.arg1 = mm_Port;
            msg.arg2 = 0;
            msg.obj = mm_IP;
            m_Handler.sendMessage(msg);
            msg = new Message();

            msg.what = INFO_UPDATE;
                try {
                    InetAddress serverAddr = InetAddress.getByName(mm_IP);
                    m_Socket = new Socket(serverAddr, m_Port);
                } catch (UnknownHostException e1) {
                    e1.printStackTrace();
                    msg.obj = e1.getMessage();
                   m_Handler.sendMessage(msg);
                    msg = new Message();
                    mm_Close = true;
                } catch (IOException e1) {
                    e1.printStackTrace();
                    msg.obj = e1.getMessage();
                    m_Handler.sendMessage(msg);
                    msg = new Message();
                   mm_Close = true;
                }
            msg.what = INFO_UPDATE;
            Log.i("ConnThread", "Looping with " + mm_IP);
            while(!mm_Close)
            {
                    while(!mm_Close && !mm_Send.compareAndSet(true, false)){
                        try {
                            mm_Send.wait(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    if(!mm_Close)
                    {

                        msg.what = SENDING;
                        String m = m_ToSend.getText().toString();
                        msg.obj = m;
                        m_Handler.sendMessage(msg);
                        msg = new Message();
                        PrintWriter out = null;
                        try {
                            out = new PrintWriter(new BufferedWriter(
                                    new OutputStreamWriter(m_Socket.getOutputStream())),
                                    true);
                            out.println(m);
                        } catch (IOException e) {
                            e.printStackTrace();
                            msg.obj = e.getMessage();
                            msg.what = INFO_UPDATE;
                            m_Handler.sendMessage(msg);
                            msg = new Message();
                        }

                        Log.d("Client", "Client sent message");
                    }
            }
            msg.what = INFO_NEW;
            msg.arg1 = mm_Port;
            msg.arg2 = 1;
            msg.obj = mm_IP;
            m_Handler.sendMessage(msg);
            Log.i("ConnThread", "Exit from " + mm_IP);
        }
    }
}
