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

    public static final String ACTION_SEND_TIME = "com.game.team9.slidingpuzzle.SEND_TIME";
    public static final String ACTION_SEND_MOVE = "com.game.team9.slidingpuzzle.SEND_MOVE";
    public static final String ACTION_SEND_QUIT = "com.game.team9.slidingpuzzle.SEND_QUIT";
    public static final String ACTION_SEND_REJECT = "com.game.team9.slidingpuzzle.REJECT";
    public static final String ACTION_SEND_ACCEPT = "com.game.team9.slidingpuzzle.ACCEPT";

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



    public static final int HEADER_REQUEST_SENT = 5001;
    public static final int HEADER_REQUEST_RECEIVED = 5002;
    public static final int HEADER_REQUEST_ACCEPTED = 5003;
    public static final int HEADER_REQUEST_REJECTED = 5004;

    public static final int HEADER_CLIENT_LOST = 50011;
    public static final int HEADER_CLIENT_TIME = 50012;
    public static final int HEADER_CLIENT_MOVE = 50013;
    public static final int HEADER_CLIENT_QUIT = 50014;

}
