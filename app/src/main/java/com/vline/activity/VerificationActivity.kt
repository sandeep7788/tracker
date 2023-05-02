package com.vline.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cn.pedant.SweetAlert.SweetAlertDialog
import com.alimuzaffar.lib.pin.PinEntryEditText
import com.google.gson.JsonObject
import com.mukeshsolanki.OnOtpCompletionListener
import com.vline.MainActivity
import com.vline.R
import com.vline.SignInActivity
import com.vline.helper.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class VerificationActivity : AppCompatActivity(), View.OnClickListener, OnOtpCompletionListener {

    private var validateButton: Button? = null
    private var number: TextView? = null
    private var pinEntry: PinEntryEditText? = null
    lateinit var context:Context
    private var progressDialog: SweetAlertDialog? = null
    private var TAG = "@@VerificationActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verification)
        context=this@VerificationActivity

        progressDialog = SweetAlertDialog(this@VerificationActivity, SweetAlertDialog.PROGRESS_TYPE)
        progressDialog!!.progressHelper.barColor = R.color.theme_color
        progressDialog!!.titleText = "Loading ..."
        progressDialog!!.setCancelable(false)

        initializeUi()
        setListeners()

        number?.text = "Enter the OTP sent to +91 "+ MyApplication.ReadIntPreferences(ApiContants.PREF_WhatsAppNumber).toString()


//        if (pinEntry != null) {
//            pinEntry!!.setOnPinEnteredListener { str ->
//                if (str.toString() == "1234") {
//                    Toast.makeText(
//                        this@VerificationActivity,
//                        "SUCCESS",
//                        Toast.LENGTH_SHORT
//                    )
//                        .show()
//
//                    val mainIntent = Intent(this@VerificationActivity, MainActivity::class.java)
//                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//                    this@VerificationActivity.startActivity(mainIntent)
//                    this@VerificationActivity.finish()
//                } else {
//                    Toast.makeText(this@VerificationActivity, "FAIL", Toast.LENGTH_SHORT)
//                        .show()
//                    pinEntry!!.setText(null)
//                }
//            }
//        }
    }

    private fun initializeUi() {
        pinEntry = findViewById<PinEntryEditText>(R.id.otp_view)
        validateButton = findViewById<Button>(R.id.validate_button)
        number = findViewById<TextView>(R.id.number)
    }

    private fun setListeners() {
        validateButton!!.setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        if (v?.id == R.id.validate_button) {
            Toast.makeText(this, pinEntry?.text, Toast.LENGTH_SHORT).show()
            loginByOtp()
        }
    }

    override fun onOtpCompleted(otp: String?) {
//        loginByOtp()
//        Toast.makeText(this, "OnOtpCompletionListener called", Toast.LENGTH_SHORT).show()
    }

    private fun loginByOtp() {
        progressDialog!!.show()
        val apiInterface: ApiInterface? =
            RetrofitManager().instanceNew(this@VerificationActivity)?.create(ApiInterface::class.java)

        apiInterface!!.loginOtp(
            MyApplication.ReadIntPreferences(ApiContants.PREF_WhatsAppNumber),
            pinEntry?.text.toString()
        ).enqueue(object : Callback<JsonObject> {
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {

                if (Utility.isNetworkAvailable(context!!)) {
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

                        val authorisationJsonObject =
                            JSONObject(response.body().toString()).getJSONObject("authorisation")




                        MyApplication.writeStringPreference(
                            ApiContants.PREF_Token, authorisationJsonObject.getString("token")
                        )
                        MyApplication.writeStringPreference(
                            ApiContants.PREF_type, authorisationJsonObject.getString("type")
                        )


                        if (jsonObject.has("id")) {

                            var data = jsonObject

                            Utility.showSnackBar(this@VerificationActivity,
                                "Welcome " + data.getString("name").toString()
                            )

                            MyApplication.writeIntPreference(
                                ApiContants.PREF_USER_ID, data.getInt("id")
                            )
                            MyApplication.writeStringPreference(
                                ApiContants.PREF_USER_NAME, data.getString("name")
                            )
                            MyApplication.writeIntPreference(
                                ApiContants.PREF_WhatsAppNumber, data.getInt("mobile")
                            )

                            MyApplication.writeStringPreference(
                                ApiContants.login, "true"
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



                            startActivity(Intent(context, MainActivity::class.java))
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