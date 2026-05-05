package com.example.portfolioapp.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    //  Replace with  actual Render URL after deploying
    private static final String BASE_URL =
            "https://portfolio-backend-1-rruq.onrender.com/";
    private static Retrofit retrofit;
    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
    public static ApiService getService() {
        return getClient().create(ApiService.class);
    }
}