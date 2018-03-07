package com.game.team9.slidingpuzzle;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.EmbossMaskFilter;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Handler;

/**
 * Created on: 1/30/18
 *     Author: David Hiatt - dhiatt89@gmail.com
 */

public abstract class BaseGameView extends View implements ViewTreeObserver.OnGlobalLayoutListener{

    public static final int BLANK_VALUE = -1;
    private static final String TAG = "GameView";

    private static final Paint m_TilePaint = new Paint();
    private static final Paint m_Grid= new Paint();
    private final Paint m_TextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final int[] m_Swipe = new int[5];
    private int m_SwipeIndex = 0;

    private final android.os.Handler m_Handler = new android.os.Handler();

    private final List<IBoardChangeListener> m_Listeners = new ArrayList<>();
    private final List<IGameStart> m_Starters = new ArrayList<>();
    private final AtomicInteger m_Init = new AtomicInteger(0);
    private final Object m_Lock = new Object();
    private final byte[] m_Tiles = new byte[25];


    private float m_TileWidth;
    private float m_TileHeight;
    private int m_Blank = -1;
    private int m_LastBlank = -1;
    private int m_LastIndex = -1;

    private float m_StartX;
    private float m_StartY;
    private float m_CurX;
    private float m_CurY;

    private boolean m_Reverse;

    private float m_Endx;
    private float m_Endy;

    private long m_TimeNow;
    private long m_TimeStart;

    private boolean m_LockedforCPU = false;
    private boolean m_Paused = false;


    private float m_XFontOffset;
    private float m_YFontOffset;


    private float m_LastClickx;//Debug use only
    private float m_LastClicky;//Debug use only



    private Bitmap m_TileMap;
    private Bitmap m_TileSelect;
    private final AtomicBoolean m_Animating = new AtomicBoolean(false);

    private ISwipeHandler m_Swiper;

    static {
        m_TilePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        m_TilePaint.setStrokeWidth(1);
        m_TilePaint.setStrokeCap(Paint.Cap.ROUND);
        m_TilePaint.setStrokeJoin(Paint.Join.BEVEL);
    }

