/*
 * Copyright (c) 2018. $user
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.game.team9.slidingpuzzle.network;

/**
 * Created on: 2/6/18
 * Author: David Hiatt - dhiatt89@gmail.com
 */

public class Constants {

    public static final int DEFAULT_PORT = 49152;
    public static final int SOCKET_TIMEOUT = 5000;

    public static final String ACTION = "ACTION_TYPE";

    public static final String ACTION_INIT = "INIT";

    public static final String ACTION_DISCOVER = "com.game.team9.slidingpuzzle.DISCOVER";
    public static final String ACTION_ATTACH_HANDLE = "com.game.team9.slidingpuzzle.ATTACH_HANDLE";
    public static final String ACTION_SEND_TIME = "com.game.team9.slidingpuzzle.SEND_TIME";
    public static final String ACTION_SEND_MOVE = "com.game.team9.slidingpuzzle.SEND_MOVE";
    public static final String ACTION_SEND_QUIT = "com.game.team9.slidingpuzzle.SEND_QUIT";
    public static final String ACTION_SEND_REJECT = "com.game.team9.slidingpuzzle.REJECT";
    public static final String ACTION_SEND_ACCEPT = "com.game.team9.slidingpuzzle.ACCEPT";

    public static final String ACTION_NAME_CHANGE = "com.game.team9.slidingpuzzle.NAMECHANGE";
    public static final String ACTION_BLUETOOTH_STOP = "com.game.team9.slidingpuzzle.BLUESTOP";
    public static final String ACTION_BLUETOOTH_CON = "com.game.team9.slidingpuzzle.BLUECON";
    public static final String ACTION_WIFI_CON = "com.game.team9.slidingpuzzle.WIFICON";
    public static final String ACTION_NEW_PEER = "com.game.team9.slidingpuzzle.NEWPEER";
    public static final String ACTION_PEER_CLICK = "com.game.team9.slidingpuzzle.PEER_CLICK";
    public static final String ACTION_BLUE_DISCOVER = "com.game.team9.slidingpuzzle.BLUE_DISCOVER";
    public static final String ACTION_BLUE_CANCEL_DISCOVER = "com.game.team9.slidingpuzzle.BLUE_CANCEL_DISCOVER";
    public static final String ACTION_BLUE_DISABLED = "com.game.team9.slidingpuzzle.BLUE_DISABLED";
    public static final String ACTION_BLUE_UNSUPPORTED = "com.game.team9.slidingpuzzle.BLUE_UNSUPPORTED";
    public static final String ACTION_WIFI_UNSUPPORTED = "com.game.team9.slidingpuzzle.WIFI_UNSUPPORTED";

    public static final String DEVICE_LIST_CHANGED = "device_list_updated";

    public static final String PLAY_REQUEST_RECEIVED = "play_request_received";
    public static final String PLAY_RESPONSE_RECEIVED = "play_response_received";

    public static final String EXTRA_OWNER_ADDRESS = "OWNER_ADDRESS";
    public static final String EXTRA_OWNER_PORT = "OWNER_PORT";
    public static final String EXTRA_TIME = "TIME";
    public static final String EXTRA_MOVEA = "MOVEA";
    public static final String EXTRA_MOVEB = "MOVEB";
    public static final String EXTRA_MOVEOP = "MOVEOP";
    public static final String EXTRA_MOVEEQ = "MOVEEQ";
    public static final String EXTRA_HANDLER = "HANDLER";
    public static final String EXTRA_IS_HOST = "IS_HOST";
    public static final String EXTRA_ROUNDS = "ROUNDS";
    public static final String EXTRA_BOARD = "BOARD";
    public static final String EXTRA_DEVICE = "DEVICE";
    public static final String EXTRA_ID = "ID";
    public static final String EXTRA_CUT = "CUTTHROAT";
    public static final String EXTRA_MODE = "MODE";
    public static final String EXTRA_REASON = "REASON";


    public static final String PREF = "MathModeName";
    public static final String PREF_USER = "name";
    public static final String PREF_LAST_MODE = "MathLastMode";
    public static final String PREF_LAST_GAME = "MathLastGame";
    public static final String PREF_LAST_ONLINE_MODE = "MathLastOnlineMode";
    public static final String PREF_LAST_ROUNDS = "MathLastRound";


    public static final int HEADER_NULL = 0;
    public static final int HEADER_ACCEPT = 1;
    public static final int HEADER_REQUEST = 2;
    public static final int HEADER_CLIENT_TIME = 3;
    public static final int HEADER_CLIENT_MOVE = 4;
    public static final int HEADER_CLIENT_QUIT = 5;
    public static final int HEADER_INIT = 6;
    public static final int HEADER_USER = 7;

    public static final int HEADER_REQUEST_SENT = 5001;
    public static final int HEADER_REQUEST_RECEIVED = 5002;
    public static final int HEADER_REQUEST_ACCEPTED = 5003;
    public static final int HEADER_REQUEST_REJECTED = 5004;

    public static final int HEADER_CLIENT_LOST = 50011;


    public final static int TYPE_WIFI = 0;
    public final static int TYPE_BLUE = 1;
    public final static int TYPE_NSD = 2;

    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final int MESSAGE_DEVLIST = 6;
    public static final int MESSAGE_NET_STATUS = 7;
    public static final int MESSAGE_INC_REQ = 8;
    public static final int MESSAGE_REQ_RESP = 9;
    public static final int MESSAGE_SERVLIST = 10;



    public static final int REQUEST_ENABLE_BT = 1;
    public static final int REQUEST_DISCOVERABLE = 2;
    public static final int REQUEST_CONNECT_DEV_INSECURE = 3;

    public static final int STATE_UNSUPPORTED = 0;
    public static final int STATE_NONE = 1;       // we're doing nothing
    public static final int STATE_LISTEN = 2;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 4; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 8;  // now connected to a remote device
    public static final int STATE_DISABLED = 16;
    public static final int STATE_DISCOVERING = 32;
    public static final int STATE_DISCOVERABLE = 256;


    public static final int PRIORITY_NETHANDLER = 0;
    public static final int PRIORITY_DISCOVER_ACT = 1;
    public static final int PRIORITY_BASEONLINE = 2;

}
