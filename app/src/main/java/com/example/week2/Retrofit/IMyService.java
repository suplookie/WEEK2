package com.example.week2.Retrofit;

import androidx.annotation.Keep;

import java.io.File;

import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.HTTP;
import retrofit2.http.POST;
import retrofit2.http.GET;
import retrofit2.http.PUT;

public interface IMyService {
    @POST("register")
    @FormUrlEncoded
    Observable<String> registerUser(@Field("email") String email,
                                    @Field("name") String name,
                                    @Field("password") String password);

    @POST("login")
    @FormUrlEncoded
    Observable<String> loginUser(@Field("email") String email,
                                    @Field("password") String password);
    //@DELETE("delete")
    //@FormUrlEncoded
    //Observable<String> deleteUser(@Field("email") String email, @Field("password") String password);

    @FormUrlEncoded
    @HTTP(method = "DELETE", path = "/delete", hasBody = true)
    Observable<String> deleteUser(@Field("email") String email, @Field("password") String password);

    @POST("upload")
    @FormUrlEncoded
    Observable<String> uploadImage(@Field("image") File file);


}
