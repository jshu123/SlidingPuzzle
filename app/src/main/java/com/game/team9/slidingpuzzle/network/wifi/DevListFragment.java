/*
 * Copyright (c) 2018. $user
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.game.team9.slidingpuzzle.network.wifi;

import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.game.team9.slidingpuzzle.R;
import com.game.team9.slidingpuzzle.network.DeviceObject;
import com.game.team9.slidingpuzzle.network.PeerListAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class DevListFragment extends ListFragment implements WifiP2pManager.PeerListListener {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER


    private ProgressBar m_Progress;
    private final List<DeviceObject> m_Peers = new ArrayList<>();
    private View m_ContentView;
    private DeviceObject m_Device;
    private TextView m_Name;
    private TextView m_Status;

    private PeerListAdapter m_ListAdapter;

    public DeviceObject getDevice()
    {
        return m_Device;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setListAdapter(new PeerListAdapter(getActivity(), R.layout.fragment_dev_detail, m_Peers));
    }

    public void setListAdapter(PeerListAdapter adapter) {
        super.setListAdapter(adapter);
        m_ListAdapter = adapter;
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        DeviceObject device = (DeviceObject) getListAdapter().getItem(position);
        ((IDeviceListener) getActivity()).showDetails(device);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return m_ContentView = inflater.inflate(R.layout.fragment_dev_list, container, false);
    }

    public void UpdateDevice(DeviceObject d)
    {
        m_Device = d;
    }

    private void initialize(){

        m_Name = m_ContentView.findViewById(R.id.nameText);
        m_Status = m_ContentView.findViewById(R.id.statusText);
    }

    @Override
    public void onPeersAvailable(@NonNull WifiP2pDeviceList wifiP2pDeviceList) {
        m_Peers.clear();
        for (WifiP2pDevice wifiP2pDevice : wifiP2pDeviceList.getDeviceList()) {
            m_Peers.add(new DeviceObject(wifiP2pDevice));
        }
        ((PeerListAdapter) getListAdapter()).notifyDataSetChanged();
    }

    public  void clearPeers()
    {
        m_Peers.clear();
        ((PeerListAdapter) getListAdapter()).notifyDataSetChanged();
    }


    public interface IDeviceListener {

        void showDetails(DeviceObject device);

        void cancelDisconnect();

        void connect(Parcelable config);

        void disconnect();
    }
}
