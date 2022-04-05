package com.arabic.math.solver.drawview;

import android.graphics.Bitmap;
import android.graphics.Path;

import androidx.core.util.Pair;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DrawViewManager {
    private final List<Path> backwardPaths,forwardPaths;
    private final List<Pair<Character,Pair<Integer,Path>>> previousCmd , nextCmd;
    private int mode;
    private final DrawView paint;
    private FloatingActionButton redoFab , undoFab;
    private int backwardSize ,forwardSize;
    public DrawViewManager(DrawView paint){
        mode = DrawViewModes.DRAW;
        backwardPaths = new ArrayList<>();
        forwardPaths = new ArrayList<>();
        previousCmd = new ArrayList<>();
        nextCmd = new ArrayList<>();
        backwardSize=0 ;
        forwardSize =0 ;
        mode= DrawViewModes.DRAW;
        this.paint = paint ;
    }

    public boolean isMoveMode() {
        return mode < DrawViewModes.DRAW && mode >= DrawViewModes.NONE;
    }

    public boolean isErase() {
        return mode == DrawViewModes.ERASE;
    }
    public void setMode(int mode) {
        this.mode = mode;
        paint.resetMatrices();
    }

    public void undo() {
        if (backwardSize!= 0) {
            backwardSize--;
            forwardSize++;
            forwardPaths.add(backwardPaths.get(backwardSize));
            backwardPaths.remove(backwardSize);
//            previousCmd.add(new Pair<>('U',new Pair<>(backwardSize)))
            paint.invalidate();
        }
        resetUndoRedo();
    }


    public void redo() {
        if (forwardSize != 0) {
            forwardSize--;
            backwardSize++;
            backwardPaths.add(forwardPaths.get(forwardSize));
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

    public List<Path> getBackwardPaths() {
        return  Collections.unmodifiableList(backwardPaths);
    }

    public List<Path> getForwardPaths() {
        return  Collections.unmodifiableList(forwardPaths);
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

    public void push(Path mPath) {
        backwardPaths.add(mPath);
        backwardSize++;
        resetUndoRedo();

    }

    public void pop(int i) {

    }
}
