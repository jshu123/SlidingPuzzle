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
        Button connect = v.findViewById(R.id.connectButton);
        Button decline = v.findViewById(R.id.declineButton);
        switch (peer.Info)
        {

            case INVALID:
                remove(peer);
                notifyDataSetChanged();
                return v;
            case UNSUPPORTED:
                connect.setEnabled(false);
                decline.setVisibility(View.GONE);
                break;
            case DISCOVERED:
                if(peer.Callback == null)
                {
                    Log.e(TAG,"Discovered device lacks callback! - " + peer);
                    return v;
                }
                else
                {
                    text.setText("");
                    decline.setVisibility(View.GONE);
                    connect.setVisibility(View.VISIBLE);
                    connect.setEnabled(true);
                    connect.setText(R.string.discover);
                    connect.setOnClickListener(peer);
                }
                break;
            case CONNECTING:
                connect.setVisibility(View.VISIBLE);
                connect.setEnabled(false);
                decline.setVisibility(View.VISIBLE);
                decline.setEnabled(true);
                decline.setText(R.string.cancel);
                decline.setOnClickListener(peer);
                break;
            case AVAILABLE:
                decline.setVisibility(View.GONE);
                text.setText("");
                connect.setText(R.string.connect);
                connect.setVisibility(View.VISIBLE);
                connect.setEnabled(true);
                connect.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AppController.SendData(Packet.AcquirePacket(addr, Packet.Header.REQUEST));
                        peer.Info = PeerInfo.Status.OUTBOUND_REQUEST;
                        notifyDataSetChanged();
                    }
                });
                break;
            case INBOUND_REQUEST_CUT:
            case INBOUND_REQUEST_BAS:
                Class c = peer.Info == INBOUND_REQUEST_BAS ? MathDoubleBasicActivity.class : MathDoubleCuthroatActivity.class;
                text.setText(peer.Info == INBOUND_REQUEST_BAS ? R.string.req_bas : R.string.req_cut);
                decline.setVisibility(View.VISIBLE);
                decline.setEnabled(true);
                decline.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AppController.SendData(Packet.AcquirePacket(addr, Packet.Header.QUIT));
                        peer.Info = PeerInfo.Status.AVAILABLE;
                        notifyDataSetChanged();
                    }
                });
                connect.setVisibility(View.VISIBLE);
                connect.setEnabled(true);
                connect.setText(R.string.accept);
                connect.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getContext(), c);
                        intent.putExtra(Constants.EXTRA_ID, device);
                        intent.putExtra(Constants.EXTRA_DEVICE, addr);
                        intent.putExtra(Constants.EXTRA_IS_HOST, false);
                        AppController.SendData(Packet.AcquirePacket(addr, Packet.Header.ACCEPT));
                        peer.Info = PeerInfo.Status.ACTIVE;
                        notifyDataSetChanged();
                        m_Context.LaunchGame(intent);
                    }
                });
                break;
            case OUTBOUND_REQUEST:
                connect.setVisibility(View.VISIBLE);
                connect.setEnabled(false);
                decline.setVisibility(View.VISIBLE);
                decline.setEnabled(true);
                decline.setText(R.string.cancel);
                decline.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AppController.SendData(Packet.AcquirePacket(device, Packet.Header.QUIT));
                        peer.Info = PeerInfo.Status.AVAILABLE;
                        notifyDataSetChanged();
                    }
                });
                break;
            case ACTIVE:
                decline.setVisibility(View.GONE);
                connect.setEnabled(false);
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
