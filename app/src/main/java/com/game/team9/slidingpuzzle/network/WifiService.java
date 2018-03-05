/*
 * Copyright (c) 2018. $user
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.game.team9.slidingpuzzle.network;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.net.wifi.p2p.nsd.WifiP2pServiceInfo;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.game.team9.slidingpuzzle.AppController;
import com.game.team9.slidingpuzzle.MathOnlineDiscoveryActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static android.net.wifi.p2p.WifiP2pManager.EXTRA_WIFI_P2P_INFO;
import static android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_STATE_DISABLED;
import static android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_STATE_ENABLED;
import static com.game.team9.slidingpuzzle.network.Constants.ACTION_WIFI_CON;
import static com.game.team9.slidingpuzzle.network.Constants.ACTION_WIFI_UNSUPPORTED;
import static com.game.team9.slidingpuzzle.network.Constants.EXTRA_DEVICE;
import static com.game.team9.slidingpuzzle.network.Constants.EXTRA_REASON;
import static com.game.team9.slidingpuzzle.network.Constants.PREF;
import static com.game.team9.slidingpuzzle.network.Constants.PREF_USER;

/**
 * Created on: 2/19/18
 * Author: David Hiatt - dhiatt89@gmail.com
 */

@Deprecated
public class WifiService extends Service {

    private static final String TAG = "Wifi";
    private static final String SERVICE_NAME = "Wifi.SlidingTiles.Team9.Service";
    private static final String SERVICE_TYPE = "_presence._tcp";
    private static final String SERVICE_INSTANCE = "_slidingiles";
    private static final String RECORD_STATUS = "STATUS";
    private static final String RECORD_USER = "USER";

    private static final String STATUS_DISCOVER = "discover";
    private static final String STATUS_BUSY = "busy";

    private static final String RECORD_NAME = "SERVICENAME";
    private static final String RECORD_SERVER = "SERVERNAME";
    private static final String RECORD_PORT = "SERVICEPORT";




    private static final AtomicBoolean s_Registered = new AtomicBoolean(false);


    private final IntentFilter m_Filter = new IntentFilter();

    private WifiP2pManager m_Manager;
    private MathOnlineDiscoveryActivity m_Activity;
    private WifiP2pManager.Channel m_Channel;
    private WifiP2pManager.PeerListListener m_PeerListener;
    private WifiP2pManager.GroupInfoListener m_GroupListener;
    private WifiP2pManager.ConnectionInfoListener m_ConListener;
    private WifiP2pManager.DnsSdTxtRecordListener m_ServiceListener;
    private WifiP2pManager.DnsSdServiceResponseListener m_ResponseListener;
    private WifiP2pDnsSdServiceRequest m_ServiceRequest;
    private WifiP2pServiceInfo m_ServiceInfo;
    private final ConnectivityManager m_ConManager;
    private final WifiManager m_WifiManager;
    private final Object m_Lock = new Object();
    private ConnectThread m_Connect;

    private String m_ErrorMessage = null;

    private boolean m_Connected;

    private boolean m_Registered;


