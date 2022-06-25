package com.arabic.math.solver.drawview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.ColorInt;

import com.arabic.math.solver.R;
import com.arabic.math.solver.drawview.actionhandlers.ActionHandler;
import com.arabic.math.solver.drawview.actionhandlers.DrawActionHandler;
import com.arabic.math.solver.drawview.actionhandlers.EraseActionHandler;
import com.arabic.math.solver.drawview.actionhandlers.MoveActionHandler;

import java.util.HashMap;
import java.util.Map;

public class DrawView extends View {

    private DrawViewManager manager;
    private final Map<Integer, ActionHandler> actionHandlers;
    @ColorInt
    private int CANVAS_PATH_COLOR , GRID_COLOR = Color.GRAY, BACKGROUND_CANVAS_COLOR;
    @ColorInt
    private final int SERVER_PATH_COLOR = Color.BLACK, BACKGROUND_SERVER_COLOR= Color.WHITE;
    private final int DEFAULT_STROKE = 10, GRID_STROKE = 1, ERASER_STROKE = 5;
    private final int DEFAULT_ALPHA = 0xff, GRID_ALPHA = 0xA0;
    private final int SAVE_PADDING = 30;
    private final boolean CLEAR_CANVAS = true;
    private Bitmap mBitmap;
    public Canvas mCanvas;
    private Paint mBitmapPaint;
    private final Paint myCanvasPathPaint, myServerPathPaint, myGridPaint, myEraserPaint;
    public boolean eraseFlag;

    public DrawView(Context context) {
        this(context, null);
    }

    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        actionHandlers = new HashMap<>();
        eraseFlag = CLEAR_CANVAS;

        setCanvasColors(context);

        // the below methods smoothens
        // the drawings of the user
        myCanvasPathPaint = new Paint();
        myCanvasPathPaint.setAntiAlias(true);
        myCanvasPathPaint.setDither(true);
        myCanvasPathPaint.setColor(CANVAS_PATH_COLOR);
        myCanvasPathPaint.setStyle(Paint.Style.STROKE);
        myCanvasPathPaint.setStrokeJoin(Paint.Join.ROUND);
        myCanvasPathPaint.setStrokeCap(Paint.Cap.ROUND);
        myCanvasPathPaint.setAlpha(DEFAULT_ALPHA);
        myCanvasPathPaint.setStrokeWidth(DEFAULT_STROKE);

        myServerPathPaint = new Paint();
        myServerPathPaint.setAntiAlias(true);
        myServerPathPaint.setDither(true);
        myServerPathPaint.setColor(SERVER_PATH_COLOR);
        myServerPathPaint.setStyle(Paint.Style.STROKE);
        myServerPathPaint.setStrokeJoin(Paint.Join.ROUND);
        myServerPathPaint.setStrokeCap(Paint.Cap.ROUND);
        myServerPathPaint.setAlpha(DEFAULT_ALPHA);
        myServerPathPaint.setStrokeWidth(DEFAULT_STROKE);


        myGridPaint = new Paint();
        myGridPaint.setAntiAlias(true);
        myGridPaint.setDither(true);
        myGridPaint.setColor(GRID_COLOR);
        myGridPaint.setAlpha(GRID_ALPHA);
        myGridPaint.setStyle(Paint.Style.STROKE);
        myGridPaint.setStrokeWidth(GRID_STROKE);
        myGridPaint.setStrokeJoin(Paint.Join.ROUND);
        myGridPaint.setStrokeCap(Paint.Cap.ROUND);
        myGridPaint.setPathEffect(new DashPathEffect(new float[]{10f, 20f}, 0f));

