/*
 * Copyright (c) 2018. $user
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.game.team9.slidingpuzzle.network.wifi;

import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.game.team9.slidingpuzzle.R;
import com.game.team9.slidingpuzzle.network.DeviceObject;



public class DevDetailFragment extends Fragment implements WifiP2pManager.ConnectionInfoListener {

    private View m_ContentView;
    private WifiP2pInfo m_Info;
    private DeviceObject m_Device;

    private Button m_Connect;
    private Button m_Accept;
    private Button m_Activate;
    private Button m_FindDev;
    private TextView m_Status;
    private TextView m_DevInfo;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        m_ContentView = inflater.inflate(R.layout.fragment_dev_detail, container, false);

        m_Activate = m_ContentView.findViewById(R.id.activateButton);
        m_Accept = m_ContentView.findViewById(R.id.acceptButton);
        m_Connect = m_ContentView.findViewById(R.id.connectButton);
        m_FindDev = m_ContentView.findViewById(R.id.findButton);
        m_Status = m_ContentView.findViewById(R.id.statusText);
        m_DevInfo = m_ContentView.findViewById(R.id.infoText);

        return  m_ContentView;
    }

    public void connectOnClick(View view)
    {
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = m_Device.Ip;
        config.wps.setup = WpsInfo.PBC;

        ((DevListFragment.IDeviceListener) getActivity()).connect(config);
    }

    public void findOnClick(View view)
    {
    }

    public void acceptOnClick(View view)
    {
    }

    public void activateOnClick(View view)
    {

    }

    private void configButtons(){

        boolean dev = m_Device != null;
        boolean con = m_Info != null;

        m_FindDev.setVisibility((!dev &&!con && wifiIsOn())? View.VISIBLE : View.GONE);
        m_Activate.setVisibility((!wifiIsOn())? View.VISIBLE : View.GONE);

        m_Connect.setVisibility((dev && !con)? View.VISIBLE : View.GONE);
        m_Accept.setVisibility((dev && con) ? View.VISIBLE : View.GONE);
    }


    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
        m_Info = wifiP2pInfo;
        m_DevInfo.setText(m_Info.groupOwnerAddress.getHostAddress());
        if(wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner)
        {
            m_Accept.setVisibility(View.VISIBLE);
            m_Connect.setVisibility(View.GONE);
            m_Status.setText("You have been challeneged!");
        }
        else
        {
            m_Accept.setVisibility(View.GONE);
            m_Connect.setVisibility(View.VISIBLE);
            m_Status.setText("Invite player");
        }
    }

    public void Reset()
    {
        m_Device= null;
        m_Info = null;
        m_Status.setText("");
        m_DevInfo.setText("");
        configButtons();

    }

    private boolean wifiIsOn() {
        return ((MathWifiActivity) getActivity()).isWifiP2pEnabled();
    }

    public void devInfo(DeviceObject dev)
    {
        m_Device = dev;
        m_DevInfo.setText(dev.toString());
        configButtons();
    }
}
