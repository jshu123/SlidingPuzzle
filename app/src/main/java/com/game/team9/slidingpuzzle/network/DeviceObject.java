/*
 * Copyright (c) 2018. $user
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.game.team9.slidingpuzzle.network;

import android.net.wifi.p2p.WifiP2pDevice;

import java.io.Serializable;

/**
 * Created on: 2/6/18
 * Author: David Hiatt - dhiatt89@gmail.com
 */

public class DeviceObject implements Serializable{
    public final int Port;
    public final String Ip;
    public final String Name;
    public final String OsVersion;
    public final String DevName;
    public final int Status;


    public DeviceObject(WifiP2pDevice d)
    {
        Status = d.status;
        Port = 0;
        Ip = d.deviceAddress;
        Name = d.deviceName;
        DevName = d.deviceName;
        OsVersion = d.primaryDeviceType;
    }
    public DeviceObject(int p, String i, String n, String os, String dev, int status)
    {
        Status = status;
     Port = p;
     Ip = i;
     Name = n;
     OsVersion = os;
     DevName = dev;
    }

    @Override
    public String toString() {
        String sbuf = "Device: " + DevName +
                "\n deviceAddress: " + Ip +
                "\n primary type: " + OsVersion +
                "\n secondary type: " + Name +
                "\n status: " + Status;
        return sbuf;
    }
}
