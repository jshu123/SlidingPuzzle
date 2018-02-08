/*
 * Copyright (c) 2018. $user
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.game.team9.slidingpuzzle.network;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;

import static com.game.team9.slidingpuzzle.network.Constants.ACTION_SEND_ACCEPT;
import static com.game.team9.slidingpuzzle.network.Constants.ACTION_SEND_MOVE;
import static com.game.team9.slidingpuzzle.network.Constants.ACTION_SEND_QUIT;
import static com.game.team9.slidingpuzzle.network.Constants.ACTION_SEND_REJECT;
import static com.game.team9.slidingpuzzle.network.Constants.ACTION_SEND_TIME;
import static com.game.team9.slidingpuzzle.network.Constants.EXTRA_MOVEA;
import static com.game.team9.slidingpuzzle.network.Constants.EXTRA_OWNER_ADDRESS;
import static com.game.team9.slidingpuzzle.network.Constants.EXTRA_OWNER_PORT;
import static com.game.team9.slidingpuzzle.network.Constants.EXTRA_TIME;
import static com.game.team9.slidingpuzzle.network.Constants.SOCKET_TIMEOUT;

public class MathModeService extends IntentService {
    public MathModeService() {
        super("MathModeService");
    }


    public static final byte START_MSG = 10;
    public static final byte TIME_MSG = 11;
    public static final byte MOVE_MSG = 12;
    public static final byte QUIT_MSG = 13;

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String host = intent.getStringExtra(EXTRA_OWNER_ADDRESS);
        int port = intent.getIntExtra(EXTRA_OWNER_PORT, -1);
        Socket s = new Socket();
        OutputStream stream = null;
        try {
            s.bind(null);
            s.connect(new InetSocketAddress(host, port), SOCKET_TIMEOUT);
            stream = s.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(stream != null) {
            switch (intent.getAction()) {
                case ACTION_SEND_TIME:
                    {
                    String time = intent.getStringExtra(EXTRA_TIME);
                    byte[] b = time.getBytes();
                    byte[] c = ByteBuffer.allocate(4).putInt(b.length).array();
                    try {
                        stream.write(TIME_MSG);
                        stream.write(c);
                        stream.write(b);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                    break;
                case ACTION_SEND_MOVE:
                    {
                    byte a = intent.getByteExtra(EXTRA_MOVEA, (byte) -1);
                    byte b = intent.getByteExtra(EXTRA_MOVEA, (byte) -1);
                    byte op = intent.getByteExtra(EXTRA_MOVEA, (byte) -1);
                    byte eq = intent.getByteExtra(EXTRA_MOVEA, (byte) -1);
                        try {
                            stream.write(MOVE_MSG);
                            stream.write(a);
                            stream.write(b);
                            stream.write(op);
                            stream.write(eq);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case ACTION_SEND_ACCEPT:
                    try {
                        stream.write(START_MSG);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case ACTION_SEND_REJECT:
                case ACTION_SEND_QUIT:
                    try {
                        stream.write(QUIT_MSG);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

            }
        }
        if(s != null && s.isConnected())
        {
            try {
                s.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
