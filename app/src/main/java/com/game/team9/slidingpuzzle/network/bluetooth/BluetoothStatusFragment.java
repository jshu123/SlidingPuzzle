/*
 * Copyright (c) 2018. $user
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.game.team9.slidingpuzzle.network.bluetooth;

import android.app.ActionBar;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.game.team9.slidingpuzzle.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link BluetoothStatusFragment} interface
 * to handle interaction events.
 * Use the {@link BluetoothStatusFragment} factory method to
 * create an instance of this fragment.
 */
public class BluetoothStatusFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private BluetoothAdapter m_Adapter;
    private BluetoothService m_Service;
    private TextView m_Status;
    private Class m_Class;


    public void Initalize(Class c){m_Class = c;}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        m_Adapter = BluetoothAdapter.getDefaultAdapter();
        if(m_Adapter == null)
        {
            FragmentActivity act = getActivity();



        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bluetooth_status, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        m_Status = view.findViewById(R.id.statusText);
        if(m_Adapter==null)
            m_Status.setText("Bluetooth not supported");
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onStart() {
        super.onStart();

        if(!m_Adapter.isEnabled())
        {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, Constants.REQUEST_ENABLE_BT);
        }
        else if(m_Service == null)
        {
            m_Service = new BluetoothService();
        }
    }


    private void ensureDiscoverable()
    {
        if(m_Adapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE)
        {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    private void setStatus(CharSequence subTitle)
    {
        FragmentActivity activity = getActivity();
        if (null == activity) {
            return;
        }
        final ActionBar actionBar = activity.getActionBar();
        if (null == actionBar) {
            return;
        }
        actionBar.setSubtitle(subTitle);
    }

    private void setStatus(int resId) {
        FragmentActivity activity = getActivity();
        if (null == activity) {
            return;
        }
        final ActionBar actionBar = activity.getActionBar();
        if (null == actionBar) {
            return;
        }
        actionBar.setSubtitle(resId);
    }

    private final Handler m_Handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg)
        {
            FragmentActivity activity = getActivity();
            switch (msg.what)
            {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case Constants.STATE_CONNECTED:
                          /*  Intent intent = new Intent(activity, m_Class);
                            Bundle b = new Bundle();
                            b.putBinder(m_Service);
                            intent.putExtras();
                            startActivity();
                            setStatus(getString(R.string.connected_to, mConnectedDeviceName));
                            mConversationArrayAdapter.clear();*/
                            break;
                        case Constants.STATE_CONNECTING:
                            setStatus(R.string.connecting);
                            break;
                        case Constants.STATE_LISTEN:
                        case Constants.STATE_NONE:
                            setStatus(R.string.offline);
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    case Constants.MESSAGE_READ:
                case Constants.MESSAGE_DEVICE_NAME:
                case Constants.MESSAGE_TOAST:
            }
        }

    };
}
