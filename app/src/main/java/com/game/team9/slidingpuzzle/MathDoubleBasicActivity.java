package com.game.team9.slidingpuzzle;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.HashSet;
import java.util.Set;

public class MathDoubleBasicActivity extends BaseMathOnlineActivity {

    private final Set<Equation> m_Host = new HashSet<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        Log.i("MATH", "STARTING BASIC");
    }

    @Override
    protected void HandleMove(Equation q) {
        m_ClientScore += q.score;
        runOnUiThread(() -> m_ClientScoreView.setText(String.valueOf(m_ClientScore)));
    }

    @Override
    protected void onLocalGameEnded() {
        m_Host.clear();
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
               if(m_Host.add(eq))
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
