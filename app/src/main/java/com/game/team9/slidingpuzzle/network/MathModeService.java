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
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pServiceInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.game.team9.slidingpuzzle.AppController;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.game.team9.slidingpuzzle.network.Constants.ACTION_ATTACH_HANDLE;
import static com.game.team9.slidingpuzzle.network.Constants.ACTION_SEND_ACCEPT;
import static com.game.team9.slidingpuzzle.network.Constants.ACTION_SEND_MOVE;
import static com.game.team9.slidingpuzzle.network.Constants.ACTION_SEND_QUIT;
import static com.game.team9.slidingpuzzle.network.Constants.ACTION_SEND_REJECT;
import static com.game.team9.slidingpuzzle.network.Constants.ACTION_SEND_TIME;
import static com.game.team9.slidingpuzzle.network.Constants.DEFAULT_PORT;
import static com.game.team9.slidingpuzzle.network.Constants.DEVICE_LIST_CHANGED;
import static com.game.team9.slidingpuzzle.network.Constants.EXTRA_DEVICE;
import static com.game.team9.slidingpuzzle.network.Constants.EXTRA_MOVEA;
import static com.game.team9.slidingpuzzle.network.Constants.EXTRA_OWNER_ADDRESS;
import static com.game.team9.slidingpuzzle.network.Constants.EXTRA_OWNER_PORT;
import static com.game.team9.slidingpuzzle.network.Constants.EXTRA_TIME;
import static com.game.team9.slidingpuzzle.network.Constants.SOCKET_TIMEOUT;


/***
 * NSD Broadcasting Service
 */
public class MathModeService extends Service {


    public static final String SERVICE_TYPE = "_nsdslidingtiles._tcp";
    public static final String SERVICE_NAME = "NsdSlidingTiles";
   // public static final byte START_MSG = 10;
    //public static final byte TIME_MSG = 11;
    //public static final byte MOVE_MSG = 12;
   // public static final byte QUIT_MSG = 13;

    private static NsdManager m_NsdManager;

    private static boolean m_Registered;
    private static final AtomicBoolean m_Registering = new AtomicBoolean(false);


    private final String m_UniqueName;
    private NsdManager.DiscoveryListener m_DiscoveryListener;
    private NsdManager.ResolveListener m_ResolveListener;
    private NsdManager.RegistrationListener m_RegListener;

    public MathModeService() {
        m_UniqueName = SERVICE_NAME + String.valueOf(new Random().nextInt(100));
    }
    @Override
    public void onCreate() {

        super.onCreate();
        m_NsdManager =  (NsdManager) getApplicationContext().getSystemService(Context.NSD_SERVICE);
        m_DiscoveryListener = new NsdManager.DiscoveryListener() {
            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.i("NSD", "Discovery start failed - " + serviceType);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.i("NSD", "Discovery stop failed - " + serviceType);
            }

            @Override
            public void onDiscoveryStarted(String serviceType) {
                Log.i("NSD", "Discovery started - " + serviceType);
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.i("NSD", "Discovery stopped - " + serviceType);
            }

            @Override
            public void onServiceFound(NsdServiceInfo serviceInfo) {
                Log.i("NSD", "Found service: " + serviceInfo.getServiceName() + " @ "+ serviceInfo.getServiceType() + " (" + serviceInfo.getHost() + ", " + serviceInfo.getPort() + ")");
                if(serviceInfo.getServiceType().startsWith(SERVICE_TYPE) && !serviceInfo.getServiceName().equals(m_UniqueName)) {
                    PeerInfo peer = PeerInfo.Retrieve(serviceInfo.getHost().getHostAddress());
                    peer.Update(info->m_NsdManager.resolveService(serviceInfo, m_ResolveListener));
                    //Toast.makeText(getApplicationContext(), "Success!!!" + serviceInfo.getServiceName(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo serviceInfo) {
                Log.i("NSD", "Lost service: " + serviceInfo.getServiceName() + " @ "+ serviceInfo.getServiceType());
                if(serviceInfo.getServiceType().startsWith(SERVICE_TYPE) && !serviceInfo.getServiceName().equals(m_UniqueName)) {
                    {
                        PeerInfo peer = PeerInfo.Retrieve(serviceInfo.getHost().getHostAddress());
                        peer.Update(PeerInfo.Status.UNSUPPORTED);
                    }
                }
            }
        };

        m_RegListener = new NsdManager.RegistrationListener() {
            @Override
            public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                m_Registered= false;
                m_NsdManager = null;
                Log.e("NSD", "Registration failed - " + errorCode);
                m_Registering.lazySet(false);
            }

            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Log.e("NSD", "Unregister failed - " + errorCode);
                m_Registered= true;
                m_Registering.lazySet(false);
            }

            @Override
            public void onServiceRegistered(NsdServiceInfo serviceInfo) {
                m_Registered= true;
                Log.i("NSD", "Registered ");
                Toast.makeText(getApplicationContext(), "Our service = " + serviceInfo.getServiceName(), Toast.LENGTH_SHORT).show();
                m_NsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, m_DiscoveryListener);
                m_Registering.lazySet(false);
            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo serviceInfo) {
                m_Registered= false;
                Log.i("NSD", "Unregistered ");
                m_NsdManager = null;
                m_Registering.lazySet(false);
            }
        };

        m_ResolveListener = new NsdManager.ResolveListener() {
            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Log.i("NSD", "Failed to resolve (" + errorCode + "): " + serviceInfo );
                PeerInfo peer = PeerInfo.Retrieve(serviceInfo.getHost().getHostAddress());
                peer.Update(PeerInfo.Status.DISCOVERED);
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                Log.i("NSD", "Resolved "  + serviceInfo );
                PeerInfo peer = PeerInfo.Retrieve(serviceInfo.getHost().getHostAddress());
                peer.Update(PeerInfo.Status.AVAILABLE);
            }
        };

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if(m_Registered)
            m_NsdManager.unregisterService(m_RegListener);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        NsdServiceInfo serviceInfo = new NsdServiceInfo();

        // The name is subject to change based on conflicts
        // with other services advertised on the same network.
        serviceInfo.setServiceName(m_UniqueName);
        serviceInfo.setServiceType(SERVICE_TYPE);
        serviceInfo.setPort(AppController.getPort());

        m_NsdManager.registerService(serviceInfo,NsdManager.PROTOCOL_DNS_SD, m_RegListener);

        return START_STICKY;
    }
}
