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
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.Chronometer;

import com.game.team9.slidingpuzzle.network.bluetooth.Constants;

public abstract class BaseMathOnlineActivity extends AppCompatActivity implements Chronometer.OnChronometerTickListener {

    private static final byte HEADER_TIME = 1;
    private static final byte HEADER_MOVE = 2;
    private static final byte HEADER_QUIT = 3;
    private static final byte HEADER_SCORE = 4;

    private final Object m_Lock = new Object();
    private final BluetoothHandler m_Handler = new BluetoothHandler();
    private boolean m_Server;
    private Chronometer m_Timer;

    @NonNull
    private byte[] m_Buffer = new byte[256];

    private Handler m_Old;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        m_Old = MathOnlineDiscoveryActivity.m_Service.transferHandle(m_Handler);
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
        for (int i = 0; i < bytes.length; i++) {
            m_Buffer[i+2] = bytes[i];
        }
       // MathOnlineDiscoveryActivity.s_BlueMan.Write(m_Buffer, bytes.length + 2);
    }

    protected void onMove()
    {

    }

    private void DecodeData(@NonNull byte[] buf, int len)
    {
        if(buf.length > 0)
        {
            switch(buf[0])
            {
                case HEADER_TIME:
                    if(!m_Server)
                    {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                m_Timer.setText(new String(buf,1, len - 1));
                            }
                        });
                    }
                    break;
                case HEADER_MOVE: onMove();
                break;
                case HEADER_QUIT:
                case HEADER_SCORE:
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
       // MathOnlineDiscoveryActivity.s_BlueMan.transferHandle(m_Old);
    }

    private class BluetoothHandler extends Handler
    {
        private BluetoothHandler(){}
        @Override
        public void handleMessage(@NonNull Message msg) {

            switch (msg.what)
            {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch(msg.arg1)
                    {
                        case Constants.STATE_CONNECTED:
                        case Constants.STATE_CONNECTING:
                        case Constants.STATE_LISTEN:
                        case Constants.STATE_NONE:
                    }
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                case Constants.MESSAGE_READ:
                    DecodeData((byte[])msg.obj, msg.arg1);
                    break;
                case Constants.MESSAGE_TOAST:
                case Constants.MESSAGE_WRITE:
            }
        }

        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);
        }

        @Override
        public boolean sendMessageAtTime(Message msg, long uptimeMillis) {
            return super.sendMessageAtTime(msg, uptimeMillis);
        }
    }
}
