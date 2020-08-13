package com.zantrik.drivesafe;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface JsonPlaceHolderApi {

    @FormUrlEncoded
    @POST("api/Driver/UserLogin")
    Call<LoginReq>  createPost(@Field("Phone") String Phone);

    @FormUrlEncoded
    @POST("api/Driver/Updateuserprofile")
    Call<UpdateprofileReq>  updateProfile(@Field("Phone") String Phone, @Field("userid") int userid,
                                          @Field("Name") String Name, @Field("NidNum") String NidNum);

    @FormUrlEncoded
    @POST("api/Driver/Getuserprofile")
    Call<GetprofileReq>  getProfile(@Field("Phone") String Phone, @Field("userid") int userid);

    @FormUrlEncoded
    @POST("api/Driver/Gettripinfo")
    Call<GettripinfoReq>  getTripinfo(@Field("Phone") String Phone, @Field("userid") int userid);

    @FormUrlEncoded
    @POST("api/Driver/UpdatetripInfo")
    Call<UpdatetripinfoReq>  updateTripinfo(@Field("Phone") String Phone, @Field("userid") int userid,
                                            @Field("numDrowsiness") int numDrowsiness, @Field("numPhone") int numPhone,
                                            @Field("tripscore") Double tripscore, @Field("triplength") Double triplength);
}