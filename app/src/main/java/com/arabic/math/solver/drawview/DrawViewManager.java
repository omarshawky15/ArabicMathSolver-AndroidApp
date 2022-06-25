package com.arabic.math.solver.drawview;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Path;
import android.view.View;
import android.widget.Button;

import androidx.core.util.Pair;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class DrawViewManager {
    private final List<Path> drawnPaths;
    private final List<Pair<Character, Path>> previousCmd, nextCmd;
    private final HashMap<Path, Integer> pathRefs;
    private final DrawView paint;
    private FloatingActionButton redoFab, undoFab;
    private Button clearBtn ;
    private int mode;
    public DrawViewManager(DrawView paint) {
        mode = DrawViewModes.DRAW;
        drawnPaths = new ArrayList<>();
        previousCmd = new ArrayList<>();
        pathRefs = new HashMap<>();
        nextCmd = new ArrayList<>();
        mode = DrawViewModes.DRAW;
        this.paint = paint;
    }

    public void scalePaths(Matrix scaleMatrix, Matrix inverse) {
        for (Iterator<Path> it = getPathRefsIter(); it.hasNext(); ) {
            Path i = it.next();
            i.transform(inverse);
            i.transform(scaleMatrix);
        }
    }

    public boolean isMoveMode() {
        return mode == DrawViewModes.MOVE;
    }

    public boolean isErase() {
        return mode == DrawViewModes.ERASE;
    }

    public void setMode(int mode) {
        this.mode = mode;
        resetButtons();
        paint.resetEraseFlag();
    }

    public void undo() {
        int idx = previousCmd.size() - 1;
        if (idx >= 0) {
            if (previousCmd.get(idx).first == Commands.DELETE) {
                drawnPaths.add(previousCmd.get(idx).second);
            } else {
                drawnPaths.remove(previousCmd.get(idx).second);
            }
            nextCmd.add(previousCmd.get(idx));
            previousCmd.remove(idx);
            resetButtons();
        }
    }

    public void redo() {
        int idx = nextCmd.size() - 1;
        if (idx >= 0) {
            if (nextCmd.get(idx).first == Commands.ADD) {
                drawnPaths.add(nextCmd.get(idx).second);
            } else {
                drawnPaths.remove(nextCmd.get(idx).second);
            }
            previousCmd.add(nextCmd.get(idx));
            nextCmd.remove(idx);
            resetButtons();
        }
    }

    public Iterator<Path> getPathRefsIter() {
        return pathRefs.keySet().iterator();
    }

    public List<Path> getDrawnPaths() {
        return Collections.unmodifiableList(drawnPaths);
    }

    public int getMode() {
        return mode;
    }


    public DrawViewManager withRedoUndo(FloatingActionButton undo, FloatingActionButton redo) {
        this.undoFab = undo;
        this.redoFab = redo;
        resetButtons();
        return this;
    }
    public DrawViewManager withClear(Button clearBtn) {
        this.clearBtn = clearBtn;
        return this;
    }


    public Bitmap save() {
        return paint.save();
    }

    public void push(Path mPath) {
        clearNextCmd();
        previousCmd.add(new Pair<>(Commands.ADD, mPath));
        drawnPaths.add(mPath);
        pathRefs.put(mPath, 1);
        resetButtons();
    }

    public void pop(int i) {
        clearNextCmd();
        previousCmd.add(new Pair<>(Commands.DELETE, drawnPaths.get(i)));
        pathRefs.put(drawnPaths.get(i), 2);
        drawnPaths.remove(i);
        resetButtons();
    }

    private void resetButtons() {
        if (undoFab != null) undoFab.setEnabled(previousCmd.size() != 0);
        if (redoFab != null) redoFab.setEnabled(nextCmd.size() != 0);
        if(clearBtn !=null) clearBtn.setVisibility(mode == DrawViewModes.ERASE && drawnPaths.size() !=0 ? View.VISIBLE:View.GONE);
        paint.invalidate();
    }

    private void clearNextCmd() {
        for (Pair<Character, Path> i : nextCmd) {
            int count = pathRefs.get(i.second);
            if (count == 1) {
                pathRefs.remove(i.second);
            } else pathRefs.put(i.second, count - 1);
        }
        nextCmd.clear();
    }

    public void deleteAll() {
        pathRefs.clear();
        drawnPaths.clear();
        nextCmd.clear();
        previousCmd.clear();
        resetButtons();
    }

    protected static class Commands {
        public static final char ADD = 'A';
        public static final char DELETE = 'D';
    }
}
