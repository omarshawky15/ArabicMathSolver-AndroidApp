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

    private final int DEFAULT_COLOR = Color.BLACK, GRID_COLOR = Color.GRAY;
    private final int DEFAULT_STROKE = 14, GRID_STROKE = 1;
    private final int DEFAULT_ALPHA = 0xff, GRID_ALPHA =0x80;
    private final int SAVE_PADDING = 30 ;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Paint mBitmapPaint;
    private final Paint pathPaint , gridPaint;


    public DrawView(Context context) {
        this(context, null);
    }

    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        actionHandlers =new HashMap<>() ;
        // the below methods smoothens
        // the drawings of the user
        pathPaint = new Paint();
        pathPaint.setAntiAlias(true);
        pathPaint.setDither(true);
        pathPaint.setColor(DEFAULT_COLOR);
        pathPaint.setStyle(Paint.Style.STROKE);
        pathPaint.setStrokeJoin(Paint.Join.ROUND);
        pathPaint.setStrokeCap(Paint.Cap.ROUND);
        pathPaint.setAlpha(DEFAULT_ALPHA);
        pathPaint.setStrokeWidth(DEFAULT_STROKE);

        gridPaint = new Paint();
        gridPaint.setAntiAlias(true);
        gridPaint.setDither(true);
        gridPaint.setColor(GRID_COLOR);
        gridPaint.setAlpha(GRID_ALPHA);
        gridPaint.setStrokeWidth(GRID_STROKE);
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeJoin(Paint.Join.ROUND);
        gridPaint.setStrokeCap(Paint.Cap.ROUND);

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
        Bitmap tempBitmap = Bitmap.createBitmap(getMeasuredWidth(), getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas tempCanvas = new Canvas(tempBitmap);
        tempCanvas.drawColor(Color.WHITE);
        drawPaths(tempCanvas);
        invalidate();
        return tempBitmap;
    }

    private Matrix computeScaleMatrix() {
        RectF new_dim = new RectF(Float.MAX_VALUE, Float.MAX_VALUE, Float.MIN_VALUE, Float.MIN_VALUE), tempRect = new RectF(), old_dim = new RectF(0, 0, getMeasuredWidth(), getMeasuredHeight());
        for (Path i : manager.getDrawnPaths()) {
            i.computeBounds(tempRect, false);
            new_dim.left = Math.min(new_dim.left, tempRect.left-SAVE_PADDING);
            new_dim.top = Math.min(new_dim.top, tempRect.top-SAVE_PADDING);
            new_dim.bottom = Math.max(new_dim.bottom, tempRect.bottom+SAVE_PADDING);
            new_dim.right = Math.max(new_dim.right, tempRect.right+SAVE_PADDING);
        }
        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setRectToRect(new_dim, old_dim, Matrix.ScaleToFit.CENTER);
        return scaleMatrix;
    }

    private void drawPaths(Canvas canvas) {
        for (Path i : manager.getDrawnPaths()) {
            canvas.drawPath(i, pathPaint);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        mCanvas.drawColor(Color.WHITE);
        drawGridLines();
        drawPaths(mCanvas);
        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
        canvas.restore();
    }

    private void drawGridLines() {
        int canvasWidth = getMeasuredWidth(), canvasHeight = getMeasuredHeight();
        float gridSize = 30f;
        float pad = Math.max(getMeasuredHeight(), getMeasuredWidth()) / gridSize;
        for (int j = 0; j <= Math.max(canvasWidth, canvasHeight); j += (int)pad) {
            mCanvas.drawLine(0, j, canvasWidth, j, gridPaint);
            mCanvas.drawLine(j, 0, j, canvasHeight, gridPaint);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        actionHandlers.get(manager.getMode()).handle(event);
        invalidate();
        return true;
    }

}
