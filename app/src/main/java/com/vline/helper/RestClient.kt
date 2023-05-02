package com.rss.suchi.helper

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.provider.Settings
import android.util.Log
import cn.pedant.SweetAlert.SweetAlertDialog
import com.vline.helper.ApiContants.PREF_base_url
import com.vline.helper.Utility.Companion.isNetworkAvailable
import com.google.gson.GsonBuilder
import com.vline.R
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

class RestClient {
    var retrofit: Retrofit? = null
    var connectionTimeout: Long = 0
    var readTimeout: Long = 0
    var writeTimeout: Long = 0
    var shouldRetryOnConnectionFailure = false

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


    fun getClient(context: Activity): Retrofit? {

        if (!isNetworkAvailable(context)) {
            showDialog(context)
        }
        var BASE_URL_1: String = PREF_base_url
        Log.e("::Retrofit:::", "getClient: $BASE_URL_1")
        if (retrofit == null) {
            Log.e("::Retrofit:::", "getClient11: $BASE_URL_1")
            val builder = OkHttpClient.Builder()
            val httpLoggingInterceptor = HttpLoggingInterceptor()
            httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
            builder.connectTimeout(connectionTimeout, TimeUnit.SECONDS)
                .readTimeout(readTimeout, TimeUnit.MINUTES)
                .writeTimeout(writeTimeout, TimeUnit.MINUTES)
                .retryOnConnectionFailure(shouldRetryOnConnectionFailure)
//                .addInterceptor(ChuckInterceptor(AppInitialization.getInstance()))
                .addInterceptor(httpLoggingInterceptor)
            val gson = GsonBuilder().setLenient().create()
            val okHttpClient = builder.build()
            retrofit = Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl(BASE_URL_1)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
        }
        return retrofit
    }

    fun getClient2(context: Activity): Retrofit? {

        if (!isNetworkAvailable(context)) {
            showDialog(context)
        }
        var BASE_URL_1: String = PREF_base_url
        Log.e("::Retrofit:::", "getClient: $BASE_URL_1")
        if (retrofit == null) {
            Log.e("::Retrofit:::", "getClient11: $BASE_URL_1")
            val builder = OkHttpClient.Builder()
            val httpLoggingInterceptor = HttpLoggingInterceptor()
            httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
            builder.connectTimeout(connectionTimeout, TimeUnit.SECONDS)
                .readTimeout(readTimeout, TimeUnit.MINUTES)
                .writeTimeout(writeTimeout, TimeUnit.MINUTES)
                .retryOnConnectionFailure(shouldRetryOnConnectionFailure)
//                .addInterceptor(ChuckInterceptor(AppInitialization.getInstance()))
                .addInterceptor(httpLoggingInterceptor)
            val gson = GsonBuilder().setLenient().create()
            val okHttpClient = builder.build()
            retrofit = Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl(BASE_URL_1)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
        }
        return retrofit
    }


}