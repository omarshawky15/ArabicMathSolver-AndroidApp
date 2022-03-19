package com.example.retrofitexpirement;

import android.net.Uri;

public class ClassifyImage {
    Uri imgUri ;

    public ClassifyImage(Uri imgUri) {
        this.imgUri = imgUri;
    }

    public Uri getImgUri() {
        return imgUri;
    }

    public void setImgUri(Uri imgUri) {
        this.imgUri = imgUri;
    }
}
