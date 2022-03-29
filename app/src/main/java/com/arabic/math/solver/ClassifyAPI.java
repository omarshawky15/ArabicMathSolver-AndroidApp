package com.arabic.math.solver;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ClassifyAPI {
    @Multipart
    @POST("/")
    Call<Classification> classify(@Part("description") RequestBody description,
                                @Part MultipartBody.Part file);
}
