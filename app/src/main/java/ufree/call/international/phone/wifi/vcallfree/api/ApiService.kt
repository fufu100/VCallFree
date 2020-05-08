package ufree.call.international.phone.wifi.vcallfree.api

import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @GET("vos.php?m=signup&pk=ufree.call.international.phone.wifi.vcallfree")
    @Streaming
    fun signup(@Query("uuid") uuid:String):Observable<User>


    @GET("vos.php?m=rates")
    fun getCallRates(@Query("iso") iso:String):Observable<RateResp>

    @GET("vos.php?m=price")
    fun getPrice(@Query("iso") iso:String,@Query("phone") phone:String?):Observable<Price>
}