package com.game.team9.slidingpuzzle;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import java.util.HashSet;
import java.util.Set;

/**
 * Created on: 1/30/18
 *     Author: David Hiatt - dhiatt89@gmail.com
 */

public class MathModeView extends BaseGameView {


    public MathModeView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
   /* private final Set<Equation> m_Hist = new HashSet<>();

    private final Set<IScored> m_OnScored = new HashSet<>();



    public void AttachScoreListener(IScored s)
    {
        m_OnScored.add(s);
    }

    @Override
    protected void onSwipeEvent(int[] indexes) {

        if(validSwipe(indexes))
        {
            int[] tiles = getTiles();
            for(int i = 0; i < 5; ++i)
            {
                indexes[i] = tiles[indexes[i]];
            }
            Equation eq = new Equation(indexes);
            ScoreType type = ScoreType.Invalid;
            if(eq.valid)
            {
                if(m_Hist.add(eq)) {
                   type = ScoreType.Valid;
                }
                else
                {
                    type = ScoreType.Taken;
                }
            }
            for (IScored scored : m_OnScored) {
                scored.Score(eq.score, type);
            }

        }
    }*/

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

   /* private static boolean validSwipe(int[] idx)
    {
        if(idx[0] == -1)
            return false;
        Direction d = Direction.ROOT;
        for(int i = 1; i < 5; ++i)
        {
            if(idx[i] == -1)
                return false;
             if(idx[i - 1] + 1 == idx[i]) //RIGHT
             {
                 if(d == Direction.ROOT)
                     d = Direction.Right;
                 if(d != Direction.Right)
                     return false;
             }
             else if(idx[i - 1] - 1 == idx[i])
             {
                 if(d == Direction.ROOT)
                     d = Direction.Left;
                 if(d != Direction.Left)
                     return false;
             }
             else if(idx[i - 1] - 5 == idx[i])
             {
                 if(d == Direction.ROOT)
                     d = Direction.Down;
                 if(d != Direction.Down)
                     return false;
             }
             else if(idx[i - 1] + 5 == idx[i])
             {
                 if(d == Direction.ROOT)
                     d = Direction.Up;
                 if(d != Direction.Up)
                     return false;
             }
             else
                 return false;
        }
        return true;
    }

    private static int Eval(int a, int b, int o)
    {
        if(o == 11)
            return a + b;
        else if(o == 12)
            return a - b;
        else return Integer.MAX_VALUE;
    }

    private static int  Pack(int a, int b, int op, int eq)
    {
        return 0xFFFF & (a | (b << 4) | (op << 8) | (eq << 12));
    }

    private class Equation implements  Comparable<Equation>
    {
        public final int value;

        public final int reverse;
        public final boolean valid;

        public final int score;
        public Equation(int[] vals)
        {
            if(vals[1] == 13)
            {
                value = Pack(vals[4],vals[2], vals[3],vals[0]);
                if(Eval(vals[2], vals[4], vals[3]) == vals[0])
                    reverse = Pack(vals[2], vals[4], vals[3], vals[0]);
                else
                    reverse = value;
            }
            else if(vals[3] == 13)
            {
                value = Pack(vals[0],vals[2], vals[1],vals[4]);
                if(Eval(vals[2], vals[0], vals[1]) == vals[4])
                    reverse = Pack(vals[2], vals[0], vals[1], vals[4]);
                else
                    reverse = value;
            }else
            {
                value = reverse = 0;
            }

            score = (0xF & (value >> 12));
            valid = Eval(0xF &  value, 0xF & (value >> 4), 0xF & (value >> 8)) == (0xF & (value >> 12))
                    && Eval(0xF &  reverse, 0xF & (reverse >> 4), 0xF & (reverse >> 8)) == (0xF & (reverse >> 12));
        }
        public Equation(int a, int b, int operator, int eq)
        {
            value = Pack(a, b, operator, eq);
            if(Eval(b, a, operator) == eq)
                reverse = Pack(b, a, operator, eq);
            else reverse = value;
            valid = Eval(0xF &  value, 0xF & (value >> 4), 0xF & (value >> 8)) == (0xF & (value >> 12))
                    && Eval(0xF &  reverse, 0xF & (reverse >> 4), 0xF & (reverse >> 8)) == (0xF & (reverse >> 12));
            score = (0xF & (value >> 12));
        }

        @Override
        public boolean equals(@Nullable Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Equation equation = (Equation) o;

            if(!valid && !equation.valid)
                return true;
            return value == equation.value || value == equation.reverse;
        }

        @Override
        public int hashCode() {
            int result = value;
            result = 31 * result + reverse;
            result = 31 * result + (valid ? 1 : 0);
            return result;
        }

        @Override
        public int compareTo(@NonNull Equation equation) {
            return Math.min(Math.abs(value - equation.value), Math.abs(reverse - equation.value));
        }
    }

    private enum Direction
    {
        ROOT (-1),
        Up(0),
        Left(1),
        Right(2),
        Down(3);

        Direction(int v){value = v;}

        private final int value;

    }

    public interface IScored
    {
        void Score(int score, ScoreType type);
    }

    public enum ScoreType
    {
        Valid,
        Invalid,
        Taken
    }*/
}
