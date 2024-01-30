package vcall.free.international.phone.wifi.calling.api

import android.util.Log
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.Response
import okhttp3.ResponseBody
import vcall.free.international.phone.wifi.calling.utils.desDecrypt
import vcall.free.international.phone.wifi.calling.utils.desEncrypt

/**
 * Created by lyf on 2020/9/14.
 */
class EncryptInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response: Response
        if (request.url().host() == "vcallfree.com") {
            val array = request.url().toString().split("?")
            val newRequest = request.newBuilder().url("${array[0]}?v=${desEncrypt(array[1])}")
            if (Api.token.isNotEmpty()) {
                newRequest.header("Authorization", "Bearer ${Api.token}")
            }
            val _response = chain.proceed(newRequest.build())
            val result = desDecrypt(_response.body()!!.string())
            response = _response.newBuilder()
                .body(ResponseBody.create(MediaType.parse("application/json"), result)).build()
        } else if (request.url().host() == "zwtestv.xyz") {
            val array = request.url().toString().split("?")
            val map = mapOf(
                "signup" to "signup",
                "addpoints" to "addPoints",
                "setphone" to "setPhone",
                "price" to "getPrice",
                "rates" to "getRates",
                "adclick" to "adClick",
                "param" to "adParam"
            )
            val m = array[1].split("&").find { it.startsWith("m=") }!!.split("=")[1]
            val newRequest = request.newBuilder().url("${Api.baseUrl}api/${map[m]}?${array[1].replace("m=${m}&","")}")
            if (Api.token.isNotEmpty()) {
                newRequest.header("Authorization", "Bearer ${Api.token}")
            }
            response = chain.proceed(newRequest.build())
//            val result = desDecrypt(_response.body()!!.string())
//            response = _response.newBuilder()
//                .body(ResponseBody.create(MediaType.parse("application/json"), result)).build()
        } else {
            response = chain.proceed(request)
        }
//00000000-25b8-7d11-0000-00000027b9f2
        val headers = response.headers()
        Log.d("EncryptInterceptor", "intercept: ${headers.toString()}")
        headers.get("CALL-TOKEN")?.let { Api.token = it }
        return response
    }
}