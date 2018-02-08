/*
 * Copyright (c) 2018. $user
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.game.team9.slidingpuzzle.network;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.game.team9.slidingpuzzle.R;

import java.util.List;

/**
 * Created on: 2/7/18
 * Author: David Hiatt - dhiatt89@gmail.com
 */

public class PeerListAdapter extends ArrayAdapter<DeviceObject> {

    private final List<DeviceObject> m_Devices;

    public PeerListAdapter(Context context, int id, List<DeviceObject> devices) {
        super(context,id,devices);
        m_Devices = devices;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View v = convertView;

        if (v == null) {
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.row_device, null);
        }
        DeviceObject device = m_Devices.get(position);
        if (device != null) {
            TextView top = v.findViewById(R.id.device_name);
            TextView bottom = v.findViewById(R.id.device_details);
            if (top != null) {
                top.setText(device.DevName);
            }
            if (bottom != null) {
                bottom.setText(getDeviceStatus(device.Status));
            }
        }

        return v;
    }

    public static String getDeviceStatus(int deviceStatus) {
        Log.d("PEERLIST", "Peer status :" + deviceStatus);
        switch (deviceStatus) {
            case WifiP2pDevice.AVAILABLE:
                return "Available";
            case WifiP2pDevice.INVITED:
                return "Invited";
            case WifiP2pDevice.CONNECTED:
                return "Connected";
            case WifiP2pDevice.FAILED:
                return "Failed";
            case WifiP2pDevice.UNAVAILABLE:
                return "Unavailable";
            default:
                return "Unknown";

        }
    }
}
