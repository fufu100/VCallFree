package vcall.free.international.phone.wifi.calling.api

import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @GET("api/user/signup")
    fun signup(@QueryMap map:MutableMap<String,String>):Observable<Base<User>>

    @GET("api/point/rates")
    fun getCallRates(@Query("iso") iso:String):Observable<Base<List<Array<String>>>>

    @GET("api/point/price")
    fun getPrice(@Query("iso") iso:String,@Query("phone") phone:String?):Observable<Base<Price>>

    @POST("api/point/preAdd/{type}")
    suspend fun preAddPoint(@Path("type") type: String):Base<PreAddPoint>

    @GET("api/point/add")
    fun addPoints(@Query("key") key:String):Observable<Base<AddPointsResp>>


    @GET("api/user/setPhone")
    fun bindPhone(@Query("phone") phone:String?):Observable<Base<String>>

    @GET("api/ad/param")
    fun getAd(@Query("firstInstallTime") firstInstallTime:String):Observable<Base<AdResp>>

    @GET("vos.php?m=adclick")
    fun addClick(@QueryMap map:MutableMap<String,String>):Observable<Response<ResponseBody>>

    @GET("http://ipinfo.io/json")
    fun getIpInfo():Observable<IpInfo>
}