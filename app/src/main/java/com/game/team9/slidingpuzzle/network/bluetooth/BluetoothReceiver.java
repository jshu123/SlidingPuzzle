/*
 * Copyright (c) 2018. $user
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.game.team9.slidingpuzzle.network.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

/**
 * Created on: 2/1/18
 * Author: David Hiatt - dhiatt89@gmail.com
 */

public class BluetoothReceiver extends BroadcastReceiver {
    public static final int REQUEST_ENABLE_BT = 10;
    private final IBluetoothRecDev m_Rev;

    public BluetoothReceiver(IBluetoothRecDev dev)
    {
        m_Rev = dev;
    }

    @Override
    public void onReceive(Context context, @NonNull Intent intent)
    {
        String action = intent.getAction();
        if (BluetoothDevice.ACTION_FOUND.equals(action))
        {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            String deviceName = device.getName();
            String deviceHardwareAddress = device.getAddress(); // MAC address
            if(m_Rev != null)
                m_Rev.BluetoothReceive(deviceName, deviceHardwareAddress);
        }
        else if (BluetoothDevice.ACTION_PAIRING_REQUEST.equals(action))
        {

        }
        else if (BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED.equals(action))
        {

        }
        else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
        {

        }
        else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action))
        {

        }
        else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action))
        {

        }
        else if (BluetoothAdapter.ACTION_SCAN_MODE_CHANGED.equals(action))
        {

        }
    }

    public interface IBluetoothRecDev
    {
        void BluetoothReceive(String dev, String add);
    }
}
