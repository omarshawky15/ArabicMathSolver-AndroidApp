package com.arabic.math.solver.retrofit;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface ClassifyAPI {
    @Multipart
    @POST("/{param}")
    Call<Classification> classify(@Part MultipartBody.Part file, @Path("param") String value);
}
