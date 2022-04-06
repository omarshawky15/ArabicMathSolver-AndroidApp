package com.arabic.math.solver.drawview;

import android.graphics.Bitmap;
import android.graphics.Path;

import androidx.core.util.Pair;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class DrawViewManager {
    private final List<Path> backwardPaths;
    private final List<Pair<Character, Pair<Integer, Path>>> previousCmd, nextCmd;
    private final HashMap<Path,Integer> pathRefs ;
    private final DrawView paint;
    private FloatingActionButton redoFab, undoFab;
    private int mode;

    public DrawViewManager(DrawView paint) {
        mode = DrawViewModes.DRAW;
        backwardPaths = new ArrayList<>();
        previousCmd = new ArrayList<>();
        pathRefs = new HashMap<>();
        nextCmd = new ArrayList<>();
        mode = DrawViewModes.DRAW;
        this.paint = paint;
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
        int idx = previousCmd.size() - 1;
        if (idx >= 0) {
            if (previousCmd.get(idx).first == Commands.DELETE) {
                backwardPaths.add(previousCmd.get(idx).second.first, previousCmd.get(idx).second.second);
            } else {
                backwardPaths.remove((int) previousCmd.get(idx).second.first);
            }
            nextCmd.add(previousCmd.get(idx));
            previousCmd.remove(idx);
            resetUndoRedo();
        }
    }

    public void redo() {
        int idx = nextCmd.size() - 1;
        if (idx >= 0) {
            if (nextCmd.get(idx).first == Commands.ADD) {
                backwardPaths.add(nextCmd.get(idx).second.first, nextCmd.get(idx).second.second);
            } else {
                backwardPaths.remove((int) nextCmd.get(idx).second.first);
            }
            previousCmd.add(nextCmd.get(idx));
            nextCmd.remove(idx);
            resetUndoRedo();
        }
    }

    public Iterator<Path> getPathRefsIter() {
        return pathRefs.keySet().iterator();
    }

    public List<Path> getBackwardPaths() {
        return Collections.unmodifiableList(backwardPaths);
    }

    public int getMode() {
        return mode;
    }


    public DrawViewManager with(FloatingActionButton undo, FloatingActionButton redo) {
        this.undoFab = undo;
        this.redoFab = redo;
        resetUndoRedo();
        return this;
    }

    public Bitmap save() {
        return paint.save();
    }

    protected void push(Path mPath) {
        previousCmd.add(new Pair<>(Commands.ADD, new Pair<>(backwardPaths.size(), mPath)));
        backwardPaths.add(mPath);
        pathRefs.put(mPath,1);
        clearNextCmd();
        resetUndoRedo();
    }

    protected void pop(int i) {
        previousCmd.add(new Pair<>(Commands.DELETE, new Pair<>(i,backwardPaths.get(i) )));
        pathRefs.put(backwardPaths.get(i),2);
        backwardPaths.remove(i);
        clearNextCmd();
        resetUndoRedo();
    }

    private void resetUndoRedo() {
        undoFab.setEnabled(previousCmd.size() != 0);
        redoFab.setEnabled(nextCmd.size() != 0);
        paint.invalidate();
    }
    private void clearNextCmd(){
        for (Pair<Character, Pair<Integer, Path>> i : nextCmd){
            int count = pathRefs.get(i.second.second);
            if(count==1) {
                pathRefs.remove(i.second.second);
            }
            else pathRefs.put(i.second.second,count-1);
        }
        nextCmd.clear();
    }
    protected static class Commands {
        public static final char ADD = 'A';
        public static final char DELETE = 'D';
    }
}
