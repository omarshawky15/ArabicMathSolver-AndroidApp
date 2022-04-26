package com.arabic.math.solver.drawview.actionhandlers;

import android.graphics.PointF;
import android.view.MotionEvent;

import com.arabic.math.solver.drawview.DrawViewManager;

public abstract class ActionHandler {
    protected DrawViewManager manager ;
    protected PointF start ;
    protected final float TOUCH_TOLERANCE = 4;
    public ActionHandler(DrawViewManager manager) {
        this.manager = manager;
        start = new PointF();
    }
    public final void handle(MotionEvent event){
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                actionDown(event);
                break;
            case MotionEvent.ACTION_UP:
                actionUp(event);
                break;
            case MotionEvent.ACTION_MOVE:
                actionMove(event);
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                actionPointerDown(event);
                break;
            case MotionEvent.ACTION_POINTER_UP:
                actionPointerUp(event);
                break;
        }
    }
    abstract void actionDown(MotionEvent event);
    abstract void actionUp(MotionEvent event);
    abstract void actionMove(MotionEvent event);
    void actionPointerDown(MotionEvent event){};
    void actionPointerUp(MotionEvent event){};
}
