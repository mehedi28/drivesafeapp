package com.zantrik.drivesafe;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Api {
    private static Retrofit retrofit = null;
    public static JsonPlaceHolderApi getClient() {

        String BASE_URL = "http://13.250.100.234/";
        // change your base URL
        if (retrofit==null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        //Creating object for our interface
        JsonPlaceHolderApi api = retrofit.create(JsonPlaceHolderApi.class);
        return api; // return the APIInterface object
    }
}
