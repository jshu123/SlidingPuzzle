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
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import java.util.Collection;
import java.util.Iterator;

/**
 * Created on: 2/6/18
 * Author: David Hiatt - dhiatt89@gmail.com
 */

public class WifiBroadcastReceiver extends BroadcastReceiver {


    private WifiP2pManager m_Manager;
    private IWifiStatusChanger m_Activity;
    private WifiP2pManager.Channel m_Channel;
    private WifiP2pManager.PeerListListener m_PeerListener;
    private WifiP2pManager.GroupInfoListener m_GroupListener;
    private WifiP2pManager.ConnectionInfoListener m_ConListener;
    private WifiP2pDeviceList m_List;
    private final WifiP2pConfig m_Config = new WifiP2pConfig();

    private boolean m_Connected;


    public WifiBroadcastReceiver(final WifiP2pManager m, final WifiP2pManager.Channel c, final IWifiStatusChanger d)
    {
        super();
        m_Manager = m;
        m_Activity = d;
        m_Channel = c;
        m_GroupListener = new WifiP2pManager.GroupInfoListener() {
            @Override
            public void onGroupInfoAvailable(WifiP2pGroup group) {
                if(group != null) {
                    Log.d("getNetworkName", group.getNetworkName());
                    Log.d(".deviceAddress", group.getOwner().deviceAddress);
                    Log.d(".deviceName", group.getOwner().deviceName);
                }
            }
        };

        m_ConListener = new WifiP2pManager.ConnectionInfoListener() {
            @Override
            public void onConnectionInfoAvailable(WifiP2pInfo info) {
                if(info != null && info.groupOwnerAddress != null) {

                    Log.d(".toString", info.groupOwnerAddress.toString());
                    Log.d(".getHostAddress", info.groupOwnerAddress.getHostAddress());
                    Log.d(".getHostName", info.groupOwnerAddress.getHostName());
                    Log.d(".getCanonicalHostName", info.groupOwnerAddress.getCanonicalHostName());
                }
            }
        };

        m_PeerListener = new WifiP2pManager.PeerListListener() {
            @Override
            public void onPeersAvailable(WifiP2pDeviceList peers) {
                Collection<WifiP2pDevice> deviceList = peers.getDeviceList();
                Log.d("PEERS", "PeerListListener "+ deviceList.size());
                for (WifiP2pDevice current : deviceList) {
                    Log.d("DEVICE deviceAddress", current.deviceAddress);
                    Log.d("DEVICE deviceName", current.deviceName);
                    Log.d("primaryDeviceType", current.primaryDeviceType);

                    //connect
                    if (!m_Connected) {
                        m_Config.deviceAddress = current.deviceAddress;
                        m_Manager.connect(m_Channel, m_Config, new WifiP2pManager.ActionListener() {
                            @Override
                            public void onSuccess() {
                                Log.d("CONNECT", "SUCCESS");
                                m_Connected = true;
                            }

                            @Override
                            public void onFailure(int reason) {
                                Log.d("CONNECT", "FAIL");
                            }
                        });
                    }
                }
            }
        };
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            // Determine if Wifi P2P mode is enabled or not, alert
            // the Activity.
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            m_Activity.onWifiStatusChanged(state == WifiP2pManager.WIFI_P2P_STATE_ENABLED);

        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

            m_Manager.requestPeers(m_Channel, m_PeerListener);
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

    public interface IWifiStatusChanger
    {
        void onWifiStatusChanged(boolean state);
    }
}
