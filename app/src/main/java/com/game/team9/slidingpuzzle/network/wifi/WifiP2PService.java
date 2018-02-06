/*
 * Copyright (c) 2018. $user
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.game.team9.slidingpuzzle.network.wifi;

import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.annotation.NonNull;

public class WifiP2PService extends Service {
    public WifiP2PService() {
    }

    public static final int START_MSG = 10;
    public static final int TIME_MSG = 11;
    public static final int MOVE_MSG = 12;
    public static final int SCORE_MSG = 13;
    public static final int QUIT_MSG = 14;

    private final Messenger m_Messenger = new Messenger(new InboundHandler());
    private final Object m_Lock = new Object();
    private boolean m_Bound = false;


    @Override
    public IBinder onBind(Intent intent) {
        return m_Messenger.getBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
    }

    class InboundHandler extends Handler
    {
        public InboundHandler(){
            super();
        }
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch(msg.what)
            {
                case START_MSG:
                    break;
                case TIME_MSG:
                    break;
                case MOVE_MSG:
                    break;
                case SCORE_MSG:
                    break;
                case QUIT_MSG:
                    break;
            }
            super.handleMessage(msg);
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
