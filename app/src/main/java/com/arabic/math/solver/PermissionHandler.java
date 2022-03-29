package com.arabic.math.solver;

import android.content.Context;
import android.content.pm.PackageManager;

import androidx.activity.result.ActivityResultCallback;
import androidx.core.app.ActivityCompat;

public class PermissionHandler<O> implements ActivityResultCallback<O> {
    private ActivityResultCallback<O> callback = null;

    public PermissionHandler() {
    }

    public PermissionHandler(ActivityResultCallback<O> mCallback) {
        this.callback = mCallback;
    }

    @Override
    public void onActivityResult(O result) {
        if (this.callback != null)
            this.callback.onActivityResult(result);
    }

    public void setCallback(ActivityResultCallback<O> mCallback) {
            this.callback = mCallback;
    }
    public static boolean checkPermissions(String[] permissions, Context context){
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(context,
                    permission) != PackageManager.PERMISSION_GRANTED) return false;
        }
        return true;
    }
}
