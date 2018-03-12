package com.game.team9.slidingpuzzle;

import android.graphics.Color;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.widget.Toast;

import java.util.HashSet;
import java.util.Set;

public class MathDoubleCuthroatActivity extends BaseMathOnlineActivity {


    private final Set<Equation> m_Host = new HashSet<>();
    private final Set<Equation> m_Client = new HashSet<>();

    @Override
    protected void HandleMove(Equation q) {
        m_ClientScore += q.score;
        m_Client.add(q);
        runOnUiThread(() -> m_ClientScoreView.setText(Integer.toString(m_ClientScore)));
    }

    @Override
    public void onSwipeEvent(int[] idx) {
        if(validSwipe(idx))
        {
            byte[] tiles = m_Game.getTiles();
            for(int i = 0; i < 5; ++i)
            {
                idx[i] = tiles[idx[i]];
            }
            Equation eq = new Equation(idx);
            if(eq.valid)
            {
                if(m_Client.contains(eq))
                {
                    m_Toast.setDuration(Toast.LENGTH_SHORT);
                    m_ToastText.setTextColor(Color.RED);
                    badToast(R.string.taken);
                }
                else if(m_Host.add(eq))
                {
                    m_HostScore += eq.score;
                    m_HostScoreView.setText(Integer.toString(m_HostScore));
                    goodToast(eq.score + (eq.score == 1 ? "point!" : " points!"));
                    onMove(eq.Unpack());
                }
                else
                {
                    badToast(R.string.no_points);
                }
            }
            else
            {
                badToast(R.string.invalid_eq);
            }
        }
    }
}
