package com.vline.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.vline.R
import com.vline.databinding.ActivityLogBinding


class LogActivity : AppCompatActivity() {
    lateinit var binding: ActivityLogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_log)


        if (intent.extras!=null) {
            val intent = intent
            val args = intent.getBundleExtra("BUNDLE")
            val list = args!!.getSerializable("ARRAYLIST") as ArrayList<String>?

            list!!.forEach {
                binding.text.setText(it.toString()+"\n")
            }
        }



    }
}