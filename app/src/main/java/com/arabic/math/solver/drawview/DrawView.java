package com.arabic.math.solver.drawview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.Iterator;
import java.util.List;

public class DrawView extends View {

    private static final float TOUCH_TOLERANCE = 4;
    private float mX, mY;
    private Path mPath;
    private DrawViewManager manager;

    // the Paint class encapsulates the color
    // and style information about
    // how to draw the geometries,text and bitmaps
    private final int DEFAULT_COLOR = Color.BLACK;
    private final int DEFAULT_STROKE = 10;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Paint mBitmapPaint;
    private final Paint mPaint;

    //Zoom & pan touch event
    // These matrices will be used to move and zoom image
    private final Matrix matrix;
    private final Matrix savedMatrix;
    private final Matrix inv;

    // Remember some things for zooming
    PointF start = new PointF();
    PointF mid = new PointF();
    float oldDist = 1f;


    // Constructors to initialise all the attributes
    public DrawView(Context context) {
        this(context, null);
    }

    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();

        // the below methods smoothens
        // the drawings of the user
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(DEFAULT_COLOR);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setAlpha(0xff);
        mPaint.setStrokeWidth(DEFAULT_STROKE);

        // scaling variables
        matrix = new Matrix();
        savedMatrix = new Matrix();
        inv = new Matrix();
    }

    public DrawViewManager getManager() {
        return manager;
    }

    public void setManager(DrawViewManager manager) {
        this.manager = manager;
    }

    // this method instantiate the bitmap and object
    public void init(int height, int width) {
        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);
    }

    void resetMatrices() {
        matrix.reset();
        inv.reset();
        savedMatrix.reset();
    }

    // this methods returns the current bitmap
    protected Bitmap save() {
        Matrix scaleMatrix = computeScaleMatrix();
        scalePaths(scaleMatrix, new Matrix());
        drawPaths();
        return mBitmap;
    }

    private Matrix computeScaleMatrix() {
        RectF new_dim = new RectF(Float.MAX_VALUE, Float.MAX_VALUE, Float.MIN_VALUE, Float.MIN_VALUE), tempRect = new RectF(), old_dim = new RectF(0, 0, getMeasuredWidth(), getMeasuredHeight());
        for (Path i : manager.getBackwardPaths()) {
            i.computeBounds(tempRect, false);
            new_dim.left = Math.min(new_dim.left, tempRect.left);
            new_dim.top = Math.min(new_dim.top, tempRect.top);
            new_dim.bottom = Math.max(new_dim.bottom, tempRect.bottom);
            new_dim.right = Math.max(new_dim.right, tempRect.right);
        }
        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setRectToRect(new_dim, old_dim, Matrix.ScaleToFit.CENTER);
        return scaleMatrix;
    }

    private void scalePaths(Matrix scaleMatrix, Matrix inverse) {
        for (Iterator<Path> it = manager.getPathRefsIter(); it.hasNext(); ) {
            Path i = it.next();
            i.transform(inverse);
            i.transform(scaleMatrix);
        }
    }

    private void drawPaths() {
        int backgroundColor = Color.WHITE;
        mCanvas.drawColor(backgroundColor);
        for (Path i : manager.getBackwardPaths()) {
            mCanvas.drawPath(i, mPaint);
        }
    }

    // this is the main method where
    // the actual drawing takes place
    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        if (manager.isMoveMode())
            scalePaths(matrix, inv);
        drawPaths();
        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
        canvas.restore();
    }

    // the onTouchEvent() method provides us with
    // the information about the type of motion
    // which has been taken place, and according
    // to that we call our desired methods
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (manager.isMoveMode()) handleIfMoveMode(event);
        else if (manager.isErase()) {
            handleIfEraseMode(event);
        } else {
            handleIfPaintMode(event);
        }
        invalidate();
        return true;
    }

    private void handleIfEraseMode(MotionEvent event) {
        float newX = event.getX(), newY = event.getY();
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                start.set(newX, newY);
                break;
            case MotionEvent.ACTION_UP:
                resetMatrices();
                break;
            case MotionEvent.ACTION_MOVE:
                float dx = Math.abs(newX - start.x);
                float dy = Math.abs(newY - start.y);
                if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                    float left = Math.min(newX, start.x), right = Math.max(newX, start.x), top = Math.min(newY, start.y), bottom = Math.max(newY, start.y);
                    RectF thumbRect = new RectF(left, top, right, bottom), pathRect = new RectF();
                    List<Path> paths = manager.getBackwardPaths();
                    for (int i = 0; i < paths.size(); i++) {
                        paths.get(i).computeBounds(pathRect, false);
                        if (RectF.intersects(thumbRect, pathRect)) {
                            manager.pop(i);
                        }
                    }
                    start.set(event.getX(), event.getY());
                }
                break;
        }
    }
    // the below methods manages the touch
    // response of the user on the screen

    // firstly, we create a new Stroke
    // and add it to the paths list
    private void touchStart(float x, float y) {
        mPath = new Path();
        manager.push(mPath);
        mPath.reset();

        // this methods sets the starting
        // point of the line being drawn
        mPath.moveTo(x, y);

        // we save the current
        // coordinates of the finger
        mX = x;
        mY = y;
    }

    // in this method we check
    // if the move of finger on the
    // screen is greater than the
    // Tolerance we have previously defined,
    // then we call the quadTo() method which
    // actually smooths the turns we create,
    // by calculating the mean position between
    // the previous position and current position
    private void touchMove(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);

        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }
    }

    // at the end, we call the lineTo method
    // which simply draws the line until
    // the end position
    private void touchUp() {
        mPath.lineTo(mX, mY);
    }


    private void handleIfPaintMode(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                touchStart(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_UP:
                touchUp();
                break;
            case MotionEvent.ACTION_MOVE:     //when fingers are dragged, transform matrix for panning
                touchMove(event.getX(), event.getY());
                break;
        }
    }

    private void handleIfMoveMode(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                //when first finger down, get first point
                savedMatrix.set(matrix);
                start.set(event.getX(), event.getY());
                manager.setMode(DrawViewModes.PAN);
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                //when 2nd finger down, get second point
                oldDist = spacing(event);
                if (oldDist > 10f) {
                    savedMatrix.set(matrix);
                    midPoint(mid, event); //then get the mid point as centre for zoom
                    manager.setMode(DrawViewModes.ZOOM);
                }
                break;
            case MotionEvent.ACTION_UP:
                resetMatrices();
            case MotionEvent.ACTION_POINTER_UP:       //when both fingers are released, do nothing
                manager.setMode(DrawViewModes.NONE);
                break;
            case MotionEvent.ACTION_MOVE:     //when fingers are dragged, transform matrix for panning
                if (manager.getMode() == DrawViewModes.PAN) {
                    matrix.invert(inv);
                    matrix.set(savedMatrix);
                    matrix.postTranslate(event.getX() - start.x, event.getY() - start.y);
                } else if (manager.getMode() == DrawViewModes.ZOOM) { //if pinch_zoom, calculate distance ratio for zoom
                    float newDist = spacing(event);
                    if (newDist > 10f) {
                        matrix.invert(inv);
                        matrix.set(savedMatrix);
                        float scale = newDist / oldDist;
                        matrix.postScale(scale, scale, mid.x, mid.y);
                    }
                }
                break;
        }
    }

    /**
     * Determine the space between the first two fingers
     */
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    /**
     * Calculate the mid point of the first two fingers
     */
    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }


}
