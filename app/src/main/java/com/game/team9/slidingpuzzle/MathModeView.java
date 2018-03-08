package com.game.team9.slidingpuzzle;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Created on: 1/30/18
 *     Author: David Hiatt - dhiatt89@gmail.com
 */

public class MathModeView extends BaseGameView {


    public MathModeView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected String boardToText(int value) {
        if(value < 10)
            return Integer.toString(value);
        switch(value)
        {
            case 11:
                return "+";
            case 12:
                return "-";
            case 13:
                return "=";
        }
        return "?";
    }
}
