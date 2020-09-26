package vcall.free.international.phone.wifi.calling.api

import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.Response
import okhttp3.ResponseBody
import vcall.free.international.phone.wifi.calling.utils.desDecrypt
import vcall.free.international.phone.wifi.calling.utils.desEncrypt

/**
 * Created by lyf on 2020/9/14.
 */
class EncryptInterceptor:Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response:Response
        if(request.url().host() == "vcallfree.com"){
            val array = request.url().toString().split("?")
            val newRequest = request.newBuilder().url("${array[0]}?v=${desEncrypt(array[1])}").build()
            val _response = chain.proceed(newRequest)
            val result = desDecrypt(_response.body()!!.string())
            response = _response.newBuilder().body(ResponseBody.create(MediaType.parse("application/json"),result)).build()
        }else {
            response = chain.proceed(request)
        }
        return response
    }
}