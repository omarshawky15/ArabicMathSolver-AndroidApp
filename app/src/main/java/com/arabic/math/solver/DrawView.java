package com.arabic.math.solver;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

public class DrawView extends View {
    public boolean zoom_mode =false;
    private static final float TOUCH_TOLERANCE = 4;
    private float mX, mY;
    private Path mPath;

    // the Paint class encapsulates the color
    // and style information about
    // how to draw the geometries,text and bitmaps
    private Paint mPaint;

    // ArrayList to store all the strokes
    // drawn by the user on the Canvas
    private ArrayList<Stroke> paths = new ArrayList<>();
    private int currentColor;
    private int strokeWidth;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Paint mBitmapPaint = new Paint(Paint.DITHER_FLAG);

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
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setAlpha(0xff);
    }

    // this method instantiate the bitmap and object
    public void init(int height, int width) {

        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);

        // set an initial color of the brush
        currentColor = Color.BLACK;

        // set an initial brush size
        strokeWidth = 10;
    }

    // sets the current color of stroke
    public void setColor(int color) {
        currentColor = color;
    }

    public int getCurrentColor() {
        return currentColor;
    }

    // sets the stroke width
    public void setStrokeWidth(int width) {
        strokeWidth = width;
    }

    public int getStrokeWidth() {
        return strokeWidth;
    }

    public void undo() {
        // check whether the List is empty or not
        // if empty, the remove method will return an error
        if (paths.size() != 0) {
            paths.remove(paths.size() - 1);
            invalidate();
        }
    }

    // this methods returns the current bitmap
    public Bitmap save() {
        return mBitmap;
    }

    // this is the main method where
    // the actual drawing takes place
    @Override
    protected void onDraw(Canvas canvas) {
        // save the current state of the canvas before,
        // to draw the background of the canvas
        canvas.save();
        int width = getMeasuredWidth();
        int height =getMeasuredHeight();
        init(height, width);
        // DEFAULT color of the canvas
        int backgroundColor = Color.WHITE;
        mCanvas.drawColor(backgroundColor);

        // now, we iterate over the list of paths
        // and draw each path on the canvas
        mPaint.setColor(currentColor);
        mPaint.setStrokeWidth(strokeWidth);
        for (Stroke fp : paths) {

            if(zoom_mode) {
                fp.path.transform(inv);
                fp.path.transform(matrix);
            }
            mCanvas.drawPath(fp.path, mPaint);
        }
        Log.e("matrix", matrix.toString());
        canvas.drawBitmap(mBitmap,  0,0, mBitmapPaint);
        canvas.restore();
    }

    // the below methods manages the touch
    // response of the user on the screen

    // firstly, we create a new Stroke
    // and add it to the paths list
    private void touchStart(float x, float y) {
        mPath = new Path();
        Stroke fp = new Stroke(currentColor, strokeWidth, mPath);
        paths.add(fp);
        // finally remove any curve
        // or line from the path
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

    // the onTouchEvent() method provides us with
    // the information about the type of motion
    // which has been taken place, and according
    // to that we call our desired methods
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.e("pos", event.getX() +" " + event.getY());
        handleTouch(event);
        invalidate();
        return true;
    }

    //Zoom & pan touch event
    // These matrices will be used to move and zoom image
    Matrix matrix = new Matrix();
    Matrix savedMatrix = new Matrix();
    Matrix inv = new Matrix();

    // Remember some things for zooming
    PointF start = new PointF();
    PointF mid = new PointF();
    float oldDist = 1f;

    // We can be in one of these 3 states
    static final int NONE = 0;
    static final int PAN = 1;
    static final int ZOOM = 2;
    int mode = NONE;

    private static final String TAG = "DebugTag";
    private void handleTouch(MotionEvent event){
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                if(zoom_mode) {
                    //when first finger down, get first point
                    savedMatrix.set(matrix);
                    start.set(event.getX(), event.getY());
//                    Log.d(TAG, "mode=PAN");
                    mode = PAN;
                }
                else {
                    touchStart(event.getX(), event.getY());
//                    matrix = new Matrix();
//                    savedMatrix = new Matrix();
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                if(zoom_mode) {
                    //when 2nd finger down, get second point
                    oldDist = spacing(event);
//                    Log.d(TAG, "oldDist=" + oldDist);
                    if (oldDist > 10f) {
                        savedMatrix.set(matrix);
                        midPoint(mid, event); //then get the mide point as centre for zoom
                        mode = ZOOM;
//                        Log.d(TAG, "mode=ZOOM");
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if(!zoom_mode){
                    touchUp();
//                    matrix = new Matrix();
                    break;
                }else {
                    matrix.reset();
                    inv.reset();
                    savedMatrix.reset();
                }
            case MotionEvent.ACTION_POINTER_UP:       //when both fingers are released, do nothing
                mode = NONE;
//                Log.d(TAG, "mode=NONE");
                break;
            case MotionEvent.ACTION_MOVE:     //when fingers are dragged, transform matrix for panning
                if(zoom_mode) {
                    if (mode == PAN) {
                        // ...
                        matrix.invert(inv);
                        matrix.set(savedMatrix);
                        matrix.postTranslate(event.getX() - start.x,
                                event.getY() - start.y);
//                        Log.d(TAG, "Mapping rect");
                        //start.set(event.getX(), event.getY());
                    } else if (mode == ZOOM) { //if pinch_zoom, calculate distance ratio for zoom
                        float newDist = spacing(event);
//                        Log.d(TAG, "newDist=" + newDist);
                        if (newDist > 10f) {
                            matrix.invert(inv);
                            matrix.set(savedMatrix);
                            float scale = newDist / oldDist;
                            matrix.postScale(scale, scale, mid.x, mid.y);
                        }
                    }
                }
                else {
                    touchMove(event.getX(),event.getY());
//                    matrix = new Matrix();
//                    savedMatrix = new Matrix();
                }
                break;
        }
    }

    /** Determine the space between the first two fingers */
    private float spacing(MotionEvent event) {
        // ...
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float)Math.sqrt(x * x + y * y);
    }

    /** Calculate the mid point of the first two fingers */
    private void midPoint(PointF point, MotionEvent event) {
        // ...
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }
}
