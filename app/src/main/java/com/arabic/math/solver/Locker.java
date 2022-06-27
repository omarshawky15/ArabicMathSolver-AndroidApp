package com.arabic.math.solver;

import android.util.Log;

public class Locker {
    private Integer counter;


    public Locker() {
        counter = 0;
    }

    public boolean lock() {
        synchronized (counter) {
            counter++;
        }
        Log.e("Lockcount", String.valueOf(counter));
        return counter == 0;
    }

    public boolean unlock() {
        synchronized (counter) {
            counter--;
        }
        Log.e("Unlockcount", String.valueOf(counter));
        return counter == 0;
    }
}
