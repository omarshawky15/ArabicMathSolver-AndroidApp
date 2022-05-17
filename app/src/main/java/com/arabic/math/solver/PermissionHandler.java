package com.arabic.math.solver;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.activity.result.ActivityResultCallback;
import androidx.core.app.ActivityCompat;

public class PermissionHandler<O> implements ActivityResultCallback<O> {
    private ActivityResultCallback<O> callback = null;
    private String[] permissionsNeeded;
    public PermissionHandler() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O_MR1) {
            permissionsNeeded = new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.INTERNET
            };
        } else {
            permissionsNeeded = new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.INTERNET
            };
        }

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
    public boolean checkPermissions(Context context){
        for (String permission : permissionsNeeded) {
            if (ActivityCompat.checkSelfPermission(context,
                    permission) != PackageManager.PERMISSION_GRANTED) return false;
        }
        return true;
    }

    public String[] getPermissionsNeeded() {
        return permissionsNeeded;
    }

    public ActivityResultCallback<O> setPermissionNeeded(String[] permissionsNeeded) {
        this.permissionsNeeded = permissionsNeeded;
        return this ;
    }
}
