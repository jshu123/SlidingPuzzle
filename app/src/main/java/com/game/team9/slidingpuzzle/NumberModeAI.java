package com.game.team9.slidingpuzzle;

import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;

import com.game.team9.slidingpuzzle.NumberModeView.IBoardSolvedListener;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;
import java.util.TreeSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created on: 1/30/18
 *     Author: David Hiatt - dhiatt89@gmail.com
 */

public class NumberModeAI implements BaseGameView.IBoardChangeListener, IBoardSolvedListener, Runnable {

    private static final boolean m_Debug = true;

    private static final boolean m_BoringAI = true;


    private final BlockingQueue<Pair<Integer,Integer>> m_Moves = new LinkedBlockingQueue<>(10);

    private final NumberModeView m_Game;

    private boolean m_Stop = false;
    private final AtomicBoolean m_Lock = new AtomicBoolean(false);
    @NonNull
    private ThreadStatus m_Status = ThreadStatus.NA;

    private final Thread m_SearchThread = new Thread(new Runnable() {
        @Override
        public void run() {
            Thread.currentThread().setName("AI Searcher");
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
            SearchThread();
        }
    });
    private final Thread m_MoveThread = new Thread(new Runnable() {
        @Override
        public void run() {
            Thread.currentThread().setName("AI Mover");
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
            if(m_BoringAI)
                BoringAIThread();
            else
                MoveThread();
        }
    });

    private static int m_Total = 0;


    public NumberModeAI(NumberModeView ai)
    {
        m_Game = ai;
        m_Game.AttachChangeListener(this);
        m_Game.AttachSolveListener(this);
    }

    @Override
    public void Solved(int id) {
        m_Stop = true;
        if(!m_BoringAI) {
            switch (m_Status) {

                case NA:
                    break;
                case RUNNING:
                    break;
                case WAITING:
                    m_Moves.notify();
                    m_Lock.notify();
                    break;
                case TAKING:
                    m_MoveThread.interrupt();
                    break;
            }
        }
    }

    @Override
    public void Changed(boolean b) {
        if(m_Debug)
        Log.d("CHANGE", "RECEIVED");
        {
            if (!b) {
                Log.w("CHANGE", "FAILED");
                //m_Reset.set(true);
            }
        }
        synchronized (m_Lock) {
            if (!m_Lock.compareAndSet(true, false))
                m_Lock.notify();
        }
        if(m_Debug)
            Log.d("CHANGE", "DONE");
    }

    @Override
    public void run() {
        if(!m_BoringAI) {
            m_SearchThread.start();
            m_MoveThread.start();
        }
        else
            BoringAIThread();
    }

