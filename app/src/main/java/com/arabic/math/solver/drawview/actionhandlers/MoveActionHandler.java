package com.arabic.math.solver.drawview.actionhandlers;

import android.graphics.Matrix;
import android.graphics.PointF;
import android.view.MotionEvent;

import com.arabic.math.solver.drawview.DrawViewManager;
import com.arabic.math.solver.drawview.DrawViewModes;

public class MoveActionHandler extends ActionHandler{
    private final int NONE = 0;
    private final int PAN = 1;
    private final int ZOOM = 2;
    private int mode ;
    private final Matrix current_matrix;
    private final Matrix savedMatrix;
    private final Matrix current_inverse;
    private float oldDist;
    public MoveActionHandler(DrawViewManager manager) {
        super(manager);
        mode = NONE;
        current_matrix = new Matrix();
        savedMatrix = new Matrix();
        current_inverse = new Matrix();
        oldDist = 1f;
    }

    @Override
    void actionDown(MotionEvent event) {
        savedMatrix.set(current_matrix);
        start.set(event.getX(), event.getY());
        mode =PAN;
    }

    @Override
    void actionUp(MotionEvent event) {

        resetMatrices();
    }

    @Override
    void actionMove(MotionEvent event) {
        if (mode == PAN) {
            current_matrix.invert(current_inverse);
            current_matrix.set(savedMatrix);
            current_matrix.postTranslate(event.getX() - start.x, event.getY() - start.y);
        } else if (mode == ZOOM) {
            float newDist = spacing(event);
            float scale = newDist / oldDist;
            if (newDist > 10f) {
                current_matrix.invert(current_inverse);
                current_matrix.set(savedMatrix);
                current_matrix.postScale(scale, scale, start.x, start.y);
            }
        }
        manager.scalePaths(current_matrix,current_inverse);
    }

    @Override
    void actionPointerDown(MotionEvent event) {
        oldDist = spacing(event);
        if (oldDist > 10f) {
            savedMatrix.set(current_matrix);
            midPoint(start, event);
            mode= ZOOM;
        }
    }

    @Override
    void actionPointerUp(MotionEvent event) {
        mode = NONE;
    }

    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    void resetMatrices() {
        current_matrix.reset();
        current_inverse.reset();
        savedMatrix.reset();
    }
}