        myEraserPaint = new Paint();
        myEraserPaint.setAntiAlias(true);
        myEraserPaint.setDither(true);
        myEraserPaint.setColor(CANVAS_PATH_COLOR);
        myEraserPaint.setAlpha(DEFAULT_ALPHA);
        myEraserPaint.setStyle(Paint.Style.STROKE);
        myEraserPaint.setStrokeWidth(ERASER_STROKE);
        myEraserPaint.setStrokeJoin(Paint.Join.ROUND);
        myEraserPaint.setStrokeCap(Paint.Cap.ROUND);
        myEraserPaint.setPathEffect(new DashPathEffect(new float[]{15f, 15f}, 0f));

//        manager = new DrawViewManager(this); //TODO to be considered later
    }

    public void setManager(DrawViewManager manager) {
        this.manager = manager;
        setActionHandlers();
    }

    private void setActionHandlers() {
        actionHandlers.put(DrawViewModes.DRAW, new DrawActionHandler(this.manager));
        actionHandlers.put(DrawViewModes.MOVE, new MoveActionHandler(this.manager));
        actionHandlers.put(DrawViewModes.ERASE, new EraseActionHandler(this.manager));
    }
    private void setCanvasColors(Context context){
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();

        theme.resolveAttribute(R.attr.colorOnPrimary, typedValue, true);
        CANVAS_PATH_COLOR = typedValue.data;

        theme.resolveAttribute(android.R.attr.windowBackground, typedValue, true);
        BACKGROUND_CANVAS_COLOR = typedValue.data;
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
        tempCanvas.drawColor(BACKGROUND_SERVER_COLOR);
        drawPaths(tempCanvas,myServerPathPaint);
        invalidate();
        return tempBitmap;
    }

    private Matrix computeScaleMatrix() {
        RectF new_dim = new RectF(Float.MAX_VALUE, Float.MAX_VALUE, Float.MIN_VALUE, Float.MIN_VALUE), tempRect = new RectF(), old_dim = new RectF(0, 0, getMeasuredWidth(), getMeasuredHeight());
        for (Path i : manager.getDrawnPaths()) {
            i.computeBounds(tempRect, false);
            new_dim.left = Math.min(new_dim.left, tempRect.left - SAVE_PADDING);
            new_dim.top = Math.min(new_dim.top, tempRect.top - SAVE_PADDING);
            new_dim.bottom = Math.max(new_dim.bottom, tempRect.bottom + SAVE_PADDING);
            new_dim.right = Math.max(new_dim.right, tempRect.right + SAVE_PADDING);
        }
        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setRectToRect(new_dim, old_dim, Matrix.ScaleToFit.CENTER);
        return scaleMatrix;
    }

    private void drawPaths(Canvas canvas, Paint pathPaint) {
        for (Path i : manager.getDrawnPaths()) {
            canvas.drawPath(i, pathPaint);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (eraseFlag == CLEAR_CANVAS) {
            mCanvas.drawColor(BACKGROUND_CANVAS_COLOR);
        } else eraseFlag = CLEAR_CANVAS;
        canvas.save();
        drawGridLines(mCanvas);
        drawPaths(mCanvas, myCanvasPathPaint);
        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
        canvas.restore();
    }

    private void drawGridLines(Canvas canvas) {
        int canvasWidth = getMeasuredWidth(), canvasHeight = getMeasuredHeight();
        float gridSize = 30f;
        float pad = Math.max(getMeasuredHeight(), getMeasuredWidth()) / gridSize;
        for (int j = 0; j <= Math.max(canvasWidth, canvasHeight); j += (int) pad) {
            canvas.drawLine(0, j, canvasWidth, j, myGridPaint);
            canvas.drawLine(j, 0, j, canvasHeight, myGridPaint);
        }
    }

    private void drawCircle(Canvas canvas, float x, float y) {
        canvas.drawCircle(x, y, ((EraseActionHandler) actionHandlers.get(DrawViewModes.ERASE)).EXTRA_PAD, myEraserPaint);

    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        actionHandlers.get(manager.getMode()).handle(event);
        mCanvas.drawColor(BACKGROUND_CANVAS_COLOR);
        if (manager.isErase() && (event.getAction() & MotionEvent.ACTION_MASK) != MotionEvent.ACTION_UP)
            drawCircle(mCanvas, event.getX(), event.getY());
        eraseFlag = !CLEAR_CANVAS;
        invalidate();
        return true;
    }

    protected void resetEraseFlag() {
        eraseFlag = CLEAR_CANVAS;
    }

}
