package com.arabic.math.solver;

import java.io.File;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Retrofiter {
    private final static String BASE_URL = "https://sample-deploy12.herokuapp.com/";
    private static Retrofit retrofit = null;
    private Retrofiter(){};
    public static Retrofit getInstance() {
        if (retrofit==null) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(interceptor)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .build();
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
        }
        return retrofit;
    }
    public static void upload_classify(File file, Callback<Classification> callback) {
        ClassifyAPI service = Retrofiter.getInstance().create(ClassifyAPI.class);
        RequestBody requestFile =
                RequestBody.create(file, MediaType.parse("image/png"));
        MultipartBody.Part body =
                MultipartBody.Part.createFormData("file", file.getName(), requestFile);
        service.classify(body).enqueue(callback);
    }

}