package com.game.team9.slidingpuzzle;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by x on 1/30/18.
 */

public abstract class BaseGameView extends View implements ViewTreeObserver.OnGlobalLayoutListener {

    public static final int BLANK_VALUE = -1;

    private static final Paint m_Background = new Paint();
    private static final Paint m_Grid= new Paint();
    private static final Paint m_BlankCol= new Paint();
    private final Paint m_Forground= new Paint(Paint.ANTI_ALIAS_FLAG);

    private final int[] m_Swipe = new int[5];
    private int m_SwipeIndex = 0;

    protected static final boolean m_Debug = true;


    private final AtomicInteger m_Init = new AtomicInteger(0);
    private final Object m_Lock = new Object();
    private final int[] m_Tiles = new int[25];
    private final List<IBoardChangeListener> m_Listeners = new ArrayList<>();
    private final List<IGameStart> m_Starters = new ArrayList<>();

    private float m_TileWidth;
    private float m_TileHeight;
    private int m_Blank = -1;
    private boolean m_LockedforCPU = false;


    private float m_XFontOffset;
    private float m_YFontOffset;


    private float m_LastClickx;//Debug use only
    private float m_LastClicky;//Debug use only

    private int m_LastIndex = -1;

    static{
        m_Background.setColor(0x929393);
        m_Grid.setStrokeWidth(10);
    }

    protected BaseGameView(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        m_Forground.setColor(0xff0000ff);
        m_Forground.setStyle(Paint.Style.FILL);
        Arrays.fill(m_Swipe, -1);
        ViewTreeObserver ob = getViewTreeObserver();
        ob.addOnGlobalLayoutListener(this);
    }



    /**
     * Must be called to initialize the board
     * @param board
     */
    public void Initialize(int board[])
    {
        Initialize(board,false);
    }

    public void Initialize(int board[], boolean ai)
    {
        if(m_Init.compareAndSet(0, 1)) {
            synchronized (m_Lock) {
                m_LockedforCPU = ai;
                m_Blank = findBlank(board);
                for (int i = 0; i < board.length; i++) {
                    m_Tiles[i] = board[i];
                }
                m_Init.set(2);
            }
        }
    }

    /**
     * Used to convert the integer value in the game board to a text string.
     * @param value at a specific location on the board
     * @return a string representation of that value.  Values can be mapped to any string.
     */
    protected abstract String boardToText(int value);


    /**
     * Override this to get swipe events.
     * @param indexes, always in groups of 5.
     */
    protected void onSwipeEvent(int[] indexes)
    {

    }

    public float getTileWidth(){return m_TileWidth;}
    public float getTileHeight(){return m_TileHeight;}
    public int getBlank(){return m_Blank;}
    public int[] getTiles(){return m_Tiles.clone();}

