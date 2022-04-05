package com.arabic.math.solver.drawview;

import android.graphics.Bitmap;
import android.graphics.Path;

import androidx.core.util.Pair;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DrawViewManager {
    private final List<Path> backwardPaths;
    private final List<Pair<Character, Pair<Integer, Path>>> previousCmd, nextCmd;
    private final DrawView paint;
    private FloatingActionButton redoFab, undoFab;
    private int mode;

    public DrawViewManager(DrawView paint) {
        mode = DrawViewModes.DRAW;
        backwardPaths = new ArrayList<>();
        previousCmd = new ArrayList<>();
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

    public void push(Path mPath) {
        previousCmd.add(new Pair<>(Commands.ADD, new Pair<>(backwardPaths.size(), mPath)));
        backwardPaths.add(mPath);
        nextCmd.clear();
        resetUndoRedo();
    }

    public void pop(int i) {
        previousCmd.add(new Pair<>(Commands.DELETE, new Pair<>(i,backwardPaths.get(i) )));
        backwardPaths.remove(i);
        nextCmd.clear();
        resetUndoRedo();
    }

    public void resetUndoRedo() {
        undoFab.setEnabled(previousCmd.size() != 0);
        redoFab.setEnabled(nextCmd.size() != 0);
        paint.invalidate();
    }

    protected static class Commands {
        public static final char ADD = 'A';
        public static final char DELETE = 'D';
    }
}
