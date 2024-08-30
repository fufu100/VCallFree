package vcall.free.international.phone.wifi.calling.api

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import vcall.free.international.phone.wifi.calling.BuildConfig
import vcall.free.international.phone.wifi.calling.lib.prefs
import java.util.concurrent.TimeUnit

object Api {
    const val baseUrl = "https://vcallfree.com/"
    private var mRetrofit:Retrofit? = null

    var ts:Long = 0L
//    var token = ""
    var token = prefs.getStringValue("token","")
        set(value) {
            field = value
            prefs.save("token",value)
            Log.d("Api", "重新设置token：$value")
        }

    fun getApiService():ApiService{
        if(mRetrofit == null) {
//            Log.d("Api","init mRetrofit--")
            val builder: OkHttpClient.Builder = OkHttpClient.Builder()
            builder.addInterceptor(EncryptInterceptor())
            if(BuildConfig.DEBUG) {
                val logging = HttpLoggingInterceptor()
                logging.level = HttpLoggingInterceptor.Level.BODY
                builder.addInterceptor(logging)
            }
            builder.connectTimeout(40, TimeUnit.SECONDS).readTimeout(40, TimeUnit.SECONDS)
            mRetrofit = Retrofit.Builder().client(builder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(baseUrl).build()
        }
        return mRetrofit!!.create(ApiService::class.java)
    }


}