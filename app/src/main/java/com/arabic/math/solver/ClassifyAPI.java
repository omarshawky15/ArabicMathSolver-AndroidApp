package com.arabic.math.solver;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ClassifyAPI {
    @Multipart
    @POST("/")
    Call<Classification> classify(@Part MultipartBody.Part file);
}
