package vcall.free.international.phone.wifi.calling.api

import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @GET("vos.php?m=signup&pk=vcall.free.international.phone.wifi.calling")
    @Streaming
    fun signup(@QueryMap map:MutableMap<String,String>):Observable<User>


    @GET("vos.php?m=rates")
    fun getCallRates(@Query("iso") iso:String):Observable<RateResp>

    @GET("vos.php?m=price")
    fun getPrice(@Query("iso") iso:String,@Query("phone") phone:String?):Observable<Price>

    @GET("vos.php?m=addpoints")
    fun addPoints(@QueryMap map:MutableMap<String,Any>):Observable<AddPointsResp>


    @GET("vos.php?m=setphone")
    fun bindPhone(@Query("sip") iso:String,@Query("phone") phone:String?):Observable<Response<ResponseBody>>

    @GET("vos.php?m=param")
    fun getAd():Observable<AdResp>

    @GET("vos.php?m=adclick")
    fun addClick(@QueryMap map:MutableMap<String,String>):Observable<Response<ResponseBody>>

    @GET("http://ipinfo.io/json")
    fun getIpInfo():Observable<IpInfo>
}