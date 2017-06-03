package com.edge.weather;

import com.google.gson.JsonObject;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.QueryMap;

/**
 * Created by kim on 2017. 5. 12..
 */

public interface ApiService {
    static final String API_URL ="http://apis.skplanetx.com/weather/";
    static final String APP_KEY="caff2e9b-5e2f-304c-b845-36addb68fca6";
    static final String VERSION="1";
    @GET("current/minutely")
    Call<JsonObject> minWeather(@Header("appKey")String appkey,@QueryMap Map<String, String> options);
    @GET("current/hourly")
    Call<JsonObject> hourWeather(@Header("appKey")String appkey,@QueryMap Map<String, String> options);
    @GET("forecast/3days")
    Call<JsonObject> shortWeather(@Header("appKey")String appkey,@QueryMap Map<String, String> options);
    @GET("forecast/6days")
    Call<JsonObject> longWeather(@Header("appKey")String appkey,@QueryMap Map<String, String> options);
    @GET("summary")
    Call<JsonObject> summaryWeather(@Header("appKey")String appkey,@QueryMap Map<String, String> options);
}
