/*
 * Copyright (c) 2018. $user
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.game.team9.slidingpuzzle.network;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.game.team9.slidingpuzzle.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on: 2/7/18
 * Author: David Hiatt - dhiatt89@gmail.com
 */

public class PeerListFragment extends Fragment {
    public static final String ARG_DEVICE_LIST = "device_list";
    private OnListFragmentInteractionListener m_Listener;

    private List<DeviceObject> m_Devices;
    private RecyclerView m_Recycler;


    public PeerListFragment()
    {
        m_Devices=  new ArrayList<>();
    }
    public static PeerListFragment newInstance(int columnCount) {
        PeerListFragment fragment = new PeerListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_DEVICE_LIST, columnCount);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        m_Devices = new ArrayList<>();
        if (getArguments() != null) {
            m_Devices = (List<DeviceObject>) getArguments().getSerializable(ARG_DEVICE_LIST);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_peer_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            m_Recycler = (RecyclerView) view;
            m_Recycler.setLayoutManager(new LinearLayoutManager(context));

            //m_Recycler.setAdapter(new PeerListAdapter(m_Devices, m_Listener));
        }
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof OnListFragmentInteractionListener)
            m_Listener = (OnListFragmentInteractionListener)context;
        else
        {
            throw new RuntimeException(context.toString() + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        m_Listener = null;
    }

    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(DeviceObject dev);
    }
}
