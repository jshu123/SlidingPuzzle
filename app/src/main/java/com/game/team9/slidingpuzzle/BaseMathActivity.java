/*
 * Copyright (c) 2018. $user
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.game.team9.slidingpuzzle;

import android.animation.ArgbEvaluator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

//import com.game.team9.slidingpuzzle.database.User;
import com.game.team9.slidingpuzzle.database.User;
import com.game.team9.slidingpuzzle.network.Constants;

import java.util.Collections;
import java.util.Random;
import java.util.Stack;

/**
 * Created on: 2/17/18
 * Author: David Hiatt - dhiatt89@gmail.com
 */

public abstract class BaseMathActivity extends AppCompatActivity implements BaseGameView.ISwipeHandler {

    protected final User m_User = new User();
    protected Toast m_Toast;
    protected TextView m_ToastText;

    protected TextView m_Notifier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences a = getSharedPreferences(Constants.PREF, MODE_PRIVATE);
        m_User.setName(a.getString(Constants.PREF_USER, "Nobody"));

        m_ValAnim.setInterpolator(new AccelerateInterpolator());
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast_layout,
                findViewById(R.id.toast_container));
        m_Toast= new Toast(getApplicationContext());
        m_Toast.setView(layout);
        m_ToastText = layout.findViewById(R.id.textView);
    }

    public static byte[] getBoard()
    {
        byte tiles[] = new byte[25];
        Stack<Byte> stack = new Stack<>();
        Random r = new Random();
        for(int i = 0; i < 5; ++i)
        {
            stack.push((byte) 13);
            stack.push((byte) (11 + r.nextInt(2)));
        }
        Collections.shuffle(stack);
        Stack<Byte> num = new Stack<>();
        for(int i = 0; i < 15; ++i)
            num.push((byte)r.nextInt(10));
        num.set(r.nextInt(15), (byte) -1);
        for(int i = 0; i < 25; ++i)
        {
            // if((2 + (i / 5)) % 2 == 0 && (i % 5 == 0 || (i % 5 == 2) || (i % 5 == 4 )))
            //tiles[i] = num.pop();
            if(!stack.empty() && (i == 7 || i == 11 || i == 13 || i == 17))
                tiles[i] = stack.pop();
            else if(stack.empty())
                tiles[i] = num.pop();
            else if(num.empty())
                tiles[i] = stack.pop();
            else
                tiles[i] = r.nextBoolean() ? num.pop() : stack.pop();
        }
        return tiles;
    }

/*
    private static final TimeInterpolator m_DecaySineWave = new TimeInterpolator() {
        @Override
        public float getInterpolation(float input) {
            double raw = Math.sin(3f * input * 2f * Math.PI);
            return (float)(raw * Math.exp(-input * 2f));
        }
    };
*/


    //The value animaitor is used to increase the view height of the toast as it moves, otherwise it will get clipped
  private final ValueAnimator m_ValAnim = ValueAnimator.ofFloat(0f,0f).setDuration(2000);
    protected void badToast(int msg)
    {
   //     if(m_Toast.getView().isShown())
        //    return;


        m_ValAnim.cancel();
        m_ValAnim.setCurrentFraction(0);
        m_ValAnim.removeAllUpdateListeners();
        m_ValAnim.addUpdateListener(animation -> {
            float p = animation.getAnimatedFraction();
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) m_ToastText.getLayoutParams();
            params.height = (int) ((1 + p) * 2000);
            m_ToastText.setAlpha(1f - p);
            m_ToastText.setLayoutParams(params);
        });

        m_ToastText.setText(msg);
        m_ToastText.setTextColor(Color.RED);
        m_Toast.setDuration(Toast.LENGTH_SHORT);
        m_Toast.show();

        m_ToastText.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.shake));
        m_ValAnim.start();

    }

    private final ArgbEvaluator m_Arg = new ArgbEvaluator();
    protected void goodToast(String msg)
        {

            m_ValAnim.cancel();
        m_ValAnim.setCurrentFraction(0);
        m_ValAnim.removeAllUpdateListeners();
        m_ValAnim.addUpdateListener(animation -> {
            float p = animation.getAnimatedFraction();
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) m_ToastText.getLayoutParams();
            params.height = (int) ((1 + p) * 2000);
            m_ToastText.setTextColor((int)m_Arg.evaluate(p, Color.GREEN, Color.BLACK));
            m_ToastText.setAlpha(1f - p + 0.5f);
            m_ToastText.setLayoutParams(params);
        });
        m_ToastText.setTextColor(Color.GREEN);
        m_ToastText.setText(msg);
        m_Toast.setDuration(Toast.LENGTH_LONG);

        m_Toast.show();
        m_ToastText.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.grow));
        m_ValAnim.start();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void onHighscoreClicked(View view)
    {
        Intent intent = new Intent(BaseMathActivity.this, HighScoreActivity.class);
        startActivity(intent);
    }

    protected void onGameEnded(){onGameEnded("Game over.  ");}
    protected abstract void onGameEnded(String msg);

    protected class Equation implements  Comparable<Equation>
    {
        private int Eval(int a, int b, int o)
        {
            if(o == 11)
                return a + b;
            else if(o == 12)
                return a - b;
            else return Integer.MAX_VALUE;
        }

        private int  Pack(int a, int b, int op, int eq)
        {
            return 0xFFFF & (a | (b << 4) | (op << 8) | (eq << 12));
        }
        public byte[] Unpack(){
            return new byte[]{
                    (byte) (0xF &  value),
                    (byte) (0xF &  (value >> 4)),
                    (byte) (0xF &  (value >> 8)),
                    (byte) (0xF &  (value >> 12))
            };
        }

        public final int value;

        public final int reverse;
        public final boolean valid;

        public final int score;
        public Equation(byte[] vals)
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

    public abstract void onSwipeEvent(int[] idx);

    protected static boolean validSwipe(int[] idx)
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

}
