/*
 * Copyright (c) 2018. $user
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.game.team9.slidingpuzzle.network;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Stack;
import java.util.concurrent.Callable;

import static com.game.team9.slidingpuzzle.network.Constants.ACTION_NEW_PEER;
import static com.game.team9.slidingpuzzle.network.Constants.EXTRA_DEVICE;

/**
 * Created on: 2/19/18
 * Author: David Hiatt - dhiatt89@gmail.com
 */

public class PeerInfo implements Serializable, View.OnClickListener {
    private static final String TAG = "PeerInfo";
    public String Name;
    public final String Address;
    public Status Info;

    transient IPeerCallback Callback;
    transient IPeerCallback OnCancel;

    public transient Object Data;

    public int Icon;

    public String Message;

    private final Stack<PeerState> m_Stack = new Stack<>();

    private static final Hashtable<String, PeerInfo> s_Peers = new Hashtable<>();

    public static LocalBroadcastManager Broadcaster;


    public void Pop(){
        PeerState s = m_Stack.pop();
        if(s != null)
        {
            Message = s.Message;
            Info = s.Info;
            Name = s.Name;
            Data = s.Data;
            Callback = s.Callback;
            onUpdate(this);
        }
    }

    public void setDiscovered(@NonNull String msg, @NonNull  IPeerCallback callback)
    {
        Message = msg;
        Info = Status.DISCOVERED;
        Callback = callback;
        onUpdate(this);
    }

    public void setConnecting(@NonNull String msg, @NonNull IPeerCallback callback)
    {
        Message = msg;
        Info = Status.CONNECTING;
        Callback = callback;
        onUpdate(this);
    }

    public void setUnsupported(@NonNull String msg)
    {
        Message = msg;
        Info = Status.UNSUPPORTED;
        onUpdate(this);
    }

    public void setOnCancel(IPeerCallback c)
    {
     OnCancel = c;
     onUpdate(this);

    }

    public void Update(){onUpdate(this);}
    public void Update(IPeerCallback callback){Update(Name, Info, callback);}

    public void Update(String name) {
        Update(name, Info);
    }

    public void Update(String name, Status info)
    {
        Update(name, info, null);
    }

    public void Update(Status info)
    {
        Update(Name, info, null);
    }
    public void Update(Status info, IPeerCallback callback)
    {
        Update(Name, info, callback);
    }

    public void Update(String name, Status info, IPeerCallback callback)
    {
        Log.i(TAG, "Updated " + this + " to " + Name + ": " + info);
        Name = name;
        Info = info;
        Callback = callback;

        onUpdate(this);
    }

    private static void onUpdate(PeerInfo info)
    {
        info.m_Stack.push(new PeerState(info));
        if(Broadcaster!=null)
            Broadcaster.sendBroadcast(new Intent(ACTION_NEW_PEER).putExtra(EXTRA_DEVICE, info));
    }

    @Override
    public void onClick(View v) {
        if(Callback == null)
        {
            Log.e(TAG,"No callback registered for " + this);
        }
        else
            Callback.onClick(this);
    }

    private PeerInfo(String addr)
    {

        Address = addr;
        Name = addr;
        Info = Status.INVALID;
        if(s_Peers.containsKey(addr))
        {
            Log.e(TAG, "Duplicate peer - " + this);
        }
        else
            Log.i(TAG,"New peer - " + this);
        s_Peers.put(addr, this);
    }

    public static PeerInfo Retrieve(String id)
    {
        PeerInfo peer = s_Peers.get(id);
        if(peer == null)
            peer = new PeerInfo(id);
        return peer;
    }

    static ArrayList<PeerInfo> getPeers(){return Collections.list(s_Peers.elements());}

    public enum Status
    {
        INVALID,                //Peer
        UNSUPPORTED,            //Peer found, they do not have our app
        DISCOVERED,             //Peer found, not sure if they have app.  Must have a network handler by this point
        CONNECTING,             //Peer found, determining if they are running the app
        AVAILABLE,              //They are running the app too
        INBOUND_REQUEST_CUT,    //Request to us
        INBOUND_REQUEST_BAS,    //Request to us
        OUTBOUND_REQUEST,   //Request sent to them
        ACTIVE              //In a match with us
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof PeerInfo)
        {
            PeerInfo o = (PeerInfo)obj;
            return Address.equals(o.Address);
        }
        return super.equals(obj);
    }

    public interface IPeerCallback
    {
        void onClick(PeerInfo info);
    }

    @Override
    public String toString() {
        return Name + "@" + Address + ": " + Info + "(" + Message + ")";
    }

    public void Restore(PeerState info)
    {
        Info = info.Info;
        Name = info.Name;
        Message = info.Message;
        Data = info.Data;
        Icon = info.Icon;
        OnCancel = info.OnCancel;
    }

    private static class PeerState
    {
        public PeerState(PeerInfo info)
        {
            Callback = info.Callback;
            Info = info.Info;
            Name = info.Name;
            Message = info.Message;
            Data = info.Data;
            Icon = info.Icon;
            OnCancel = info.OnCancel;

        }
        public final IPeerCallback OnCancel;
        public final IPeerCallback Callback;
        public final Status Info;
        public final String Name;
        public final String Message;
        public final Object Data;
        public final int Icon;
    }
}
