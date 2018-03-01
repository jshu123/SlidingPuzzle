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
import android.content.Context;
import android.content.Intent;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.game.team9.slidingpuzzle.AppController;

import java.io.IOException;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;


/***
 * NSD Broadcasting Service
 */
public class MathModeService extends Service {

    private static final String TAG = "NSD";
    public static final String SERVICE_TYPE = "_nsdslidingtiles._tcp";
    public static final String SERVICE_NAME = "NsdSlidingTiles";

    private NsdManager m_NsdManager;

    private boolean m_Registered;
    private static final AtomicBoolean m_Registering = new AtomicBoolean(false);


    private final String m_UniqueName;
    private NsdManager.DiscoveryListener m_DiscoveryListener;
    private NsdManager.ResolveListener m_ResolveListener;
    private NsdManager.RegistrationListener m_RegListener;

    private PeerInfo.IPeerCallback m_OnResolve;
    private PeerInfo.IPeerCallback m_OnDiscovered;

    public MathModeService() {
        m_UniqueName = SERVICE_NAME + String.valueOf(new Random().nextInt(100));
        m_OnResolve = info-> {
            info.setConnecting("Resolving..", m_OnDiscovered);
            m_NsdManager.resolveService((NsdServiceInfo)info.Data, m_ResolveListener);
        };
        m_OnDiscovered = info -> info.setDiscovered("Found service, click to resolve.", m_OnResolve);
    }
    @Override
    public void onCreate() {

        super.onCreate();
        m_NsdManager =  (NsdManager) getApplicationContext().getSystemService(Context.NSD_SERVICE);


        m_RegListener = new NsdManager.RegistrationListener() {
            @Override
            public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                m_Registered= false;
                m_NsdManager = null;
                Log.e(TAG, "Registration failed - " + errorCode);
                m_Registering.lazySet(false);
            }

            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Log.e(TAG, "Unregister failed - " + errorCode);
                m_Registered= true;
                m_Registering.lazySet(false);
            }

            @Override
            public void onServiceRegistered(NsdServiceInfo serviceInfo) {
                m_Registered= true;
                Log.i(TAG, "Registered ");
                m_NsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, m_DiscoveryListener);
                m_Registering.lazySet(false);
            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo serviceInfo) {
                m_Registered= false;
                Log.i(TAG, "Unregistered ");
                m_NsdManager = null;
                m_Registering.lazySet(false);
            }
        };

        m_DiscoveryListener = new NsdManager.DiscoveryListener() {
            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.i(TAG, "Discovery start failed - " + serviceType);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.i(TAG, "Discovery stop failed - " + serviceType);
            }

            @Override
            public void onDiscoveryStarted(String serviceType) {
                Log.i(TAG, "Discovery started - " + serviceType);
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.i(TAG, "Discovery stopped - " + serviceType);
            }

            @Override
            public void onServiceFound(NsdServiceInfo serviceInfo) {
                Log.i(TAG, "Found service: " + serviceInfo.getServiceName() + " @ "+ serviceInfo.getServiceType() + " (" + serviceInfo.getHost() + ", " + serviceInfo.getPort() + ")");
                if(serviceInfo.getServiceType().startsWith(SERVICE_TYPE) && !serviceInfo.getServiceName().equals(m_UniqueName)) {
                    PeerInfo peer = PeerInfo.Retrieve(serviceInfo.getHost().getHostAddress());
                    peer.Data = serviceInfo;
                    peer.setDiscovered("Found service, click to resolve.", m_OnResolve);
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo serviceInfo) {
                Log.i(TAG, "Lost service: " + serviceInfo.getServiceName() + " @ "+ serviceInfo.getServiceType());
                if(serviceInfo.getServiceType().startsWith(SERVICE_TYPE) && !serviceInfo.getServiceName().equals(m_UniqueName)) {
                    PeerInfo peer = PeerInfo.Retrieve(serviceInfo.getHost().getHostAddress());
                    peer.Data = null;
                    peer.setUnsupported("Service has been lost");
                }
            }
        };

        m_ResolveListener = new NsdManager.ResolveListener() {
            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Log.i(TAG, "Failed to resolve (" + errorCode + "): " + serviceInfo );
                PeerInfo peer = PeerInfo.Retrieve(serviceInfo.getHost().getHostAddress());
                peer.setUnsupported("Failed to resolve host.");
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                Log.i(TAG, "Resolved "  + serviceInfo );
                AppController.bindSocket(serviceInfo.getHost().getHostAddress(), serviceInfo.getPort());
            }
        };

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if(m_Registered)
            m_NsdManager.unregisterService(m_RegListener);
        Log.i(TAG, "Destroyed");
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
        Log.i(TAG, "Started");
        return START_STICKY;
    }
}
