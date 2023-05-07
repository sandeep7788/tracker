package com.vline

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.gson.JsonObject
import com.vline.activity.VerificationActivity
import com.vline.databinding.ActivitySignInBinding
import com.vline.helper.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding
    private var progressDialog: SweetAlertDialog? = null
    private var TAG = "@@SignInActivity"
    private var status_sakha = false
    private lateinit var context: Activity
    var arr = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_BACKGROUND_LOCATION,
        Manifest.permission.CAMERA
    )
    private val permissionCode = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)
        setContentView(R.layout.activity_sign_in)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_in)
        context = this@SignInActivity

        progressDialog = SweetAlertDialog(this@SignInActivity, SweetAlertDialog.PROGRESS_TYPE)
        progressDialog!!.progressHelper.barColor = R.color.theme_color
        progressDialog!!.titleText = "Loading ..."
        progressDialog!!.setCancelable(false)

        clickListener()
//        startActivity(Intent(context, VerificationActivity::class.java))

        ActivityCompat.requestPermissions(
            this@SignInActivity, arr, permissionCode
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == permissionCode) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
//                dialogPermission("background location")
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            } else if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
            } else {

//                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun clickListener() {

        binding.btnSignIn.setOnClickListener {
            binding.l1.setBackgroundResource(R.drawable.edit_txtbg)
            binding.l2.setBackgroundResource(R.drawable.edit_txtbg)
            when {
                binding.txtUsername.text.isEmpty() -> {
                    Utility.showSnackBar(this, "please enter your name")
                    binding.txtUsername.setBackgroundResource(R.drawable.edit_txt_error)
                    binding.l1.setBackgroundResource(R.drawable.edit_txt_error)
                }
                binding.txtANumber.text.isEmpty() -> {
                    Utility.showSnackBar(this, "please enter your A number")
                    binding.txtANumber.setBackgroundResource(R.drawable.edit_txt_error)
                    binding.l2.setBackgroundResource(R.drawable.edit_txt_error)
                }
                binding.txtBONumber.text.isEmpty() -> {
                    Utility.showSnackBar(this, "please enter your BO number")
                    binding.txtBONumber.setBackgroundResource(R.drawable.edit_txt_error)
                    binding.l3.setBackgroundResource(R.drawable.edit_txt_error)
                }
                binding.txtMobileNumber.text.isEmpty() -> {
                    Utility.showSnackBar(this, "please enter your mobile number")
                    binding.txtMobileNumber.setBackgroundResource(R.drawable.edit_txt_error)
                    binding.l4.setBackgroundResource(R.drawable.edit_txt_error)
                }
                binding.txtMobileNumber.text.length < 10 -> {
                    Utility.showSnackBar(this, "please enter valid mobile number")
                    binding.txtMobileNumber.setBackgroundResource(R.drawable.edit_txt_error)
                    binding.l4.setBackgroundResource(R.drawable.edit_txt_error)
                }
                else -> {
                    signIn()

//                    MyApplication.writeStringPreference(
//                        ApiContants.PREF_USER_NAME, binding.txtUsername.text.toString()
//                    )
//                    MyApplication.writeStringPreference(
//                        ApiContants.PREF_WhatsAppNumber, binding.txtMobileNumber.text.toString()
//                    )
//                    startActivity(Intent(context, VerificationActivity::class.java))
                }
            }
        }

        binding.btnSignUp.setOnClickListener {
            Utility.showSnackBar(this, "बाद में कोशिश करें।")
        }

        binding.btnPrivacyPolicy.setOnClickListener {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW, Uri.parse(ApiContants.PREF_privacypolicy)
                )
            )
        }
    }

    private fun signIn() {
        progressDialog!!.show()
        val apiInterface: ApiInterface? =
            RetrofitManager().instanceNew(context)?.create(ApiInterface::class.java)

        apiInterface!!.signIn(
            binding.txtUsername.text.toString().trim(),
            binding.txtANumber.text.toString().trim(),
            binding.txtMobileNumber.text.toString().trim(),
            binding.txtBONumber.text.toString().trim(),
        ).enqueue(object : Callback<JsonObject> {
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {

                if (Utility.isNetworkAvailable(context)) {
                    Toast.makeText(
                        context, " " + resources.getString(R.string.error), Toast.LENGTH_LONG
                    ).show()
                    Log.e(TAG, "onFailure: " + t.message)
                }
                progressDialog!!.dismiss()
            }

            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                progressDialog!!.dismiss()
                var msg: String? = null;
                try {
                    if (response.isSuccessful) {

                        Log.d(TAG, "onResponse: " + response.body().toString())
                        val jsonObject =
                            JSONObject(response.body().toString()).getJSONObject("user")

                        Utility.showSnackBar(
                            this@SignInActivity,
                            JSONObject(response.body().toString()).getString("message").toString()
                        )
                        if (jsonObject.has("id")) {

                            var data = jsonObject

                            MyApplication.writeIntPreference(
                                ApiContants.PREF_USER_ID, data.getInt("id")
                            )
                            MyApplication.writeStringPreference(
                                ApiContants.PREF_USER_NAME, data.getString("name")
                            )
                            MyApplication.writeStringPreference(
                                ApiContants.PREF_WhatsAppNumber, data.getString("mobile")
                            )

//                            MyApplication.writeStringPreference(
//                                ApiContants.PREF_role, data.getString("role_name")
//                            )
//                            MyApplication.writeIntPreference(
//                                ApiContants.PREF_nager, data.getInt("nager")
//                            )
//                            MyApplication.writeStringPreference(ApiContants.login, "true")
//                            MyApplication.writeBoolPreference(
//                                ApiContants.isMskUser, data.getInt("shaka") > 0
//                            )


                            startActivity(Intent(context, VerificationActivity::class.java))
                            finish()
                        } else {
                            Utility.showDialog(
                                context, SweetAlertDialog.WARNING_TYPE, "Something went wrong"
                            )
                        }
                    } else {
                        Utility.showDialog(
                            context,
                            SweetAlertDialog.WARNING_TYPE,
                            resources.getString(R.string.error)
                        )
                    }

                } catch (e: Exception) {
                    Utility.showDialog(
                        context, SweetAlertDialog.WARNING_TYPE, resources.getString(R.string.error)
                    )

                    Toast.makeText(
                        context, " " + resources.getString(R.string.error), Toast.LENGTH_LONG
                    ).show()
                }
            }

        })
    }
}