    protected BaseGameView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        //m_Activity = (Activity)context;
        Resources res = getResources();
        m_TilePaint.setColor(res.getColor(R.color.colorTiles));
        //m_BlankCol.setColor(res.getColor(R.color.colorBlank));
        m_TextPaint.setColor(res.getColor(R.color.colorText));
        m_TextPaint.setStyle(Paint.Style.FILL);
        m_TextPaint.setAntiAlias(true);
        m_TextPaint.setTypeface(Typeface.SANS_SERIF);
        //m_TextPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        m_TilePaint.setMaskFilter(new EmbossMaskFilter(
                        new float[]{1,5,1}, // direction of the light source
                        0.5f, // ambient light between 0 to 1
                        100, // specular highlights
                        0 // blur before applying lighting
                ));
        m_TextPaint.setShadowLayer(1, 2, 2, 0xff000000);

        Arrays.fill(m_Swipe, -1);
        ViewTreeObserver ob = getViewTreeObserver();
        ob.addOnGlobalLayoutListener(this);
    }



    /**
     * Must be called to initialize the board
     * @param board
     */
    public void Initialize(@NonNull byte board[], ISwipeHandler handler)
    {
        Initialize(board,handler, false);
    }

    private final AtomicBoolean m_TileInit = new AtomicBoolean(false);
    private final AtomicBoolean m_TileFinished = new AtomicBoolean(false);

    public void Initialize(@NonNull byte board[], ISwipeHandler handler, boolean ai)
    {
        if(m_TileInit.compareAndSet(false, true))
        { synchronized (m_Lock) {
                m_Swiper = handler;
                m_LockedforCPU = ai;
                m_Blank = findBlank(board);
                System.arraycopy(board, 0, m_Tiles, 0, board.length);
                if(m_Init.incrementAndGet() == 2)
                {
                    finishInit();
                }
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
    public byte[] getTiles(){return m_Tiles.clone();}

    @Override
    public final boolean onTouchEvent(@NonNull MotionEvent event) {
        if(m_Paused)
            return true;
        Log.i("MOTION", "= " + event.getAction());
        switch(event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                if(m_LockedforCPU)
                    return true;
                m_LastClickx = event.getX();
                m_LastClicky = event.getY();
                Reset();
                m_LastIndex = coordsToIndex(m_LastClickx, m_LastClicky);
                m_Swipe[m_SwipeIndex++] = m_LastIndex;
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                if(m_LockedforCPU)
                    return true;
                if(m_SwipeIndex < 5) {
                    m_LastClickx = event.getX();
                    m_LastClicky = event.getY();
                    int idx = coordsToIndex(m_LastClickx, m_LastClicky);
                    if (idx != m_LastIndex) {
                        m_LastIndex = idx;
                        m_Swipe[m_SwipeIndex++] = m_LastIndex;
                        invalidate();
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if(m_LockedforCPU)
                    return true;
                if(m_SwipeIndex == 1)
                {
                    int idx = coordsToIndex(event.getX(), event.getY());
                    boolean s = m_LastIndex == idx;
                    if(s)
                    {
                        ChangeBlank();
                    }
                    Reset();
                    invalidate();
                }
                else
                {
                    if(m_Swiper != null)
                        m_Swiper.onSwipeEvent(m_Swipe);
                    Reset();
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                Reset();
                invalidate();
                break;

            //noinspection deprecation
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
            if(AppController.DEBUG)
                Log.d("MOVE", "Received as Index: " + m_LastIndex + " @ " + m_LastClickx + ", " + m_LastClicky);
            if(m_LastIndex >= 0 && m_LastIndex < 25 && m_Tiles[m_LastIndex] != BLANK_VALUE && canSlide(m_LastIndex))
            {
                if(AppController.DEBUG)
                    Log.d("MOVE", "Valid!");

                m_Tiles[m_Blank]= m_Tiles[m_LastIndex];


                m_StartX = m_CurX = (m_LastIndex % 5) * m_TileWidth;
                m_StartY = m_CurY = (m_LastIndex > 0 ? m_LastIndex / 5 : 0) * m_TileHeight;

                m_Endx = (m_Blank % 5) * m_TileWidth;
                m_Endy = (m_Blank > 0 ? m_Blank / 5 : 0) * m_TileHeight;

                m_Reverse = m_Blank < m_LastIndex;
                m_Tiles[m_LastIndex] = BLANK_VALUE;

                m_LastBlank = m_Blank;
                m_Blank = m_LastIndex;

                m_TimeStart = System.currentTimeMillis();

                if(AppController.DEBUG) {
                    Log.i("BLANK", "Blank: " + m_Blank);
                    Log.i("BLANK", android.os.Process.myTid() + ", " + android.os.Process.myPid());
                }

                m_Animating.set(true);
                invalidate();
                //m_Thread.Resume();
            }
            else
            {

                if(AppController.DEBUG) {
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

    private boolean swipeContains(int idx)
    {
        for (int i : m_Swipe) {
            if(i == idx)
                return true;
        }
        return false;
    }


    @Override
    protected final void onDraw(@NonNull Canvas canvas) {
        if (m_Blank < 0) {
            super.onDraw(canvas);
            return;
        }
      /*  if(m_Debug) {
            Log.i("DRAW", "Blank: " + m_Blank);
            Log.i("DRAW", android.os.Process.myTid() + ", " + android.os.Process.myPid());
        }*/
        canvas.drawRect(0, 0, getWidth(), getHeight(), m_Grid);
        /*for (int i = 0; i < 6; ++i) {
            canvas.drawLine(0, i * m_TileHeight, getWidth(), i * m_TileHeight, m_Grid);
            canvas.drawLine(i * m_TileWidth, 0, i * m_TileWidth, getHeight(), m_Grid);
        }*/
        boolean a = m_Animating.get();
        for (int i = 0; i < 25; ++i) {
            if(a && (i == m_Blank || i == m_LastBlank))
                continue;
            if (m_Tiles[i] != BLANK_VALUE) {
                float rx = (i % 5) * m_TileWidth;
                float ry = (i > 0 ? i / 5 : 0) * m_TileHeight;
                if(swipeContains(i))
                        canvas.drawBitmap(m_TileSelect, rx, ry, m_TilePaint);
                    else
                        canvas.drawBitmap(m_TileMap, rx, ry, m_TilePaint);
                //canvas.drawRect(rx + (m_GridWidth / 2), ry + (m_GridWidth / 2), rx + m_TileWidth - (m_GridWidth / 2), ry + m_TileHeight - (m_GridWidth / 2), m_TilePaint);
                canvas.drawText(boardToText(m_Tiles[i]), rx + m_XFontOffset, ry + m_YFontOffset, m_TextPaint);
            } else
            {
                Log.e(TAG, "Moving tile to a nonblank tile!!");
            }
        }

        m_TimeNow =  System.currentTimeMillis();
        if(a)
        {
            float frac = (float)curFrac();
            m_CurX = m_StartX + (( m_Endx - m_StartX) * frac);
            m_CurY = m_StartY + ((m_Endy - m_StartY) * frac);

            if((!m_Reverse && m_CurX > m_Endx) || (m_Reverse && m_CurX < m_Endx))
            {
                m_CurX = m_Endx;
                m_Animating.set(false);
                for (IBoardChangeListener b : m_Listeners) {
                    b.Changed(true);
                }
            }
            else if((!m_Reverse && m_CurY > m_Endy) || (m_Reverse && m_CurY < m_Endy))
            {
                m_CurY = m_Endy;
                m_Animating.set(false);
                for (IBoardChangeListener b : m_Listeners) {
                    b.Changed(true);
                }
            }
            canvas.drawBitmap(m_TileMap, m_CurX, m_CurY, m_TilePaint);
            canvas.drawText(boardToText(m_Tiles[m_LastBlank]), m_CurX + m_XFontOffset, m_CurY + m_YFontOffset, m_TextPaint);
        }



        if(AppController.DEBUG)
       canvas.drawCircle(m_LastClickx, m_LastClicky, 10.0f, m_TextPaint);
        if(m_Animating.get())
            m_Handler.postDelayed(this::invalidate, 16);
    }

    private double curFrac()
    {
        long dt = m_TimeNow - m_TimeStart;
        if(dt > 300)
            return 1.1;
       // return 1 + (Math.pow(2, (-10 * (dt / 250f))) * Math.sin(2 * Math.PI * ((dt / 250f) - 0.3/4)));

        return (Math.cos(((dt/250f) + 1f) * Math.PI) / 2.0f) + 0.5f;
    }

    public void Pause()
    {
        m_Paused = true;
    }

    public void UnPause()
    {
        m_Paused = false;
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
    /*
     * Called internally only.
     */
    public void onGlobalLayout()
    {
        synchronized (m_Lock) {
            ViewTreeObserver ob = getViewTreeObserver();
            ob.removeOnGlobalLayoutListener(this);
            m_TileHeight = getHeight() / 5;
            m_TileWidth = getWidth() / 5;
            m_TextPaint.setTextSize(m_TileHeight * 0.60f);
            m_TextPaint.setTextScaleX(m_TileWidth / m_TileHeight);
            m_TextPaint.setTextAlign(Paint.Align.CENTER);
            m_XFontOffset = m_TileWidth / 2;
            Paint.FontMetrics fm = m_TextPaint.getFontMetrics();
            m_YFontOffset = (m_TileHeight / 2) - (fm.ascent + fm.descent) / 2;
            m_TileMap = Bitmap.createBitmap((int)m_TileWidth, (int)m_TileHeight, Bitmap.Config.ARGB_8888);
            m_TileSelect = Bitmap.createBitmap((int)m_TileWidth, (int)m_TileHeight, Bitmap.Config.ARGB_8888);
            @SuppressWarnings("deprecation") Drawable d = getResources().getDrawable(R.drawable.tile);
            d.setBounds(0,0,(int)m_TileWidth, (int)m_TileHeight);
            Canvas c = new Canvas(m_TileMap);
            d.draw(c);

            //noinspection deprecation
            d = getResources().getDrawable(R.drawable.tile_select);
            d.setBounds(0,0,(int)m_TileWidth, (int)m_TileHeight);
            c = new Canvas(m_TileSelect);
            d.draw(c);
           // m_TileMap = applyFleaEffect(m_TileMap);
            //m_TileMap = Bitmap.createBitmap((int)m_TileWidth, (int)m_TileHeight,Bitmap.Config.ARGB_8888);

            m_TilePaint.setShader(new BitmapShader(m_TileMap, Shader.TileMode.MIRROR, Shader.TileMode.CLAMP));
            if(m_Init.incrementAndGet() == 2)
                finishInit();
        }
    }

    private void finishInit()
    {
        invalidate();
        for (IGameStart st : m_Starters) {
            st.OnStart();
        }
        m_Starters.clear();
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
        return idx % 5 < 4 && m_Tiles[idx + 1] == BLANK_VALUE;

    }

    protected int coordsToIndex(float x, float y)
    {
        int ix = (int)(x / m_TileWidth);
        int iy = (int)(y / m_TileHeight);
        return iy * 5 + ix;
    }


    public static int findBlank(@NonNull byte[] array)
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

    public interface ISwipeHandler
    {
        void onSwipeEvent(int[] indexes);
    }
}