    public WifiService() {
        super();
        m_Filter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        m_Filter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        m_Filter
                .addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        m_Filter
                .addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        m_WifiManager = (WifiManager) AppController.getInstance().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        m_ConManager = (ConnectivityManager) AppController.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
        if(m_ConManager == null)
        {
            m_ErrorMessage = "No wifi found.";
        }
        else {
            NetworkInfo wifi = m_ConManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (!wifi.isAvailable())
            {
                m_ErrorMessage = "No wireless service available";
                return;
            }
            if (!m_WifiManager.isP2pSupported()) {
                m_ErrorMessage = "Wifi P2P not supported!  ";
                return;
            }
            m_Manager = (WifiP2pManager) AppController.getInstance().getSystemService(Context.WIFI_P2P_SERVICE);
            if (m_Manager != null) {



            m_ResponseListener = (instanceName, registrationType, srcDevice) -> {
                // A service has been discovered. Is this our app?
                Log.i(TAG, "Wifi service found: " + instanceName);
                if (instanceName.equalsIgnoreCase(SERVICE_INSTANCE)) {
                    //We found a device with our game!
                    PeerInfo peer = PeerInfo.Retrieve(srcDevice.deviceAddress);
                    if (peer.Info != PeerInfo.Status.UNSUPPORTED)
                        peer.Update(PeerInfo.Status.AVAILABLE);
                }
            };

            m_GroupListener = group -> {
                if (group != null) {

                    //AppController.bindSocket(new Socket(group.getOwner().deviceAddress, ));
                    PeerInfo info = PeerInfo.Retrieve(group.getOwner().deviceAddress);
                    info.Message = "Peer group";
                    if (info.Info != PeerInfo.Status.UNSUPPORTED)
                        info.Update(group.getNetworkName(), PeerInfo.Status.UNSUPPORTED);
                    //TODO
                    Log.d("getNetworkName", group.getNetworkName());
                    Log.d(".deviceAddress", group.getOwner().deviceAddress);
                    Log.d(".deviceName", group.getOwner().deviceName);
                }
            };

            m_ConListener = info -> {
                if (info != null && info.groupOwnerAddress != null) {
//TODO

                    if (info.isGroupOwner) {

                    } else {
                    }
                    Log.d(".toString", info.groupOwnerAddress.toString());
                    Log.d(".getHostAddress", info.groupOwnerAddress.getHostAddress());
                    Log.d(".getHostName", info.groupOwnerAddress.getHostName());
                    Log.d(".getCanonicalHostName", info.groupOwnerAddress.getCanonicalHostName());
                    PeerInfo peer = PeerInfo.Retrieve(info.groupOwnerAddress.getHostAddress());
                    peer.Message = "Peer connection";
                    if (peer.Info != PeerInfo.Status.UNSUPPORTED)
                        peer.Update(PeerInfo.Status.UNSUPPORTED);
                }
            };

            m_PeerListener = peers -> {

                for (WifiP2pDevice w : peers.getDeviceList()) {
                    PeerInfo peer = PeerInfo.Retrieve(w.deviceAddress);
                    peer.Message = "Peer found";
                    if (peer.Info != PeerInfo.Status.UNSUPPORTED)
                        peer.Update(PeerInfo.Status.UNSUPPORTED);
                }
            };

            m_ServiceListener = (fullDomainName, txtRecordMap, srcDevice) -> {
                PeerInfo peer = PeerInfo.Retrieve(srcDevice.deviceAddress);
                String name = txtRecordMap.get(RECORD_USER);
                String status = txtRecordMap.get(RECORD_STATUS);
                int port = Integer.getInteger(txtRecordMap.get(RECORD_PORT));
                switch (status) {
                    case STATUS_BUSY:
                        peer.Message = "User is in a game";
                        peer.Update(name, PeerInfo.Status.UNSUPPORTED);
                        break;
                    case STATUS_DISCOVER:
                        try {
                            AppController.bindSocket(new Socket(srcDevice.deviceAddress, port));
                        } catch (IOException e) {
                            Log.e(TAG, "Failed to bind to " + srcDevice + " - " + e);
                        }
                        break;
                    default:
                        peer.Message = "User does not have SlidingTiles";
                        peer.Update(name, PeerInfo.Status.UNSUPPORTED);
                }
            };




        m_PeerListener = peers -> {
            Collection<WifiP2pDevice> deviceList = peers.getDeviceList();
            Log.d("PEERS", "PeerListListener "+ deviceList.size());
            for (WifiP2pDevice current : deviceList) {
                Log.d("DEVICE deviceAddress", current.deviceAddress);
                Log.d("DEVICE deviceName", current.deviceName);
                Log.d("primaryDeviceType", current.primaryDeviceType);

                //connect
                if (!m_Connected) {
                    WifiP2pConfig config = new WifiP2pConfig();
                    config.deviceAddress = current.deviceAddress;
                    m_Manager.connect(m_Channel, config, new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                            Log.d(TAG, "Connected  to peer - " + current);
                            m_Connected = true;
                        }

                        @Override
                        public void onFailure(int reason) {
                            Log.d(TAG, "Failed to connect to peer - " + current);
                        }
                    });
                }
            }
        };
            }
            else
            {
                m_ErrorMessage = "No p2p manager found";
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Start();
        if(m_Manager != null)
        {
            m_Channel = m_Manager.initialize(this, getMainLooper(), null);
            Map rec = new HashMap();
            rec.put(RECORD_PORT, String.valueOf(AppController.getPort()));
            rec.put(RECORD_SERVER, Build.MODEL);
            rec.put(RECORD_NAME, SERVICE_NAME);
            rec.put(RECORD_USER, getSharedPreferences(PREF, MODE_PRIVATE).getString(PREF_USER, "Nobody"));
            rec.put(RECORD_STATUS, STATUS_DISCOVER);

            m_ServiceInfo  = WifiP2pDnsSdServiceInfo.newInstance(SERVICE_INSTANCE, SERVICE_TYPE, rec);
            m_Manager.addLocalService(m_Channel, m_ServiceInfo, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Log.i(TAG,"Added Local Service");
                }
                @Override
                public void onFailure(int error) {
                    Log.i(TAG,"Failed to add a service");
                }
            });

