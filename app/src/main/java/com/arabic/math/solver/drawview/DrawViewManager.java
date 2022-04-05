package com.arabic.math.solver.drawview;

import android.graphics.Bitmap;
import android.graphics.Path;
import android.view.View;

import com.arabic.math.solver.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DrawViewManager {
    private final List<Path> backPaths ,forwardPaths;
    private int mode;
    private final DrawView paint;
    private FloatingActionButton redoFab , undoFab;
    private int backwardSize ,forwardSize;
    public DrawViewManager(DrawView paint){
        mode = DrawViewModes.DRAW;
        backPaths = new ArrayList<>();
        forwardPaths = new ArrayList<>();
        backwardSize=0 ;
        forwardSize =0 ;
        mode= DrawViewModes.DRAW;
        this.paint = paint ;
    }

    public boolean isMoveMode() {
        return mode < DrawViewModes.DRAW && mode >= DrawViewModes.NONE;
    }

    public void setMode(int mode) {
        this.mode = mode;
        paint.resetMatrices();
    }

    public void undo() {
        if (backwardSize!= 0) {
            backwardSize--;
            forwardSize++;
            forwardPaths.add(backPaths.get(backwardSize));
            backPaths.remove(backwardSize);
            paint.invalidate();
        }
        resetUndoRedo();
    }


    public void redo() {
        if (forwardSize != 0) {
            forwardSize--;
            backwardSize++;
            backPaths.add(forwardPaths.get(forwardSize));
            forwardPaths.remove(forwardSize);
            paint.invalidate();
        }
        resetUndoRedo();
    }
    public void clearRedo() {
        forwardSize=0;
        forwardPaths.clear();
        resetUndoRedo();
    }
    public void resetUndoRedo (){
        undoFab.setEnabled(backwardSize != 0);
        redoFab.setEnabled(forwardSize != 0);
    }

    public Iterator<Path> getBackPaths() {
        return  backPaths.iterator();
    }

    public Iterator<Path> getForwardPaths() {
        return forwardPaths.iterator();
    }

    public int getMode() {
        return mode;
    }


    public DrawViewManager with(FloatingActionButton undo, FloatingActionButton redo) {
        this.undoFab = undo ;
        this.redoFab = redo ;
        resetUndoRedo();
        return this;
    }

    public Bitmap save() {
        return paint.save();
    }

    public void addBack(Path mPath) {
        backPaths.add(mPath);
        backwardSize++;
        resetUndoRedo();

    }
}
