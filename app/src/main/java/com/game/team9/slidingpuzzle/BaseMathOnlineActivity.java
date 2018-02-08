/*
 * Copyright (c) 2018. $user
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.game.team9.slidingpuzzle;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.Chronometer;

import com.game.team9.slidingpuzzle.network.IOnlineGameManager;

public abstract class BaseMathOnlineActivity extends AppCompatActivity implements Chronometer.OnChronometerTickListener {

    private static final byte HEADER_TIME = 1;
    private static final byte HEADER_MOVE = 2;
    private static final byte HEADER_QUIT = 3;
    private static final byte HEADER_SCORE = 4;

    private final Object m_Lock = new Object();
    private NetManager m_Manager;
    private boolean m_Server;
    private Chronometer m_Timer;

    @NonNull
    private byte[] m_Buffer = new byte[256];

    private Handler m_Old;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        m_Manager =  new NetManager(this);
//        m_Old = MathOnlineDiscoveryActivity.m_Service.transferHandle(m_Handler);
        AppController app = (AppController)getApplicationContext();
        app.setHandler(m_Manager);
        Intent intent = getIntent();
        m_Timer = findViewById(R.id.chronometer);
        m_Server = intent.getBooleanExtra("Server", false);
        if(m_Server)
        {

        }
        else
        {
            m_Timer.start();
            m_Timer.setOnChronometerTickListener(this);
        }
    }

    public void onChronometerTick(Chronometer var)
    {

        m_Buffer[0] = 1;
        byte[] bytes = var.getText().toString().getBytes();
        m_Buffer[1] = (byte)bytes.length;
        System.arraycopy(bytes, 0, m_Buffer, 2, bytes.length);
       // MathOnlineDiscoveryActivity.s_BlueMan.Write(m_Buffer, bytes.length + 2);
    }

    protected void onMove(byte a, byte b, byte op, byte eq)
    {

    }

    private void setTime(String time)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                m_Timer.setText(time);
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
       // MathOnlineDiscoveryActivity.s_BlueMan.transferHandle(m_Old);
    }

    private static class NetManager implements IOnlineGameManager
    {
        private final BaseMathOnlineActivity m_Base;
        public NetManager(BaseMathOnlineActivity b)
        {
            m_Base = b;
        }

        @Override
        public void receiveTime(String t) {
            m_Base.setTime(t);
        }

        @Override
        public void receiveMove(byte a, byte b, byte op, byte eq) {
            m_Base.onMove(a,b,op,eq);
        }

        @Override
        public void receiveQuit() {

        }

        @Override
        public void receiveRematchRequest() {

        }

        @Override
        public void receiveRematchResponse(boolean b) {

        }
    }
}