            m_ServiceRequest = WifiP2pDnsSdServiceRequest.newInstance();
            m_Manager.setDnsSdResponseListeners(m_Channel, m_ResponseListener, m_ServiceListener);
            m_Manager.setServiceResponseListener(m_Channel, (protocolType, responseData, srcDevice) -> Log.d(TAG, "Service available from " + srcDevice));
            m_Manager.addServiceRequest(m_Channel, m_ServiceRequest, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onFailure(int reason) {

                }
            });

       /*     m_Manager.discoverPeers(m_Channel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onFailure(int reason) {

                }
            });
*/
            m_Manager.discoverServices(m_Channel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onFailure(int reason) {

                }
            });

        }
        else
        {
            Log.w(TAG,"Wifidirect not available");
            sendBroadcast(new Intent(ACTION_WIFI_UNSUPPORTED));
            stopSelf();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public boolean stopService(Intent name) {
        Stop();
        return super.stopService(name);
    }
    private boolean Start()
    {
        if(m_ErrorMessage != null)
        {
            sendBroadcast(new Intent(ACTION_WIFI_UNSUPPORTED).putExtra(EXTRA_REASON, m_ErrorMessage));
            stopSelf();
            return false;
        }
        if(!m_WifiManager.isWifiEnabled() || !m_WifiManager.setWifiEnabled(true))
        {
            sendBroadcast(new Intent(ACTION_WIFI_UNSUPPORTED).putExtra(EXTRA_REASON, "Failed to enable wifi"));
            stopSelf();
            return false;
        }
        if(m_Manager == null)
        {

            sendBroadcast(new Intent(ACTION_WIFI_UNSUPPORTED).putExtra(EXTRA_REASON, "No p2p manager found"));
            stopSelf();
            return false;
        }
        synchronized (m_Lock)
        {
            if(!m_Registered)
            {
                registerReceiver(m_LocalReceiver, m_Filter);
                m_Registered = true;
            }
        }
        return true;
    }


    private void Stop()
    {
        if(m_Connect != null)
        {
            m_Connect.cancel();
        }
        synchronized (m_Lock)
        {
            if(m_Registered)
            {
                unregisterReceiver(m_LocalReceiver);
                m_Registered = false;
            }
        }
        if(m_Manager != null && m_Channel != null)
        {
            m_Manager.removeGroup(m_Channel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onFailure(int reason) {

                }
            });
            m_Manager.removeServiceRequest(m_Channel, m_ServiceRequest,new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onFailure(int reason) {

                }
            });

            if(m_ServiceInfo != null){
            m_Manager.removeLocalService(m_Channel, m_ServiceInfo, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onFailure(int reason) {

                }
            });
            m_Manager.stopPeerDiscovery(m_Channel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onFailure(int reason) {

                }
            });
            m_Manager.cancelConnect(m_Channel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onFailure(int reason) {

                }
            });
            }
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Stop();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void conFailed(String dev)
    {
        synchronized (m_Lock)
        {
            m_Connect = null;
        }
        PeerInfo p = PeerInfo.Retrieve(dev);
       // p.Icon = m_DisabledIcon;
        p.Update(PeerInfo.Status.UNSUPPORTED);
    }

    private synchronized void Connected(@NonNull PeerInfo peer)
    {
        // Stop();

        Log.i(TAG,"Connected to " + peer);
        OutputStream o = null;
        InputStream i = null;
        Socket sock = (Socket)peer.Data;
        peer.Data = null;
        try {
            o = sock.getOutputStream();
        } catch (IOException e) {
            Log.e(TAG, "Faield to open output stream - " + e);
            try {
                sock.close();
            } catch (IOException eb) {
                Log.e(TAG, "Faield to close socket - " + eb);
            }
        }
        try {
            i = sock.getInputStream();
        } catch (IOException e) {
            Log.e(TAG, "Faield to open input stream - " + e);
            try {
                sock.close();
            } catch (IOException eb) {
                Log.e(TAG, "Faield to close socket - " + eb);
            }
        }

        if(i != null && o != null) {
            AppController.AddNetwork(new NetworkHandler(sock, peer.Address, i, o));
        }
    }

    private class ConnectThread extends Thread {
        @NonNull
        private final String mm_Device;
        private final int mm_Port;

        private final Socket mm_Socket = new Socket();

        public ConnectThread(@NonNull String addr, int port) {
            mm_Device = addr;
            mm_Port = port;
        }

        public void run() {
            setName("Wifi: Connect");

            try {
                mm_Socket.bind(null);
                mm_Socket.connect(new InetSocketAddress(mm_Device,
                        mm_Port));
            } catch (IOException e) {
                Log.e(TAG, "Failed to connect to " +  mm_Device);
                try {
                    mm_Socket.close();
                } catch (IOException e1) {
                    Log.e(TAG, "Could not close the client socket", e1);
                }finally {
                    conFailed(mm_Device);
                }
                return;
            }

            if(mm_Socket.isConnected()) {
                synchronized (m_Lock)
                {
                    m_Connect = null;
                }
                PeerInfo peer = PeerInfo.Retrieve(mm_Device);
                peer.Data = mm_Socket;
                sendBroadcast(new Intent(ACTION_WIFI_CON).putExtra(EXTRA_DEVICE, mm_Device));
            }
            else
                conFailed(mm_Device);
            //Connected(mm_Socket, mm_Device);
        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mm_Socket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }finally {
                conFailed(mm_Device);
            }
        }
    }


    private final BroadcastReceiver m_LocalReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action)
            {
                case WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION:
                {
                    // Determine if Wifi P2P mode is enabled or not, alert
                    // the Activity.
                    switch (intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1))
                    {
                        case WIFI_P2P_STATE_DISABLED:
                        {
                            Start();
                        }
                        case WIFI_P2P_STATE_ENABLED:
                            default:
                    }
                    // m_Activity.onWifiStatusChanged(state == WifiP2pManager.WIFI_P2P_STATE_ENABLED);

                }
                break;
                case WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION:
                {
                    Log.i(TAG, "Peer list changed");
                    m_Manager.requestPeers(m_Channel, m_PeerListener);
                }
                break;
                case WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION:
                {
                    WifiP2pInfo winfo = intent.getParcelableExtra(EXTRA_WIFI_P2P_INFO);
                    PeerInfo peer = PeerInfo.Retrieve(winfo.groupOwnerAddress.getHostAddress());
                    NetworkInfo networkInfo = intent
                            .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
                    if (networkInfo.isConnected()) {
                        // we are connected with the other device, request connection
                        // info to find group owner IP
                        peer.Update(PeerInfo.Status.DISCOVERED);
                        Log.d(TAG,
                                "Connected to p2p network. Requesting network details");
                        m_Manager.requestConnectionInfo(m_Channel, m_ConListener);
                    } else {
                        peer.Update(PeerInfo.Status.DISCOVERED);
                        // It's a disconnect
                    }
                }
                break;
                case WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION:
                    WifiP2pDevice dev = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
                    Log.d(TAG, "Device status - " + getStatus(dev.status));
                    m_Manager.requestConnectionInfo(m_Channel,m_ConListener);
                    break;
                case Constants.ACTION_WIFI_CON:
                    String d = intent.getStringExtra(EXTRA_DEVICE);
                    PeerInfo peer = PeerInfo.Retrieve(d);
                    Connected(peer);
                    break;




            }
        }
    };

    private static String getStatus(int i)
    {
        switch (i)
        {
            case 0: return "Connceted";
            case 1:return "Invited";
            case 2: return "Failed";
            case 3: return "Available";
            case 4: return "Unavailable";
        }
        return "Unknown";
    }
}
