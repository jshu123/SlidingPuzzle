/*
 * Copyright (c) 2018. $user
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.game.team9.slidingpuzzle;


import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.game.team9.slidingpuzzle.network.BluetoothService;
import com.game.team9.slidingpuzzle.network.Constants;
import com.game.team9.slidingpuzzle.network.IPacketHandler;
import com.game.team9.slidingpuzzle.network.Packet;
import com.game.team9.slidingpuzzle.network.PeerInfo;
import com.game.team9.slidingpuzzle.network.PeerListAdapter;
import com.game.team9.slidingpuzzle.network.TestNetwork;
import com.game.team9.slidingpuzzle.network.WifiService;

import static com.game.team9.slidingpuzzle.network.Constants.ACTION_BLUE_DISABLED;
import static com.game.team9.slidingpuzzle.network.Constants.ACTION_BLUE_UNSUPPORTED;
import static com.game.team9.slidingpuzzle.network.Constants.ACTION_WIFI_UNSUPPORTED;
import static com.game.team9.slidingpuzzle.network.Constants.EXTRA_MODE;
import static com.game.team9.slidingpuzzle.network.Constants.EXTRA_REASON;
import static com.game.team9.slidingpuzzle.network.Constants.REQUEST_ENABLE_BT;

public class MathOnlineDiscoveryActivity extends AppCompatActivity implements IPacketHandler {

    private BluetoothAdapter m_Bluetooth;
    private PeerListAdapter m_Adapter;
    private ListView m_DevList;

    private TextView m_Status;

    private Class m_Class;  //Determines which game type to call after connecting

    private ToggleButton m_Wifi;
    private ToggleButton m_Blue;

    private boolean m_WifiBound;
    private boolean m_BlueBound;
    private boolean m_BlueWaiting;

    private final Object m_Lock = new Object();

    private static final IntentFilter m_LocalFilter = new IntentFilter();

    private final TestNetwork s_Test = new TestNetwork();

    static {
        m_LocalFilter.addAction(ACTION_BLUE_UNSUPPORTED);
        m_LocalFilter.addAction(ACTION_WIFI_UNSUPPORTED);

    }
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_math_online_discovery);
        Intent intent = getIntent();
        m_Adapter = new PeerListAdapter(this, R.layout.fragment_dev_detail);

        AppController.addHandler(this);

        m_Class = intent.getIntExtra(EXTRA_MODE, -1) == 1 ? MathDoubleCuthroatActivity.class : MathDoubleBasicActivity.class;

        m_Status = findViewById(R.id.statusText);
        m_Wifi = findViewById(R.id.wifiToggle);
        m_Blue = findViewById(R.id.blueToggle);

        m_DevList = findViewById(R.id.listView);
        m_DevList.setAdapter(m_Adapter);

        registerReceiver(m_LocalReceiver, m_LocalFilter);
        m_Bluetooth = BluetoothAdapter.getDefaultAdapter();

        if(m_Bluetooth == null)
        {
            m_Status.setText("Bluetooth not supported.  ");
            m_Blue.setEnabled(false);
        }

        if(AppController.DEBUG)
        {
            //Fake it!
            s_Test.start();
           // s_Test.MessageQueue.add(Packet.AcquirePacket("127.0.0.1", Packet.Header.REQUEST, (byte)0));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        synchronized (m_Lock) {
            if(m_BlueWaiting && requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK)
            {
                startService(new Intent(this, BluetoothService.class));
                m_BlueBound = true;
            } else {
                m_Blue.setChecked(false);
            }
            m_BlueWaiting = false;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void setWifi(boolean status)
    {
        synchronized (m_Lock) {
            if (m_WifiBound != status) {
                if (m_WifiBound) {
                    stopService(new Intent(this, WifiService.class));
                } else {
                    startService(new Intent(this, WifiService.class));
                }
                m_WifiBound = status;
            }
        }
    }

    private void setBlue(boolean status)
    {
        synchronized (m_Lock) {
            if (m_BlueBound != status) {
                if (m_BlueBound) {
                    stopService(new Intent(this, BluetoothService.class));
                    m_BlueWaiting = false;
                } else {
                    if(!m_Bluetooth.isEnabled()) {
                        m_BlueWaiting = true;
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                    }
                    else
                    {
                        startService(new Intent(this, BluetoothService.class));
                    }
                }

                m_BlueBound = status && !m_BlueWaiting;
            }
        }
    }


    private void unbindAll()
    {
        synchronized (m_Lock) {
            if (m_WifiBound)
            {
                stopService(new Intent(this, WifiService.class));
            }
            if (m_BlueBound)
            {
                stopService(new Intent(this, BluetoothService.class));
            }
            m_WifiBound = m_BlueBound = false;
        }
    }
    private void bindAll()
    {
        if(m_Wifi.isEnabled())
            setWifi(m_Wifi.isChecked());
        if(m_Blue.isEnabled())
            setBlue(m_Blue.isChecked());
    }

    public void onToggle(View view)
    {
        /*bindAll();*/
    }


    @Override
    protected void onResume() {
        bindAll();
        registerReceiver(m_LocalReceiver, m_LocalFilter);
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(m_LocalReceiver);
        unbindAll();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }


    @Override
    public int Priority() {
        return Constants.PRIORITY_DISCOVER_ACT;
    }

    @Override
    public int compareTo(@NonNull IPacketHandler o) {
        return Priority() - o.Priority();
    }

    @Override
    public boolean handleData(Packet p) {
        switch(p.Type) {

            case FREE:
                break;
            case REQUEST:
            {
               // runOnUiThread(()->
              //  m_Adapter;
                //m_DevList.invalidate();
               /* DialogInterface.OnClickListener clicker = new DialogInterface.OnClickListener() {
                    public void onClick(@NonNull DialogInterface dialog, int which) {
                        switch (which) {
                            default:
                            case AlertDialog.BUTTON_NEGATIVE:
                                AppController.SendData(Packet.AcquirePacket(p.Source, Packet.Header.QUIT));
                                break;
                            case AlertDialog.BUTTON_POSITIVE:
                                AppController.SendData(Packet.AcquirePacket(p.Source, Packet.Header.ACCEPT));
                                Intent intent;
                                if (p.Data[0] == 0)
                                    intent = new Intent(MathOnlineDiscoveryActivity.this, MathDoubleCuthroatActivity.class);
                                else
                                    intent = new Intent(MathOnlineDiscoveryActivity.this, MathDoubleBasicActivity.class);
                                intent.putExtra(Constants.EXTRA_ID, p.Source);
                                intent.putExtra(Constants.EXTRA_IS_HOST, false);
                                intent.putExtra(Constants.EXTRA_CUT, p.Data[0] == 0);
                                startActivity(intent);
                        }
                    }
                };
                runOnUiThread(new Runnable() {
                                  @Override
                                  public void run() {
                                      AlertDialog alertDialog = new AlertDialog.Builder(MathOnlineDiscoveryActivity.this).create();
                                      alertDialog.setTitle("New request");
                                      alertDialog.setMessage(p.Source + " wants to play " + (p.Data[0] == 0 ? "cutthroat" : "basic") + " mode.");
                                      alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Accept", clicker);
                                      alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Reject", clicker);
                                      alertDialog.show();
                                  }
                              });*/

        }
            //    p.Free();
            return false;
            case ACCEPT:/* {

                Intent intent = new Intent(MathOnlineDiscoveryActivity.this, m_Class);
                intent.putExtra(Constants.EXTRA_ID, p.Source);
                intent.putExtra(Constants.EXTRA_IS_HOST, true);
                intent.putExtra(Constants.EXTRA_CUT, m_Class == MathDoubleCuthroatActivity.class);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        m_Adapter.inboundRequest(p.Source, 0);
                AlertDialog alertDialog = new AlertDialog.Builder(MathOnlineDiscoveryActivity.this).create();
                alertDialog.setTitle("Invite accepted!");
                alertDialog.setMessage("Click to start.");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(@NonNull DialogInterface dialog, int which) {
                                dialog.dismiss();
                                startActivity(intent);
                                finish();
                            }
                        });
                alertDialog.show();
                    }
                });
            }
            p.Free();*/
            return false;
            case QUIT:
            {
                PeerInfo i = PeerInfo.Retrieve(p.Source);
                i.Update(PeerInfo.Status.AVAILABLE);
            }
                runOnUiThread(()-> Toast.makeText(this, p.Source + " has rejected your invite.", Toast.LENGTH_SHORT));
                p.Free();
                return true;
            case MOVE:
                break;
            case TIME:
                break;
            case INIT:
                break;
        }
        return false;
    }

    public void LaunchGame(Intent intent)
    {
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AppController.removeHandler(this);
        m_Adapter.Teardown();
       // s_Test.Close();
    }


    private final BroadcastReceiver m_LocalReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action != null)
            {
                switch (action)
                {
                    case ACTION_BLUE_DISABLED:
                        m_Blue.setChecked(false);
                        bindAll();
                        break;
                    case ACTION_BLUE_UNSUPPORTED:
                        m_Blue.setEnabled(false);
                        m_Status.setText(m_Status.getText() + "Bluetooth not supported.  ");
                        break;
                    case ACTION_WIFI_UNSUPPORTED:
                        m_Wifi.setEnabled(false);
                        m_Status.setText(m_Status.getText() + intent.getStringExtra(EXTRA_REASON));
                }
            }
        }
    };
}
