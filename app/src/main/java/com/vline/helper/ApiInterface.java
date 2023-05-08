package com.vline.helper;

import com.google.gson.JsonObject;

import java.util.HashMap;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface ApiInterface {

    //    http://cbisolar.vidhyalaya.co.in/Api/Api/customerlogin?mobno=9828775444&password=1234&remember_token=1234
//    http://cbisolar.vidhyalaya.co.in/Api/Api/customerlogin?mobno="9828775444"&password="1234"&remember_token="1234"
    @FormUrlEncoded
    @POST(ApiContants.PREF_customer_login)
    Call<JsonObject> signIn(
            @Field("name") String mobno,
            @Field("a_number") String password,
            @Field("mobile") String mobile,
            @Field("bo_number") String bo_number);

    @FormUrlEncoded
    @POST(ApiContants.PREF_tracking)
    Call<JsonObject> location(
            @Field("user_id") String user_id,
            @Field("let") String let,
            @Field("long") String longitube,
            @Field("location") String location,
            @Field("status") String status);

    @FormUrlEncoded
    @POST(ApiContants.tracking_selfielist)
    Call<JsonObject> tracking_selfielist(@Field("user_id") String user_id);

    //    @FormUrlEncoded
//    @Headers("Content-Type: multipart/form-data")

    @FormUrlEncoded
    @POST(ApiContants.PREF_login)
    Call<JsonObject> loginOtp(@Field("mobile") String mobile,
                              @Field("password") String password);

    @POST(ApiContants.PREF_login)
    Call<JsonObject> loginOtp(@Body HashMap<String, String> body);

    @Multipart
    @POST("tracking/selfie")
    Call<JsonObject> image(
            @Part("user_id") RequestBody id,
            @Part("tracking_id") RequestBody tracking_id,
            @Part MultipartBody.Part file
    );
}