    private void BoringAIThread()
    {
        if(m_Debug)
            Log.i("AI", "START");
        int last = -1;
        //up = 0
        //down == 1
        //left == 2
        //right == 3
        int[] tiles = m_Game.getTiles();
        int blank = m_Game.getBlank();
        Random rand = new Random();
        float xOffset = m_Game.getTileWidth() / 2;
        float yOffset = m_Game.getTileHeight() / 2;
        int move;
        while(!m_Stop)
        {
            do {
                move = rand.nextInt(4);
            }while(!m_Stop && !tryMove(move, last, blank));
                last = move;
                blank = dir2int(move, blank);
            int x = (blank % 5);
            int y = (blank > 0 ? blank / 5 : 0);

            float dx = x * m_Game.getTileWidth() + xOffset;
            float dy = y * m_Game.getTileHeight() + yOffset;
            if(m_Debug)
            Log.d("AI", blank + ": (" + x + ", " + y + ") @ " + dx + ", " + dy);
            MotionEvent event = MotionEvent.obtain(SystemClock.uptimeMillis(),
                    SystemClock.uptimeMillis() + 100,
                    MotionEvent.ACTION_POINTER_INDEX_MASK,
                    x * m_Game.getTileWidth() + xOffset,
                    y * m_Game.getTileHeight() + yOffset,
                    0);
            // Assert.assertTrue(m_Game.canSlide(i.intValue()));
            synchronized (m_Lock) {
                while (!m_Stop && !m_Lock.compareAndSet(false, true))
                    try {
                        m_Status = ThreadStatus.WAITING;
                        if(m_Debug)
                        Log.d("CHANGE", "WAITING");
                        m_Lock.wait(400+rand.nextInt(150));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        m_Status = ThreadStatus.RUNNING;
                    }
                    if(m_Stop)
                        break;
                if(m_Debug)
                    Log.d("CHANGE", "ADDED");
                m_Game.dispatchTouchEvent(event);
            }
            try {
                Thread.sleep(400+rand.nextInt(150));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if(m_Debug)
        Log.i("AI", "EXIT");
    }

    private static int dir2int(int dir, int blank)
    {
        switch(dir)
        {
            case 0: return blank - 5;
            case 1:return blank + 5;
            case 2:return blank - 1;
            case 3:return blank + 1;
        }
        return blank;
    }

    private static boolean tryMove(int dir, int last, int blank)
    {
        switch(dir)
        {
            case 0:if(last == 1 || blank - 5 < 0)return false;
            break;
            case 1:if(last == 0 || blank + 5 > 24) return false;
            break;
            case 2:if(last == 3 || (blank % 5) == 0) return false;
            break;
            case 3:if(last == 2 || ((blank + 1) % 5) == 0)return false;
            break;
        }
        return true;


    }

    private void MoveThread()
    {
        m_Status = ThreadStatus.RUNNING;
        float xOffset = m_Game.getTileWidth() / 2;
        float yOffset = m_Game.getTileHeight() / 2;

        while(!m_Stop)
        {
            synchronized (m_Moves)
            {
                try {
                    m_Status = ThreadStatus.WAITING;
                    m_Moves.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                }
                finally{
                    m_Status = ThreadStatus.RUNNING;
                }
            }

            while (!m_Stop && !m_Moves.isEmpty())
            {
                Pair<Integer, Integer> i = null;
                try {
                    m_Status = ThreadStatus.TAKING;
                    i = m_Moves.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                finally
                {
                    m_Status = ThreadStatus.RUNNING;
                }

                if (i != null && i.second >= 0) {
                    if(i.first == m_Game.getBlank()) {
                        synchronized (m_Lock) {
                            while (!m_Lock.compareAndSet(false, true))
                                try {
                                    m_Status = ThreadStatus.WAITING;
                                Log.d("CHANGE", "WAITING");
                                    m_Lock.wait();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                finally
                                {
                                    m_Status = ThreadStatus.RUNNING;
                                }

                            int x = (i.second % 5);
                            int y = (i.second > 0 ? i.second / 5 : 0);

                            float dx = x * m_Game.getTileWidth() + xOffset;
                            float dy = y * m_Game.getTileHeight() + yOffset;
                            Log.d("MOVE", i + ": (" + x + ", " + y + ") @ " + dx + ", " + dy);
                            MotionEvent event = MotionEvent.obtain(SystemClock.uptimeMillis(),
                                    SystemClock.uptimeMillis() + 100,
                                    MotionEvent.ACTION_POINTER_INDEX_MASK,
                                    x * m_Game.getTileWidth() + xOffset,
                                    y * m_Game.getTileHeight() + yOffset,
                                    0);
                            // Assert.assertTrue(m_Game.canSlide(i.intValue()));
                            Log.d("CHANGE", "FREE");
                            m_Game.dispatchTouchEvent(event);
                        }
                    }
                }
            }
        }
        m_Status = ThreadStatus.NA;
    }

    private void SearchThread()
    {

        if(m_Debug)
            Log.i("AI Thread Started", "ID: " + android.os.Process.myTid());
        TreeSet<state> tree = new TreeSet<>(new Comparator<state>() {
            @Override
            public int compare(@NonNull state s, @NonNull state t1) {
                return s.getFitness() - t1.getFitness();
            }
        });

        state best = new state(m_Game.getTiles());
        m_Total += 1 + best.FindSuccessor();
        tree.add(best);
        int bound = best.getFitness();
        int b = bound;
        while(!m_Stop && !tree.isEmpty())
        {
            state next = tree.pollFirst();
            if(next.h == 0)
            {
                //SOLUTION FOUND
                m_Stop = true;
            }
            if(next.h < b)
                b = next.h;
            m_Total += next.FindSuccessor();
    //        int min = Integer.MAX_VALUE;
            while(!m_Stop && !next.Successors.isEmpty())
            {
                state child = next.Successors.pollFirst();
                tree.add(child);
                if(child.h < b)
                    b = next.h;
      //          if(child.getFitness() < bound)
    //                child);
  //              else
//                    m_Total -= child.ClosePath();
            }
            if(m_Debug && (m_Total % 1000 < 5))
                Log.i("SEARCH", "Opened " + m_Total + " Best " + b);
        }
        if(m_Debug)
            Log.i("SEARCH", "Exiting with " + m_Total + " at " + bound);
    }




    private class state
    {
        @NonNull
        public final Direction dir;
        public final int from;
        public final int to;
        public final int env[] = new int[25];
        public final int v;
        public final int g;
        public final int h;
        private final boolean[] m_Conflicts = new boolean[25];

        @Nullable
        public state parent;

        private boolean m_Opened = false;
        private boolean m_Closed  = false;

        public final TreeSet<state> Successors = new TreeSet<>(new Comparator<state>() {
            @Override
            public int compare(@NonNull state s, @NonNull state t1) {
                return s.getFitness() - t1.getFitness();
            }
        });

        public state(@NonNull int e[])
        {
            dir = Direction.ROOT;
            to = v = -1;
            g = 0;
            System.arraycopy(e, 0, env, 0, e.length);
            h = heuristic();
            from = BaseGameView.findBlank(env);
            //touched = opened = added = false;
            parent = null;
        }

        public state(int f, int t, @NonNull int[] e, int gg)
        {
            to = t;
            from = f;
            v = e[f];
            g=gg;
            System.arraycopy(e, 0, env, 0, e.length);
            env[f] = e[t];
            env[t] = v;
            //opened = true;
            //touched = false;
            parent = null;
            h = heuristic();
            if(to + 5 == from)
                dir = Direction.Down;
            else if(to - 5 == from)
                dir = Direction.Up;
            else if(to - 1 == from)
                dir = Direction.Left;
            else
                dir = Direction.Right;
        }

        public int getFitness()
        {
            return g + h; //A*
            //return h; //Best first
        }


        public int ClosePath()
        {
            m_Closed = true;
            int closed = 1;
            if(parent != null && parent.Successors.remove(this) && parent.Successors.isEmpty())
            {
                closed += parent.ClosePath();
            }
            state s;
            while((s = Successors.pollFirst()) !=null)
                closed += s.ClosePath();
            return closed;
        }

        public boolean checkMatches(@Nullable state last)
        {
            if(last == null)
                return false;
            return Successors.contains(last);
        }


        public boolean checkPath(@NonNull state b)
        {
            state c = this;
            while(c != null)
            {
                if(c.match(b.env))
                    return true;
                c = c.parent;
            }
            return false;
        }

        public boolean match(@NonNull int[] a)
        {
            boolean ret = true;
            for(int i = 0; i < a.length; ++i)
            {
                if(a[i] != env[i]) {
                    ret = false;
                    break;
                }
            }
            return ret;
        }


        public int FindSuccessor()
        {
            if(m_Closed)
                return -1;
            if(m_Opened)
                return Successors.size();
            m_Opened = true;
            //state node = null;
            int min = Integer.MAX_VALUE;
            int opened = 0;
            if(dir != Direction.Down && from - 5 >= 0)
            {
               state node = new state(from - 5, from, env, g+1);//add(env[s - 5], s, s - 5, moves, cur);
                if (!checkMatches(node) && !checkPath(node)) {
                    node.parent = this;
                    if(node.h < min)
                        min = node.h;
                    Successors.add(node);
                    ++opened;
                }
            }
            if (dir != Direction.Up && from + 5 < (25)) // DOWN
            {
                // printf("Down ");
                state node = new state(from + 5, from, env, g+1);//add(env[s + 5], s, s + 5, moves, cur);
                if (!checkMatches(node) && !checkPath(node)) {
                    node.parent = this;
                    if(node.h < min)
                        min = node.h;
                    Successors.add(node);
                    ++opened;
                }
            }
            if (dir != Direction.Left && (from % 5) < (5 - 1))//RIGHT
            {
                // printf("Right ");
                state node = new state(from + 1, from, env, g+1);//add(env[s + 1], s, s + 1, moves, cur);
                if (!checkMatches(node) && !checkPath(node)) {
                    node.parent = this;
                    if(node.h < min)
                        min = node.h;
                    Successors.add(node);
                    ++opened;
                }
            }
            if (dir != Direction.Right && (from % 5) > 0)//LEFT
            {
                // printf("Left ");
                state node = new state(from - 1, from, env, g+1);//add(env[s - 1], s, s - 1, moves, cur);
                if (!checkMatches(node) && !checkPath(node)) {
                    node.parent = this;
                    if(node.h < min)
                        min = node.h;
                    Successors.add(node);
                    ++opened;
                }
            }
            return opened;
        }


        private int heuristic()
        {
            return manhattan_distance() + linear_conflict() + corner_tiles() + last_2moves();
        }

        private int manhattan_distance()
        {
            int x,y,dy,dx;
            int h1 = 0;
            for (int i = 0; i < env.length; i++) {
                if(env[i] == BaseGameView.BLANK_VALUE)
                    continue;
                else
                {
                    x = ((env[i] - 1) % 5); //subtract 1 because '1' is the first number, not 0.
                    y = ((env[i] > 1 ? env[i] - 1 : env[i]) / 5);
                }
                dx = i % 5;
                dy = (i == 0 ? 0 : i / 5);
                h1+= Math.abs(x - dx) + Math.abs(y - dy);
            }
            return h1;
        }

        private int linear_conflict()
        {
            int h1 = 0;
            Arrays.fill(m_Conflicts, false);
            for(int row = 0, col = 0; row < 5; ++row)
            {

                int ymax = -1;
                int xmax = -1;
                for(; col < 5; ++col)
                {
                    int i = col * 5 + row;
                    if(env[i] != BaseGameView.BLANK_VALUE)
                    {
                        if(((env[i] - 1) / 5 == row))
                        {
                            if(env[i] > ymax)
                                ymax = env[i];
                            else
                            {
                                m_Conflicts[i] = true;
                                h1+=2;
                            }

                        }
                        if((env[i] % 5 == row))
                        {
                            if(env[i] > xmax)
                                xmax = env[i];
                            else
                            {
                                m_Conflicts[i] = true;
                                h1+=2;
                            }
                        }
                    }
                }
            }
            return h1;
        }

        private int corner_tiles()
        {
            int h1 = 0;
            if(env[4] != 5)
            {
                if(!m_Conflicts[3] && env[3] == 4)
                    h1+=2;
                if(!m_Conflicts[9] && env[9] == 10)
                    h1+=2;
            }

            if(env[0] != 1)
            {
                if(!m_Conflicts[1] && env[1] == 2)
                    h1+=2;
                if(!m_Conflicts[5] && env[5] == 6)
                    h1+=2;
            }

            if(env[20] != 21)
            {
                if(!m_Conflicts[15] && env[15] == 16)
                    h1+=2;
                if(!m_Conflicts[21] && env[21] == 22)
                    h1+=2;
            }
            return h1;
        }

        private int last_2moves()
        {
            if(env[23] == BaseGameView.BLANK_VALUE || env[19] == BaseGameView.BLANK_VALUE)
                return 0;
            if(!m_Conflicts[19] && !m_Conflicts[23] && (env[22] == BaseGameView.BLANK_VALUE || env[18] == BaseGameView.BLANK_VALUE || env[14] == BaseGameView.BLANK_VALUE))
                return 2;
            if(!m_Conflicts[14] && !m_Conflicts[18] && !m_Conflicts[22])
                return 4;
            return 0;
        }
    }

    private static void debugPrint(int env[])
    {
        for(int i = 0; i < 5; ++i)
        {
            Log.i("STATUS", "[ " + env[i * 5] + ", " + env[i * 5 + 1] + ", " + env[i * 5 + 2] + ", " + env[i * 5 + 3] +   ", " + env[i * 5 + 4] + "]");
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

    private enum ThreadStatus
    {
        NA,
        RUNNING,
        WAITING,
        TAKING
    }
}
