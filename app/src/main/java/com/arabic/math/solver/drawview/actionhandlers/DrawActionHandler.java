package com.arabic.math.solver.drawview.actionhandlers;

import android.graphics.Path;
import android.graphics.Point;
import android.view.MotionEvent;

import com.arabic.math.solver.drawview.DrawViewManager;

public class DrawActionHandler extends ActionHandler {
    Path mPath;

    public DrawActionHandler(DrawViewManager manager) {
        super(manager);
    }

    // firstly, we create a new Stroke
    // and add it to the paths list
    @Override
    void actionDown(MotionEvent event) {
        mPath = new Path();
        mPath.reset();
        manager.push(mPath);
        start.set(event.getX(), event.getY());
        mPath.moveTo(start.x, start.y);
    }

    // at the end, we call the lineTo method
    // which simply draws the line until
    // the end position
    @Override
    void actionUp(MotionEvent event) {
        mPath.lineTo(start.x, start.y);
        mPath = null;
    }

    // in this method we check
    // if the move of finger on the
    // screen is greater than the
    // Tolerance we have previously defined,
    // then we call the quadTo() method which
    // actually smooths the turns we create,
    // by calculating the mean position between
    // the previous position and current position
    @Override
    void actionMove(MotionEvent event) {
        float dx = Math.abs(event.getX() - start.x);
        float dy = Math.abs(event.getY() - start.y);

        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(start.x, start.y, (event.getX() + start.x) / 2, (event.getY() + start.y) / 2);
            start.set(event.getX(), event.getY());
        }
    }
}

