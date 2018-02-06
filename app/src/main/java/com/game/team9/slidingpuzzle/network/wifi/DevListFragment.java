/*
 * Copyright (c) 2018. $user
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.game.team9.slidingpuzzle.network.wifi;

import android.content.Context;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.game.team9.slidingpuzzle.R;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link IOnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DevListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DevListFragment extends ListFragment implements WifiP2pManager.PeerListListener {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_DEV_LIST = "dev_list";

    // TODO: Rename and change types of parameters
    @Nullable
    private String mParam1;

    @Nullable
    private IOnFragmentInteractionListener m_Listener;
    private final List<WifiP2pDevice> m_Peers = new ArrayList<WifiP2pDevice>();
    private View m_ContentView;
    private WifiP2pDevice m_Device;

    public DevListFragment() {

        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment DevListFragment.
     */
    // TODO: Rename and change types and number of parameters
    @NonNull
    public static DevListFragment newInstance(int c) {
        DevListFragment fragment = new DevListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_DEV_LIST, c);
        fragment.setArguments(args);
        return fragment;
    }


    public WifiP2pDevice getDevice()
    {
        return m_Device;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.setListAdapter(new WifiPeerListAdapter(getActivity(), R.layout.row_device, m_Peers));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_DEV_LIST);
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        WifiP2pDevice device = (WifiP2pDevice) getListAdapter().getItem(position);
        ((IDeviceListener) getActivity()).showDetails(device);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return m_ContentView = inflater.inflate(R.layout.fragment_peer_list, container, false);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (m_Listener != null) {
            m_Listener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof IOnFragmentInteractionListener) {
            m_Listener = (IOnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement IOnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        m_Listener = null;
    }

    @Override
    public void onPeersAvailable(@NonNull WifiP2pDeviceList wifiP2pDeviceList) {
        m_Peers.clear();
        m_Peers.addAll(wifiP2pDeviceList.getDeviceList());
        ((WifiPeerListAdapter) getListAdapter()).notifyDataSetChanged();
        if(m_Peers.size() == 0)
        {

        }
    }

    public  void clearPeers()
    {
        m_Peers.clear();
        ((WifiPeerListAdapter) getListAdapter()).notifyDataSetChanged();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface IOnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public interface IDeviceListener {

        void showDetails(WifiP2pDevice device);

        void cancelDisconnect();

        void connect(WifiP2pConfig config);

        void disconnect();
    }

    private class WifiPeerListAdapter extends ArrayAdapter<WifiP2pDevice>
    {

        private List<WifiP2pDevice> mm_Items;

        public WifiPeerListAdapter(@NonNull Context context, int resource, @NonNull List<WifiP2pDevice> objects) {
            super(context, resource, objects);
            mm_Items = objects;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.row_device, null);
            }
            WifiP2pDevice device = mm_Items.get(position);
            if (device != null) {
                TextView top = v.findViewById(R.id.device_name);
                TextView bottom = v.findViewById(R.id.device_details);
                if (top != null) {
                    top.setText(device.deviceName);
                }
                if (bottom != null) {
                    bottom.setText(device.status);
                }
            }

            return v;

        }
    }
}
