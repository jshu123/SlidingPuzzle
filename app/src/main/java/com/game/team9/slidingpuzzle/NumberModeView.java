package com.game.team9.slidingpuzzle;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on: 1/30/18
 *     Author: David Hiatt - dhiatt89@gmail.com
 */

public class NumberModeView extends BaseGameView implements BaseGameView.IBoardChangeListener {

    private final List<IBoardSolvedListener> m_OnSolved = new ArrayList<>();


    public NumberModeView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public void AttachSolveListener(IBoardSolvedListener b)
    {
        if(!m_OnSolved.contains(b))
            m_OnSolved.add(b);
    }

    @Override
    protected String boardToText(int value) {
        return Integer.toString(value);
    }

    @Override
    public void Changed(boolean b) {
        if(!m_OnSolved.isEmpty() && checkSolved(getTiles()))
        {
            int id = getId();
            //m_OnSolved.forEach((c)->c.Solved(id));
            for (IBoardSolvedListener boardSolvedListener : m_OnSolved) {
                boardSolvedListener.Solved(id);
            }
        }
    }

    public static boolean checkSolved(@NonNull int env[])
    {
        if(env[env.length - 1] != 0)
            return false;
        for(int i = 0; i < env.length; ++i)
            if(env[i] != i + 1)
                return false;
        return true;
    }

    public interface IBoardSolvedListener
    {
        void Solved(int id);
    }
}
