/*
 * Copyright (c) 2018. $user
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.game.team9.slidingpuzzle.network.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.game.team9.slidingpuzzle.MathOnlineDiscoveryActivity;

/**
 * Created on: 1/31/18
 *     Author: David Hiatt - dhiatt89@gmail.com
 */

public class PeerBroadcastReceiver extends BroadcastReceiver implements WifiP2pManager.PeerListListener {


    private WifiP2pManager m_Manager;
    private MathOnlineDiscoveryActivity m_Activity;
    private WifiP2pManager.Channel m_Channel;

    public PeerBroadcastReceiver(final WifiP2pManager m, final WifiP2pManager.Channel c, MathOnlineDiscoveryActivity ac)
    {

        m_Manager = m;
        m_Activity = ac;
        m_Channel = c;
    }
   @Override
    public void onReceive(Context context, @NonNull Intent intent) {
        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            // Determine if Wifi P2P mode is enabled or not, alert
            // the Activity.
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            m_Activity.onWifiStatusChanged(state == WifiP2pManager.WIFI_P2P_STATE_ENABLED);

        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

           m_Manager.requestPeers(m_Channel, this);
            // The peer list has changed! We should probably do something about
            // that.

        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            if(m_Manager == null)
                return;
            NetworkInfo info = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            if(info.isConnected())
            {
                Log.d("WIFI", "Connected");
                m_Manager.requestConnectionInfo(m_Channel,(WifiP2pManager.ConnectionInfoListener) m_Activity);
            }
            // Connection state changed! We should probably do something about
            // that.

       } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            WifiP2pDevice dev = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
            Log.d("WIFI", "Device status - " + dev.status);
            m_Manager.requestConnectionInfo(m_Channel,(WifiP2pManager.ConnectionInfoListener) m_Activity);
           // DeviceListFragment fragment = (DeviceListFragment) m_Activity.getFragmentManager()
           //         .findFragmentById(R.id.frag_list);
           // fragment.updateThisDevice((WifiP2pDevice) intent.getParcelableExtra(
               //     WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));

        }else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

            // Request available peers from the wifi p2p manager. This is an
            // asynchronous call and the calling m_Activity is notified with a
            // callback on PeerListListener.onPeersAvailable()
            if (m_Manager != null) {
                //m_Manager.requestPeers(m_Channel, (DevListFragment)m_Activity.getFragmentManager().findFragmentById(R.id.frag_list));
            }
          //  Log.d(WiFiDirectActivity.TAG, "P2P peers changed");
        }
    }


    @Override
    public void onPeersAvailable(@NonNull WifiP2pDeviceList wifiP2pDeviceList) {
        for (WifiP2pDevice device : wifiP2pDeviceList.getDeviceList()) {

        }
    }
}
