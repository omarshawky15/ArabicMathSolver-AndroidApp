package com.arabic.math.solver;

import android.util.Log;

public class Locker {
    private int counter ;


    public Locker (){
            counter= 0;
    }
    public boolean lock(){
        counter++;
        Log.e("Lockcount", String.valueOf(counter));
        return counter == 0;
    }
    public boolean unlock(){
        counter--;
        Log.e("Unlockcount", String.valueOf(counter));
        return counter == 0;
    }
}
