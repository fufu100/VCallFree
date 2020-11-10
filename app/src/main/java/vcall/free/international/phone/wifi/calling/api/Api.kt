package vcall.free.international.phone.wifi.calling.api

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import vcall.free.international.phone.wifi.calling.BuildConfig
import java.util.concurrent.TimeUnit

object Api {
    private const val baseUrl = "https://vcallfree.com"
    private var mRetrofit:Retrofit? = null

    fun getApiService():ApiService{
        if(mRetrofit == null) {
            Log.d("Api","init mRetrofit--")
            val builder: OkHttpClient.Builder = OkHttpClient.Builder()
            if(BuildConfig.DEBUG) {
                val logging = HttpLoggingInterceptor()
                logging.level = HttpLoggingInterceptor.Level.BODY
                builder.addInterceptor(logging)
            }
            builder.addInterceptor(EncryptInterceptor())
            builder.connectTimeout(40, TimeUnit.SECONDS).readTimeout(40, TimeUnit.SECONDS)
            mRetrofit = Retrofit.Builder().client(builder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(baseUrl).build()
        }
        return mRetrofit!!.create(ApiService::class.java)
    }


}