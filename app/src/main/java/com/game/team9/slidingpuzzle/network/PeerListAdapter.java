/*
 * Copyright (c) 2018. $user
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.game.team9.slidingpuzzle.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.game.team9.slidingpuzzle.AppController;
import com.game.team9.slidingpuzzle.MathDoubleBasicActivity;
import com.game.team9.slidingpuzzle.MathDoubleCuthroatActivity;
import com.game.team9.slidingpuzzle.MathOnlineDiscoveryActivity;
import com.game.team9.slidingpuzzle.R;

import static com.game.team9.slidingpuzzle.network.PeerInfo.Status.INBOUND_REQUEST_BAS;

/**
 * Created on: 2/7/18
 * Author: David Hiatt - dhiatt89@gmail.com
 */

public class PeerListAdapter extends ArrayAdapter<PeerInfo> {

    private static final String TAG = "Peer";

    private final MathOnlineDiscoveryActivity m_Context;
    public static int m_ConnectIcon;
    public static int m_CancelIcon;
    public static int m_RejectIcon;
    public static int m_AcceptIcon;
    public static int m_ConnectingIcon;
    public static int m_WaitingIcon;
    public static int ActiveIcon;

    public PeerListAdapter(MathOnlineDiscoveryActivity context, int id) {
        super(context,id, PeerInfo.getPeers());
        m_Context= context;
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.ACTION_NEW_PEER);
        LocalBroadcastManager.getInstance(m_Context).registerReceiver(m_LocalReceiver, filter);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            v = LayoutInflater.from(m_Context).inflate(R.layout.fragment_dev_detail, parent, false);
        }
        PeerInfo peer = getItem(position);
        Log.i(TAG, "Adding " + peer);
        if(peer== null)
            return v;
        final String device = peer.Name;
        final String addr = peer.Address;
        ImageView image = v.findViewById(R.id.row_icon);
        if(peer.Icon != -1)
        {
            image.setImageResource(peer.Icon);
            image.setVisibility(View.VISIBLE);
        }
        else
            image.setVisibility(View.GONE);

        TextView text = v.findViewById(R.id.nameText);
        text.setText(device);
        text = v.findViewById(R.id.statusText);
        text.setText(peer.Message);
        ImageButton connect = v.findViewById(R.id.connectButton);
        ImageButton decline = v.findViewById(R.id.declineButton);

        switch (peer.Info)
        {

            case INVALID:
                remove(peer);
                Log.i(TAG, "Deleteing " + peer);
                notifyDataSetChanged();
                return v;
            case UNSUPPORTED:
                connect.setEnabled(false);
                decline.setVisibility(View.GONE);
                Log.i(TAG, "Unsupported " + peer);
                break;
            case DISCOVERED:
                if(peer.Callback == null)
                {
                    Log.e(TAG,"Discovered device lacks callback! - " + peer);
                    return v;
                }
                else
                {
                    Log.i(TAG, "Discovered " + peer);
                    text.setText("");
                    decline.setVisibility(View.GONE);
                    connect.setVisibility(View.VISIBLE);
                    connect.setEnabled(true);
                    connect.setOnClickListener(peer);
                }
                break;
            case CONNECTING:
                connect.setVisibility(View.VISIBLE);
                connect.setImageResource(m_ConnectingIcon);
                connect.setEnabled(false);
                decline.setVisibility(View.VISIBLE);
                decline.setImageResource(m_CancelIcon);
                decline.setEnabled(true);
                decline.setOnClickListener(peer);
                Log.i(TAG, "Connecting, " + m_ConnectingIcon + ", " + m_CancelIcon + ", " + peer);
                break;
            case AVAILABLE:
                decline.setVisibility(View.GONE);
                text.setText("");
                connect.setVisibility(View.VISIBLE);
                connect.setEnabled(true);
                connect.setImageResource(m_ConnectIcon);
                connect.setOnClickListener(v14 ->{
                        Packet p = Packet.AcquirePacket(addr, Packet.Header.REQUEST);
                        p.Length = 2;
                        p.Data[0] = (byte) AppController.getGameMode();
                        p.Data[1] = (byte) AppController.getRounds();
                    AppController.SendData(p);
                    peer.Info = PeerInfo.Status.OUTBOUND_REQUEST;
                    notifyDataSetChanged();
                });
                Log.i(TAG, "Available, " + m_ConnectingIcon + ", "  + peer);
                break;
            case INBOUND_REQUEST_CUT:
            case INBOUND_REQUEST_BAS:
                String t = "They have invite you to play " + (byte)peer.Data + " rounds of " + (peer.Info == INBOUND_REQUEST_BAS ? "basic mode" : "cutthroat mode");
                text.setText(t);
                decline.setVisibility(View.VISIBLE);
                decline.setEnabled(true);
                decline.setImageResource(m_RejectIcon);
                decline.setOnClickListener(v13 -> {
                    AppController.SendData(Packet.AcquirePacket(addr, Packet.Header.QUIT));
                    peer.Info = PeerInfo.Status.AVAILABLE;
                    notifyDataSetChanged();
                });
                connect.setVisibility(View.VISIBLE);
                connect.setEnabled(true);
                connect.setImageResource(m_AcceptIcon);
                   connect.setOnClickListener(v12 -> {
                    Intent intent = new Intent(getContext(), peer.Info == INBOUND_REQUEST_BAS ? MathDoubleBasicActivity.class : MathDoubleCuthroatActivity.class);
                    intent.putExtra(Constants.EXTRA_ID, device);
                    intent.putExtra(Constants.EXTRA_DEVICE, addr);
                    intent.putExtra(Constants.EXTRA_IS_HOST, false);
                       intent.putExtra(Constants.EXTRA_ROUNDS, (byte)peer.Data);
                    AppController.SendData(Packet.AcquirePacket(addr, Packet.Header.ACCEPT));
                    peer.Info = PeerInfo.Status.ACTIVE;
                    notifyDataSetChanged();
                    m_Context.LaunchGame(intent);
                });
                Log.i(TAG, "Inbound, " + m_AcceptIcon + ", " + m_RejectIcon + ", " + peer);
                break;
            case OUTBOUND_REQUEST:
                connect.setVisibility(View.VISIBLE);
                connect.setImageResource(m_WaitingIcon);
                connect.setEnabled(false);
                decline.setVisibility(View.VISIBLE);
                decline.setEnabled(true);
                decline.setImageResource(m_CancelIcon);
                decline.setOnClickListener(v1 -> {
                    AppController.SendData(Packet.AcquirePacket(addr, Packet.Header.QUIT));
                    peer.Info = PeerInfo.Status.AVAILABLE;
                    notifyDataSetChanged();
                });
                Log.i(TAG, "Outbound, " + m_WaitingIcon + ", " + m_CancelIcon + ", " + peer);
                break;
            case ACTIVE:
                decline.setImageResource(m_RejectIcon);
                decline.setVisibility(View.VISIBLE);
                connect.setVisibility(View.VISIBLE);
                connect.setImageResource(ActiveIcon);
                connect.setEnabled(false);
                Log.i(TAG, "Active, " + ActiveIcon + ", " + m_RejectIcon + ", " + peer);
                break;
        }

        return v;
    }


    public void Teardown()
    {
        LocalBroadcastManager.getInstance(m_Context).unregisterReceiver(m_LocalReceiver);
        for(int i = 0; i < getCount(); ++i)
        {
            PeerInfo peer = getItem(i);
            if (peer != null) {
                switch(peer.Info)
                {
                    case UNSUPPORTED:
                        break;
                    case ACTIVE:
                    case AVAILABLE:
                    case INVALID:
                    case DISCOVERED:
                        break;
                    case CONNECTING:
                        peer.Callback.onClick(null);
                        break;
                    case INBOUND_REQUEST_CUT:
                    case INBOUND_REQUEST_BAS:
                    case OUTBOUND_REQUEST:
                        AppController.SendData(Packet.AcquirePacket(peer.Address, Packet.Header.QUIT));
                        break;
                }
            }
        }
    }

    private final BroadcastReceiver m_LocalReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action != null && action.equals(Constants.ACTION_NEW_PEER)) {
                clear();
                addAll(PeerInfo.getPeers());
                m_Context.runOnUiThread(() -> notifyDataSetChanged());
            }
        }
    };
}
