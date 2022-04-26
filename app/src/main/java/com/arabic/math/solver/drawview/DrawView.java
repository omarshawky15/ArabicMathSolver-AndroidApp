package com.arabic.math.solver.drawview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.arabic.math.solver.drawview.actionhandlers.ActionHandler;
import com.arabic.math.solver.drawview.actionhandlers.DrawActionHandler;
import com.arabic.math.solver.drawview.actionhandlers.EraseActionHandler;
import com.arabic.math.solver.drawview.actionhandlers.MoveActionHandler;

import java.util.HashMap;
import java.util.Map;

public class DrawView extends View {

    private DrawViewManager manager;
    private final Map<Integer, ActionHandler> actionHandlers ;

    private final int DEFAULT_COLOR = Color.BLACK;
    private final int DEFAULT_STROKE = 10;
    private final int ALPHA = 0xff;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Paint mBitmapPaint;
    private final Paint mPaint;


    public DrawView(Context context) {
        this(context, null);
    }

    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        actionHandlers =new HashMap<>() ;
        // the below methods smoothens
        // the drawings of the user
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(DEFAULT_COLOR);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setAlpha(ALPHA);
        mPaint.setStrokeWidth(DEFAULT_STROKE);

//        manager = new DrawViewManager(this); //TODO to be considered later
    }

    public void setManager(DrawViewManager manager) {
        this.manager = manager;
        setActionHandlers();
    }
    private void setActionHandlers(){
        actionHandlers.put(DrawViewModes.DRAW,new DrawActionHandler(this.manager));
        actionHandlers.put(DrawViewModes.MOVE,new MoveActionHandler(this.manager));
        actionHandlers.put(DrawViewModes.ERASE,new EraseActionHandler(this.manager));
    }

    public void init(int height, int width) {
        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);
    }

    protected Bitmap save() {
        Matrix scaleMatrix = computeScaleMatrix();
        manager.scalePaths(scaleMatrix, new Matrix());
        drawPaths();
        return mBitmap;
    }

    private Matrix computeScaleMatrix() {
        RectF new_dim = new RectF(Float.MAX_VALUE, Float.MAX_VALUE, Float.MIN_VALUE, Float.MIN_VALUE), tempRect = new RectF(), old_dim = new RectF(0, 0, getMeasuredWidth(), getMeasuredHeight());
        for (Path i : manager.getDrawnPaths()) {
            i.computeBounds(tempRect, false);
            new_dim.left = Math.min(new_dim.left, tempRect.left-20);
            new_dim.top = Math.min(new_dim.top, tempRect.top-20);
            new_dim.bottom = Math.max(new_dim.bottom, tempRect.bottom+20);
            new_dim.right = Math.max(new_dim.right, tempRect.right+20);
        }
        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setRectToRect(new_dim, old_dim, Matrix.ScaleToFit.CENTER);
        return scaleMatrix;
    }

    private void drawPaths() {
        int backgroundColor = Color.WHITE;
        mCanvas.drawColor(backgroundColor);
        for (Path i : manager.getDrawnPaths()) {
            mCanvas.drawPath(i, mPaint);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        drawPaths();
        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
        canvas.restore();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        actionHandlers.get(manager.getMode()).handle(event);
        invalidate();
        return true;
    }

}
