package com.vline

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_BACKGROUND_LOCATION
    )
    private val LOCATION_PERMISSION_CODE = 101
    private val BACKGROUND_LOCATION_PERMISSION_CODE = 102

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

//        ActivityCompat.requestPermissions(
//            this@SignInActivity, arr, LOCATION_PERMISSION_CODE
//        )

        checkPermission()
    }
    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(
                this@SignInActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Fine Location permission is granted
            // Check if current android version >= 11, if >= 11 check for Background Location permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (ContextCompat.checkSelfPermission(
                        this@SignInActivity,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    // Background Location Permission is granted so do your work here
                } else {
                    // Ask for Background Location Permission
                    askPermissionForBackgroundUsage()
                }
            }
        } else {
            // Fine Location Permission is not granted so ask for permission
            askForLocationPermission()
        }
    }
    private fun askForLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this@SignInActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            AlertDialog.Builder(this)
                .setTitle("Permission Needed!")
                .setMessage("Location Permission Needed!")
                .setPositiveButton("OK",
                    DialogInterface.OnClickListener { dialog, which ->
                        ActivityCompat.requestPermissions(
                            this@SignInActivity, arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ), LOCATION_PERMISSION_CODE
                        )
                    })
                .setNegativeButton("CANCEL", DialogInterface.OnClickListener { dialog, which ->
                    // Permission is denied by the user
                })
                .create().show()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_CODE
            )
        }
    }

    private fun askPermissionForBackgroundUsage() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this@SignInActivity,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        ) {
            AlertDialog.Builder(this)
                .setTitle("Permission Needed!")
                .setMessage("Background Location Permission Needed!, tap \"Allow all time in the next screen\"")
                .setPositiveButton(
                    "OK"
                ) { dialog, which ->
                    ActivityCompat.requestPermissions(
                        this@SignInActivity,
                        arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                        BACKGROUND_LOCATION_PERMISSION_CODE
                    )
                }
                .setNegativeButton(
                    "CANCEL"
                ) { dialog, which ->
                    // User declined for Background Location Permission.
                }
                .create().show()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                BACKGROUND_LOCATION_PERMISSION_CODE
            )
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // User granted location permission
                // Now check if android version >= 11, if >= 11 check for Background Location Permission
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (ContextCompat.checkSelfPermission(this@SignInActivity, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        // Background Location Permission is granted so do your work here

                    } else {
                        // Ask for Background Location Permission
                        askPermissionForBackgroundUsage();
                    }
                }
            } else {
                // User denied location permission
            }
        } else if (requestCode == BACKGROUND_LOCATION_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // User granted for Background Location Permission.
            } else {
                // User declined for Background Location Permission.
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