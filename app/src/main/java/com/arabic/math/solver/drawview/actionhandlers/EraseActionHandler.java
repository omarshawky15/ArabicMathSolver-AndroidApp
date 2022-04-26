package com.arabic.math.solver.drawview.actionhandlers;

import android.graphics.Path;
import android.graphics.RectF;
import android.view.MotionEvent;

import com.arabic.math.solver.drawview.DrawViewManager;

import java.util.List;

public class EraseActionHandler extends ActionHandler{
    public EraseActionHandler(DrawViewManager manager) {
        super(manager);
    }

    @Override
    void actionDown(MotionEvent event) {
        start.set(event.getX(), event.getY());
    }

    @Override
    void actionUp(MotionEvent event) {}

    @Override
    void actionMove(MotionEvent event) {
        float newX = event.getX(),newY = event.getY();
        float dx = Math.abs(newX - start.x);
        float dy = Math.abs(newY - start.y);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            float left = Math.min(newX, start.x), right = Math.max(newX, start.x), top = Math.min(newY, start.y), bottom = Math.max(newY, start.y);
            RectF thumbRect = new RectF(left, top, right, bottom), pathRect = new RectF();
            List<Path> paths = manager.getDrawnPaths();
            for (int i = 0; i < paths.size(); i++) {
                paths.get(i).computeBounds(pathRect, false);
                if (RectF.intersects(thumbRect, pathRect)) {
                    manager.pop(i);
                }
            }
            start.set(event.getX(), event.getY());
        }
    }
}