    @Override
    public final boolean onTouchEvent(MotionEvent event) {
        switch(event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                m_LastClickx = event.getX();
                m_LastClicky = event.getY();
                Reset();
                m_LastIndex = coordsToIndex(m_LastClickx, m_LastClicky);
                m_Swipe[m_SwipeIndex++] = m_LastIndex;
                break;
            case MotionEvent.ACTION_MOVE:
                if(m_SwipeIndex < 5) {
                    m_LastClickx = event.getX();
                    m_LastClicky = event.getY();
                    int idx = coordsToIndex(m_LastClickx, m_LastClicky);
                    if (idx != m_LastIndex) {
                        m_LastIndex = idx;
                        m_Swipe[m_SwipeIndex++] = m_LastIndex;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if(m_SwipeIndex == 1)
                {
                    int idx = coordsToIndex(event.getX(), event.getY());
                    if(m_LastIndex == idx)
                    {
                        ChangeBlank();
                    }
                    Reset();
                }
                else
                {
                    onSwipeEvent(m_Swipe);
                    Reset();
                }
                break;
            case MotionEvent.ACTION_POINTER_ID_MASK:
                if(m_LockedforCPU)
                {
                    m_LastClickx = event.getX();
                    m_LastClicky = event.getY();
                    m_LastIndex = coordsToIndex(m_LastClickx, m_LastClicky);
                    ChangeBlank();
                    Reset();
                }
                else
                    return super.onTouchEvent(event);
                break;

        }
        return true;
    }

    private void ChangeBlank()
    {
        synchronized (m_Lock) {
            if(m_Debug)
                Log.d("MOVE", "Received as Index: " + m_LastIndex + " @ " + m_LastClickx + ", " + m_LastClicky);
            if(m_LastIndex >= 0 && m_LastIndex < 25 && m_Tiles[m_LastIndex] != BLANK_VALUE && canSlide(m_LastIndex))
            {
                if(m_Debug)
                    Log.d("MOVE", "Valid!");

                m_Tiles[m_Blank]= m_Tiles[m_LastIndex];
                m_Tiles[m_LastIndex] = BLANK_VALUE;
                m_Blank = m_LastIndex;

                if(m_Debug) {
                    Log.i("BLANK", "Blank: " + m_Blank);
                    Log.i("BLANK", android.os.Process.myTid() + ", " + android.os.Process.myPid());
                }
                invalidate();

            }
            else
            {

                if(m_Debug) {
                    Log.w("INVALID CLICK", "Index: " + m_LastIndex);
                    invalidate();
                }
                for (IBoardChangeListener b : m_Listeners) {
                    b.Changed(false);
                }
            }
        }
    }


    private void Reset()
    {
        m_SwipeIndex = 0;
        Arrays.fill(m_Swipe, -1);
        m_LastIndex = -1;
    }


    @Override
    protected final void onDraw(Canvas canvas) {
        if (m_Blank < 0) {
            super.onDraw(canvas);
            return;
        }
        if(m_Debug) {
            Log.i("DRAW", "Blank: " + m_Blank);
            Log.i("DRAW", android.os.Process.myTid() + ", " + android.os.Process.myPid());
        }
        canvas.drawRect(0, 0, getWidth(), getHeight(), m_Background);

        for (int i = 0; i < 6; ++i) {
            canvas.drawLine(0, i * m_TileHeight, getWidth(), i * m_TileHeight, m_Grid);
            canvas.drawLine(i * m_TileWidth, 0, i * m_TileWidth, getHeight(), m_Grid);
        }
        for (int i = 0; i < 25; ++i) {
            float rx = (i % 5) * m_TileWidth;
            float ry = (i > 0 ? i / 5 : 0) * m_TileHeight;
            if (m_Tiles[i] != BLANK_VALUE) {
                canvas.drawText(boardToText(m_Tiles[i]), rx + m_XFontOffset, ry + m_YFontOffset,        m_Forground);
            } else
                canvas.drawRect(rx, ry, rx + m_TileWidth, ry + m_TileHeight,
                        m_BlankCol);
        }

        if(m_Debug)
       canvas.drawCircle(m_LastClickx, m_LastClicky, 10.0f, m_Forground);
        for (IBoardChangeListener b : m_Listeners) {
            b.Changed(true);
        }

    }

    /**
     * Use this to receive updates anytime the board changes
     * @param b
     */
    public void AttachChangeListener(IBoardChangeListener b)
    {
        m_Listeners.add(b);
    }

    public void AttachStartListener(IGameStart b)
    {
        m_Starters.add(b);
    }

    @Override
    /**
     * Called internally only.
     */
    public void onGlobalLayout()
    {
        synchronized (m_Lock) {
            ViewTreeObserver ob = getViewTreeObserver();
            ob.removeOnGlobalLayoutListener(this);
            m_TileHeight = getHeight() / 5;
            m_TileWidth = getWidth() / 5;
            m_Forground.setTextSize(m_TileHeight * 0.75f);
            m_Forground.setTextScaleX(m_TileWidth / m_TileHeight);
            m_Forground.setTextAlign(Paint.Align.CENTER);
            m_XFontOffset = m_TileWidth / 2;
            Paint.FontMetrics fm = m_Forground.getFontMetrics();
            m_YFontOffset = (m_TileHeight / 2) - (fm.ascent + fm.descent) / 2;
            while(m_Init.get() != 2)
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            invalidate();
            for (IGameStart st : m_Starters) {
                st.OnStart();
            }
            m_Starters.clear();
        }
    }

    private boolean canSlide(int idx)
    {
        //check up
        if(idx < 20 && m_Tiles[idx + 5] == BLANK_VALUE)
            return true;
        //check down
        if(idx > 4 && m_Tiles[idx - 5] == BLANK_VALUE)
            return true;
        //check left
        if(idx % 5 > 0 && m_Tiles[idx - 1] == BLANK_VALUE)
            return true;
        //check right
        if(idx % 5 < 4 && m_Tiles[idx + 1] == BLANK_VALUE)
            return true;
        return false;

    }

    protected int coordsToIndex(float x, float y)
    {
        int ix = (int)(x / m_TileWidth);
        int iy = (int)(y / m_TileHeight);
        return iy * 5 + ix;
    }


    public static int findBlank(int[] array)
    {
        for(int i = 0; i < array.length; ++i)
        {
            if(array[i] == BLANK_VALUE)
                return i;
        }
        return -1;
    }

    public interface IBoardChangeListener {
        void Changed(boolean b);
    }

    public interface IGameStart
    {
        void OnStart();
    }
}
