/*
 * Copyright (c) 2018. $user
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.game.team9.slidingpuzzle;

import android.app.AlertDialog;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;

import com.game.team9.slidingpuzzle.network.ConListener;
import com.game.team9.slidingpuzzle.network.Constants;
import com.game.team9.slidingpuzzle.network.DataHandler;
import com.game.team9.slidingpuzzle.network.IOnlineGameManager;
import com.game.team9.slidingpuzzle.network.MathModeService;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Created on: 2/6/18
 * Author: David Hiatt - dhiatt89@gmail.com
 */

public class AppController extends Application {

    private static final int PORT = 49152;
    private boolean m_Running;
    private static final DataHandler m_Handler = new DataHandler();
    private static final ConListener m_Connection = new ConListener(PORT, m_Handler);
    private static final IntentFilter m_Filter = new IntentFilter();

    private BroadcastReceiver m_Receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            android.support.v7.app.AlertDialog alertDialog = new android.support.v7.app.AlertDialog.Builder(context).create();
            alertDialog.setTitle("New play request");
            alertDialog.setMessage("You have been challenged by another player!");
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Reject", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(context, MathModeService.class);
                    intent.setAction(Constants.ACTION_SEND_REJECT);
                    dialog.dismiss();

                }
            });
            alertDialog.setButton(android.support.v7.app.AlertDialog.BUTTON_POSITIVE, "Accept",
                    new DialogInterface.OnClickListener() {
                        public void onClick(@NonNull DialogInterface dialog, int which) {
                            Intent intent = new Intent(context, MathModeService.class);
                            intent.setAction(Constants.ACTION_SEND_ACCEPT);
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
        }
    };

    static {
        m_Filter.addAction(Constants.PLAY_REQUEST_RECEIVED);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        int m_Port = findPort();
        registerReceiver(m_Receiver, m_Filter);
    }


    private static int findPort()
    {
        int p = -1;
        try {
            ServerSocket s = new ServerSocket(0);
            p = s.getLocalPort();
            if(!s.isClosed())
                s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return p;
    }

    public void setHandler(IOnlineGameManager m)
    {
        m_Handler.setManager(m);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        unregisterReceiver(m_Receiver);
        if(m_Connection.isAlive())
        {
            m_Connection.Close();
            try {
                m_Connection.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
