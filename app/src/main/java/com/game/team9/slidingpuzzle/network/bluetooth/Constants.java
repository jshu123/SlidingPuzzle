/*
 * Copyright (c) 2018. $user
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.game.team9.slidingpuzzle.network.bluetooth;

/**
 * Created on: 2/1/18
 * Author: David Hiatt - dhiatt89@gmail.com
 */

public class Constants {
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;




    public static final int REQUEST_ENABLE_BT = 1;
    public static final int REQUEST_CONNECT_DEV = 2;
    public static final int REQUEST_CONNECT_DEV_INSECURE = 3;

    public static final int STATE_UNSUPPORTED = 0;
    public static final int STATE_NONE = 1;       // we're doing nothing
    public static final int STATE_LISTEN = 2;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 4; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 8;  // now connected to a remote device
    public static final int STATE_DISABLED = 16;
    public static final int STATE_DISCOVERING = 32;
    public static final int STATE_DISCOVERABLE = 256;
}
