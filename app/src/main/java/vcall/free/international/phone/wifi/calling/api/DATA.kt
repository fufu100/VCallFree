package vcall.free.international.phone.wifi.calling.api

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import vcall.free.international.phone.wifi.calling.utils.AESUtils
import java.lang.Integer.min


open class Base<T>(
    val code:Int = -1,
    val message: String,
    val data:T
)/**
 * Created by lyf on 2020/4/29.
 */
@Parcelize
data class Contact(
    var phoneId: Long,
    var contractId: Long,
    var username: String,
    var phone: String,
    var photoId: Long
):Parcelable{
    override fun equals(other: Any?): Boolean {
        return other != null && (other as Contact).contractId == this.contractId
    }
}

@Parcelize
data class Record(
    val id:Long,
    val phoneId:Long,
    val contractId:Long,
    val phone:String,
    val iso:String,
    val code:String,
    val prefix:String,
    val username: String,
    val userPhoto:Long?,
    val addTime:Long,
    val duration:Long,
    val rate:Int,
    val coinCost:Int,
    val state:Int
):Parcelable

data class Country(
    val country: String,
    val iso:String,
    var code:String,
    val length:Int,
    val prefix:String,
    var isHot:Boolean = false
)

data class RateResp(
    val errcode:Int,
    val rates:List<Array<String>>
)

data class Server(
    val host:String,
    val port:String,
    val prefix:String,
    val enport:String
)

data class AdConfig(
    val title:String,
    val desc:String,
    val icon:String,
    val package_name:String
)

data class InviteCountryPoints(
    @SerializedName("3000")
    val _3000:Array<String>,
    @SerializedName("10000")
    val _10000:Array<String>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as InviteCountryPoints

        if (!_3000.contentEquals(other._3000)) return false
        if (!_10000.contentEquals(other._10000)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _3000.contentHashCode()
        result = 31 * result + _10000.contentHashCode()
        return result
    }
}

data class User(
    val sip:String,
    var points:Int,
    val passwd:String,
    val servers:List<Server>,
    val invite_points:Int,
    val offer_points:Int,
    val new_user:Int,
    val mode:String,
    val maxWheel:Int,
    val wheel_points:Int,
    val max_rate:Float,
    val reward_points:Int,
    val rewardInterval:Int,
    val maxReward:Int,
    val rewarded:Int,
    val wheelInterval:Int,
    val invite:String,
    val inviteCountryPoints:InviteCountryPoints,
    val max_video:Int,
    val video_interval:Int,
    var phone:String,
    val ads_config:AdConfig,
    var time:Long
){
    fun getDecryptSip():String{
        val key = Api.token.reversed().substring(0,min(16,Api.token.length)).padEnd(16,'0')
        return AESUtils.decrypt(key,sip)
    }

    fun getDecryptPasswd():String{
        val key = Api.token.reversed().substring(0,min(16,Api.token.length)).padEnd(16,'0')
        return AESUtils.decrypt(key,passwd)
    }
}

data class Price(
    val phone:String,
    val iso:String,
    val prefix:String,
    val routeName:String,
    val points:Int,
    val tz:String,
    val type:String,
    val geo:String,
    val carrier:String
)

data class AddPointsResp(
    val sip:String,
    val points:Int
)

data class AdResp(
    var ads:List<AdCategory>,
)

data class AdCategory(
    var adPlaceID:String,
    var adStatus:Int,
    var adSources:List<Ad>
)

data class Ad(
    var adSourceName:String,
    var adFormatType:String,
    var adPlaceID:String,
    var adStatus:Int
){
//    if()
}

data class IpInfo(
    var ip:String,
    var city:String,
    var region:String,
    var country:String,
    var loc:String,
    var org:String,
    var timezone:String
)

data class PreAddPoint(
    var points: Int, //预增积分值
    var key: String, //预增积分原始key，为MD5值，长度为32字符
    var winId: String, //game游戏时中奖的水果id
    var luckyKey: String, //game游戏时幸运奖的key
    var count: Int, //计数值，wheel、reward、luc
    var type: String = ""
)