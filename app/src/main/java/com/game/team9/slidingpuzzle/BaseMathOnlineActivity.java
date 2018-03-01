/*
 * Copyright (c) 2018. $user
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.game.team9.slidingpuzzle;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.game.team9.slidingpuzzle.database.HighScoreDatabase;
import com.game.team9.slidingpuzzle.database.User;
import com.game.team9.slidingpuzzle.network.Constants;
import com.game.team9.slidingpuzzle.network.IPacketHandler;
import com.game.team9.slidingpuzzle.network.Packet;
import com.game.team9.slidingpuzzle.network.PeerInfo;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class BaseMathOnlineActivity extends BaseMathActivity implements Chronometer.OnChronometerTickListener, IPacketHandler {

    private static final String TAG = "OnlineMode";
    private boolean m_Server;
    private String m_Id;
    private Chronometer m_Timer;

    protected TextView m_HostScoreView;
    protected TextView m_ClientScoreView;
    protected MathModeView m_Game;
    protected final User m_Opponent = new User();

    protected int m_HostScore;
    protected int m_ClientScore;

    private byte[] m_Tiles;

    private boolean m_Started = false;
    private boolean m_Closed = false;

    private AtomicBoolean m_Finalized = new AtomicBoolean(false);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();

        m_Id = intent.getStringExtra(Constants.EXTRA_DEVICE);
        m_Opponent.setName(intent.getStringExtra(Constants.EXTRA_ID));
        setContentView(R.layout.activity_math_online);
        AppController.addHandler(this);
        m_Timer = findViewById(R.id.chronometer);
        m_HostScoreView = findViewById(R.id.hostScoreText);
        m_ClientScoreView = findViewById(R.id.clientScoreText);
        m_ClientScoreView.setText("0");
        m_HostScoreView.setText("0");
        TextView t = findViewById(R.id.hostNameText);
        t.setText(m_User.getName());
        t = findViewById(R.id.clientNameText);
        t.setText(m_Opponent.getName());
        m_Game = findViewById(R.id.playerView);
        m_Server = intent.getBooleanExtra(Constants.EXTRA_IS_HOST, false);
        AppController.addHandler(this);
        PeerInfo info = PeerInfo.Retrieve(m_Id);
        info.Update(PeerInfo.Status.ACTIVE);
        if(m_Server)
        {
            m_Tiles = getBoard();
            AppController.SendData(Packet.AcquirePacket(m_Id, Packet.Header.INIT, 25, m_Tiles));
            startGame();
            m_Timer.start();
            m_Timer.setOnChronometerTickListener(this);

        }
    }

    public void onChronometerTick(Chronometer var)
    {
        AppController.SendData(Packet.AcquirePacket(m_Id, Packet.Header.TIME, var.getText().toString().getBytes()));
    }


    private synchronized void startGame()
    {
        if(m_Started != true) {
            m_Started = true;
            ProgressBar bar = findViewById(R.id.progressBar);
            bar.setVisibility(View.GONE);
            m_Game.Initialize(m_Tiles, this);
            m_Game.setVisibility(View.VISIBLE);
        }
    }

    public boolean handleData(Packet p)
    {
        Log.i(TAG, "Handling " + p);
        switch(p.Type)
        {

            case INIT:
                if(m_Server)
                {
                    Log.w(TAG, "Received board from nonhost");
                }
                else if(m_Started)
                {
                    Log.w(TAG, "Received duplicate board from host");
                }
                else
                {
                    if(p.Length != 25)
                        Log.e(TAG, "Received invalid board size - " + p);
                    m_Tiles = new byte[p.Length];
                    System.arraycopy(p.Data,0,m_Tiles, 0, p.Length);
                    runOnUiThread(this::startGame);

                }
                p.Free();
                return true;
            case FREE:
                break;
            case REQUEST:
                Log.e(TAG,"Received request.");
              //  AppController.SendData(Packet.AcquirePacket(p.Source, Packet.Header.QUIT));
                //p.Free();
                return false;
            case ACCEPT:
                Log.e(TAG,"Received accept.");
                //p.Free();
                return false;
            case QUIT:
                m_Closed = true;
                p.Free();
                onGameEnded(m_Id + " has ended the game.  ");
                return true;
            case MOVE:HandleMove(new Equation(p.Data));
                p.Free();
                return true;
            case TIME:
                if(m_Server)
                {
                    Log.w(TAG, "Received time from nonhost");
                }
                else
                {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            m_Timer.setText(new String(p.Data,0, p.Length));
                        }
                    });
                }
                p.Free();
                return true;
        }
        return false;
    }

    protected abstract void HandleMove(Equation q);


    public void onGiveupClicked(View view)
    {
        AppController.SendData(Packet.AcquirePacket(m_Id, Packet.Header.QUIT));
        m_Closed=true;
        onGameEnded();
    }


    protected void onGameEnded(String msg)
    {
        if(!m_Finalized.compareAndSet(false, true))
        {
            Log.e(TAG, "Attempted to end the game twice");
            return;
        }
        m_User.setScore(m_HostScore);
        m_Opponent.setScore(m_ClientScore);

        if(m_HostScore > m_ClientScore)
            msg += "You are the winner!  ";
        else if(m_ClientScore > m_HostScore)
            msg += m_Opponent.getName() + " is the winner.  ";
        else
            msg += "The game was a tie.  ";
        //Update opponent score in database first.
        HighScoreDatabase.updateUser(m_Opponent);
        List<User> top = HighScoreDatabase.getTop();
        HighScoreDatabase.updateUser(m_User);
        //top will not contain our score, so we can see if beat anyone.

        Intent intent = new Intent(BaseMathOnlineActivity.this, HighScoreActivity.class);
        for (User user : top) {
            if(m_ClientScore > user.getScore())
            {
                intent.putExtra("NewScore", true);
                msg += "You have set a new highscore!";
                break;
            }
        }

        final String rmsg = msg;
        runOnUiThread(()->{
            AlertDialog alertDialog = new AlertDialog.Builder(BaseMathOnlineActivity.this).create();
            alertDialog.setTitle("Game is over");
            alertDialog.setMessage(rmsg);
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(@NonNull DialogInterface dialog, int which) {
                            dialog.dismiss();
                            startActivity(intent);
                            finish();
                        }
                    });
            alertDialog.show();
        });
    }

    /**
     * Sends the move to the currently connected client.
     * The move should be validated by deriving classes before being sent.
     * @param moves
     */
    protected void onMove(byte[] moves)
    {
        AppController.SendData(Packet.AcquirePacket(m_Id, Packet.Header.MOVE, moves));
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(!m_Closed)
            AppController.SendData(Packet.AcquirePacket(m_Id, Packet.Header.QUIT));
        AppController.removeHandler(this);
        AppController.endGame(m_Id);
        if(m_Timer !=null)
            m_Timer.stop();
        if(m_Game != null)
            m_Game.Destroy();
        PeerInfo info = PeerInfo.Retrieve(m_Id);
        info.Update(PeerInfo.Status.AVAILABLE);
    }

    @Override
    public int Priority() {
        return Constants.PRIORITY_BASEONLINE;
    }

    @Override
    public int compareTo(@NonNull IPacketHandler o) {
        return Priority() - o.Priority();
    }
}
