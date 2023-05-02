package com.vline.helper

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.ConnectivityManager
import android.provider.Settings
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.gson.GsonBuilder
import com.vline.R
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager


class RetrofitManager {
    private var retrofit: Retrofit? = null


    val instance: Retrofit?
        get() {
            if (retrofit == null) {


                synchronized(RetrofitManager::class.java) {
                    val gson = GsonBuilder()
                        .setLenient()
                        .create()
                    retrofit = Retrofit.Builder()
                        .baseUrl(ApiContants.PREF_base_url)
                        .client(okHttpClient)
                        .addConverterFactory(GsonConverterFactory.create(gson))
                        .build()
                }
            }
            return retrofit
        }


    fun showDialog(context: Activity) {

        val pDialog = SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
        pDialog.titleText = "कोई इंटरनेट कनेक्शन नहीं"
        pDialog.contentText = "सुनिश्चित करें कि WI-FI या मोबाइल डेटा चालू है, फिर पुनः प्रयास करें"
        pDialog.confirmText = context.getString(R.string.retry)
        pDialog.cancelText = context.getString(R.string.cancel)
        pDialog.progressHelper.barColor = Color.parseColor("#02639C")
        pDialog.setCancelClickListener {
            context.finish()
        }
        pDialog.setConfirmClickListener {
            if (isNetworkAvailable(context)) {
                pDialog.dismiss()
            } else {
                val intent = Intent(Settings.ACTION_DATA_ROAMING_SETTINGS)
                context.startActivity(intent)
            }

        }
        pDialog.show()
//        pDialog.getButton(SweetAlertDialog.BUTTON_CONFIRM)
//            .setBackgroundColor(Color.parseColor("#FFE8560D"))

    }


    fun instanceNew(context: Activity): Retrofit? {

        if (!isNetworkAvailable(context)) {
            showDialog(context)
//            return null
        }

        if (retrofit == null) {

            synchronized(RetrofitManager::class.java) {
                val gson = GsonBuilder()
                    .setLenient()
                    .create()
                retrofit = Retrofit.Builder()
                    .baseUrl(ApiContants.PREF_base_url)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build()
            }
        }
        return retrofit
    }

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return connectivityManager.activeNetworkInfo != null && connectivityManager.activeNetworkInfo!!
            .isConnected
    }



    private val okHttpClient = unsafeOkHttpClient.connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val builder = chain.request().newBuilder()
            builder.addHeader("content-type", "application/json")
//            builder.addHeader("Content-Length", "")
//            builder.addHeader("Access-Control-Allow-Origin", "api")
//            builder.addHeader("APPKEY", "JsfnZGWj20NJMIyg2LDIvQ==")
            chain.proceed(builder.build())
        }

        .addNetworkInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        .build()
    private var newRetrofit: Retrofit? = null
    val client1: Retrofit?
        get() {
            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BODY
            val client =
                unsafeOkHttpClient.addInterceptor(interceptor).readTimeout(80, TimeUnit.SECONDS)
                    .connectTimeout(80, TimeUnit.SECONDS).build()
            val gson = GsonBuilder().serializeNulls().create()
            newRetrofit = Retrofit.Builder()
                .baseUrl("url")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client)
                .build()
            return newRetrofit
        }

    val googleClient: Retrofit
        get() {
            val gson = GsonBuilder()
                .setLenient()
                .create()
            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BODY
            val client =
                unsafeOkHttpClient.addInterceptor(interceptor).readTimeout(80, TimeUnit.SECONDS)
                    .connectTimeout(80, TimeUnit.SECONDS).build()
            return Retrofit.Builder()
                .baseUrl("https://www.googleapis.com/youtube/v3/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client)
                .build()
        }

    // Create a trust manager that does not validate certificate chains
    val unsafeOkHttpClient: OkHttpClient
    .Builder
        get() = try {
            // Create a trust manager that does not validate certificate chains
            val trustAllCerts = arrayOf<TrustManager>(
                object : X509TrustManager {
                    @Throws(CertificateException::class)
                    override fun checkClientTrusted(
                        chain: Array<X509Certificate>,
                        authType: String
                    ) {
                    }

                    @Throws(CertificateException::class)
                    override fun checkServerTrusted(
                        chain: Array<X509Certificate>,
                        authType: String
                    ) {
                    }

                    override fun getAcceptedIssuers(): Array<X509Certificate> {
                        return arrayOf()
                    }
                }
            )

            // Install the all-trusting trust manager
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, SecureRandom())
            val sslSocketFactory = sslContext.socketFactory
            val builder = OkHttpClient.Builder()
            builder.sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
            builder.hostnameVerifier { hostname, session -> true }
            builder
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
}