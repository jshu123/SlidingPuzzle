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
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.game.team9.slidingpuzzle.AppController;

import java.net.InetAddress;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.game.team9.slidingpuzzle.network.Constants.ACTION_NAME_CHANGE;
import static com.game.team9.slidingpuzzle.network.Constants.PREF;
import static com.game.team9.slidingpuzzle.network.Constants.PREF_USER;


/***
 * NSD Broadcasting Service
 */
public class MathNSDService extends Service {

    private static final String TAG = "NSD";
    public static final String SERVICE_TYPE = "_nsdslidingtiles._tcp";
    public static final String SERVICE_NAME = "NsdSlidingTiles";

    public static int NSDIcon;

    private NsdManager m_NsdManager;

    private final AtomicBoolean m_Registered = new AtomicBoolean(false);
    private boolean m_Discovering;
    private static final AtomicBoolean m_Registering = new AtomicBoolean(false);
    private static final Object m_Lock = new Object();


    private String m_Name;
    private final String m_UniqueName;
    private NsdManager.DiscoveryListener m_DiscoveryListener;
    private NsdManager.RegistrationListener m_RegListener;

    SharedPreferences.OnSharedPreferenceChangeListener m_NameChanger = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if(key.equals(PREF_USER))
                Reset(sharedPreferences.getString(key, m_Name));
        }
    };

    public MathNSDService() {
        m_UniqueName = SERVICE_NAME + String.valueOf(new Random().nextInt(100));
    }
    @Override
    public void onCreate() {

        super.onCreate();
        m_NsdManager =  (NsdManager) getApplicationContext().getSystemService(Context.NSD_SERVICE);


        m_RegListener = new NsdManager.RegistrationListener() {
            @Override
            public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                m_Registered.lazySet(false);
                //m_NsdManager = null;
                Log.e(TAG, "Registration failed - " + errorCode);
                m_Registering.lazySet(false);
            }

            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Log.e(TAG, "Unregister failed - " + errorCode);
                m_Registering.lazySet(false);
            }

            @Override
            public void onServiceRegistered(NsdServiceInfo serviceInfo) {
                m_Registered.set(true);
                SharedPreferences pref = getSharedPreferences(PREF, MODE_PRIVATE);
                pref.registerOnSharedPreferenceChangeListener(m_NameChanger);
                Log.i(TAG, "Registered ");
                m_NsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, m_DiscoveryListener);
                m_Registering.lazySet(false);
            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo serviceInfo) {
                SharedPreferences pref = getSharedPreferences(PREF, MODE_PRIVATE);
                pref.unregisterOnSharedPreferenceChangeListener(m_NameChanger);
                Log.i(TAG, "Unregistered ");
                //m_NsdManager = null;
                m_Registering.lazySet(false);
                synchronized (m_Registered) {
                    m_Registered.set(false);
                    m_Registered.notifyAll();
                }
            }
        };

        m_DiscoveryListener = new NsdManager.DiscoveryListener() {
            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.i(TAG, "Discovery start failed - " + serviceType);
                m_Discovering = false;
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.i(TAG, "Discovery stop failed - " + serviceType);
            }

            @Override
            public void onDiscoveryStarted(String serviceType) {
                Log.i(TAG, "Discovery started - " + serviceType);
                m_Discovering= true;
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.i(TAG, "Discovery stopped - " + serviceType);
                m_Discovering = false;
            }

            @Override
            public void onServiceFound(NsdServiceInfo serviceInfo) {
                Log.i(TAG, "Found service: " + serviceInfo.getServiceName() + " @ "+ serviceInfo.getServiceType() + " (" + serviceInfo.getHost() + ", " + serviceInfo.getPort() + ")");
                if(serviceInfo.getServiceType().startsWith(SERVICE_TYPE) && !serviceInfo.getServiceName().equals(m_Name)) {
                    m_NsdManager.resolveService(serviceInfo, new NsdManager.ResolveListener() {
                        @Override
                        public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                            Log.i(TAG, "Failed to resolve (" + errorCode + "): " + serviceInfo );
                        }

                        @Override
                        public void onServiceResolved(NsdServiceInfo serviceInfo) {
                            Log.i(TAG, "Resolved "  + serviceInfo );
                            InetAddress addr = serviceInfo.getHost();

                            if(addr != null)
                            {
                                PeerInfo peer = PeerInfo.Retrieve(addr.getHostAddress());
                                peer.Name =serviceInfo.getServiceName();
                                peer.Icon = NSDIcon;
                                AppController.bindSocket(serviceInfo.getHost().getHostAddress(), serviceInfo.getPort());
                            }
                        }
                    });
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo serviceInfo) {
                Log.i(TAG, "Lost service: " + serviceInfo);
                if(serviceInfo.getServiceType().startsWith(SERVICE_TYPE) && !serviceInfo.getServiceName().equals(m_Name)) {
                    InetAddress addr = serviceInfo.getHost();
                    if (addr != null)
                    {
                        PeerInfo peer = PeerInfo.Retrieve(addr.getHostAddress());
                        peer.Update(PeerInfo.Status.INVALID);
                    }
                    else
                    {
                        PeerInfo peer = null;
                        for (PeerInfo i : PeerInfo.getPeers()) {
                            if (i.Name.equals(serviceInfo.getServiceName())) {
                                peer = i;
                                break;
                            }
                        }
                        if(peer != null)
                            peer.Update(PeerInfo.Status.INVALID);
                    }
                }
            }
        };
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if(m_Registered.get())
        {
            m_NsdManager.unregisterService(m_RegListener);
            SharedPreferences pref = getSharedPreferences(PREF, MODE_PRIVATE);
            pref.unregisterOnSharedPreferenceChangeListener(m_NameChanger);
            m_Registered.lazySet(false);
        }
        Log.i(TAG, "Destroyed");
    }

    private void Reset(String name)
    {
        Log.i(TAG, "Resetting from " + m_Name + " to " + name);
        NsdServiceInfo serviceInfo = new NsdServiceInfo();
        serviceInfo.setServiceName(name);
        serviceInfo.setServiceType(SERVICE_TYPE);
        serviceInfo.setPort(AppController.getPort());
        synchronized (m_Lock)
        {
            if(m_Name != null)
            {
                if(m_Discovering)
                    m_NsdManager.stopServiceDiscovery(m_DiscoveryListener);
                m_NsdManager.unregisterService(m_RegListener);
                Log.i(TAG, "Stopping " + m_Name);
                while (m_Registered.get())
                {
                    synchronized (m_Registered) {
                        try {
                            m_Registered.wait();
                        } catch (InterruptedException e) {
                            Log.e(TAG, "Failed to wait in unregistration", e);
                        }
                    }
                }
            }
            m_Name = name;
            m_NsdManager.registerService(serviceInfo,NsdManager.PROTOCOL_DNS_SD, m_RegListener);
        }
        Log.i(TAG, "Started as " + m_Name);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        SharedPreferences pref = getSharedPreferences(PREF, MODE_PRIVATE);
        Reset(pref.getString(PREF_USER, m_UniqueName));
        return START_STICKY;
    }
}
