package com.vline.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import androidx.databinding.DataBindingUtil
import com.vline.MainActivity
import com.vline.R
import com.vline.SignInActivity
import com.vline.databinding.ActivitySplashScreenBinding
import com.vline.helper.ApiContants
import com.vline.helper.MyApplication


class SplashScreen : AppCompatActivity() {
    lateinit var binding: ActivitySplashScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash_screen)


        Handler().postDelayed(Runnable {

            if(MyApplication.ReadStringPreferences(ApiContants.login).equals("true",true)){
                val mainIntent = Intent(this@SplashScreen, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                this@SplashScreen.startActivity(mainIntent)
                this@SplashScreen.finish()
            }else {
                val mainIntent = Intent(this@SplashScreen, SignInActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                this@SplashScreen.startActivity(mainIntent)
                this@SplashScreen.finish()
            }
        },3000)

    }
}