package com.vline


import android.Manifest
import android.Manifest.permission.*
import android.app.ActivityManager
import android.app.AlertDialog
import android.content.*
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import androidx.databinding.DataBindingUtil
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.gson.JsonObject
import com.vline.activity.SplashScreen
import com.vline.databinding.ActivityMainBinding
import com.vline.endless.*
import com.vline.helper.*
import com.vline.helper.MyApplication.Companion.count
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    private val TAG = "@@MainActivity"
    var textViewTimer: TextView? = null
    var buttonStart: Button? = null
    var buttonStop: Button? = null
    var arr = arrayOf(
        ACCESS_COARSE_LOCATION,
        ACCESS_FINE_LOCATION,
        ACCESS_BACKGROUND_LOCATION,
    )
    var list: ArrayList<String> = ArrayList()
    lateinit var context: Context
    var locStatus = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        context = this@MainActivity
        textViewTimer = binding.timeView
        buttonStart = binding.startButton
        buttonStop = binding.stopButton
        buttonStart?.visibility = View.VISIBLE
        progressDialog = SweetAlertDialog(this@MainActivity, SweetAlertDialog.PROGRESS_TYPE)
        progressDialog!!.progressHelper.barColor = R.color.theme_color
        progressDialog!!.titleText = "Loading ..."
        progressDialog!!.setCancelable(false)

        binding.txtStart.setText("")
        binding.txtCurrent.setText("")
        binding.name.setText(MyApplication.ReadStringPreferences(ApiContants.PREF_USER_NAME))
        binding.number.setText(MyApplication.ReadStringPreferences(ApiContants.PREF_WhatsAppNumber))

        buttonStop!!.setOnClickListener {
            actionOnService(Actions.STOP)
//            textViewTimer!!.setText("00:00:00")
//            binding.txtStart.setText("")
//            binding.txtCurrent.setText("")
            isStartedButton(false)
            setCurrent(true)
        }

        binding.logout.setOnClickListener { logout() }

        LocalBroadcastManager.getInstance(this).registerReceiver(
            mMessageReceiver, IntentFilter("GPSLocationUpdates")
        )

        buttonStart!!.setOnClickListener {

//            checkPermission()

            if (ActivityCompat.checkSelfPermission(
                    this, ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                dialogPermission("GPS location")
            } else if (ActivityCompat.checkSelfPermission(
                    this, ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                dialogPermission("current location finer")
            } else if (ActivityCompat.checkSelfPermission(
                    this, ACCESS_BACKGROUND_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q
            ) {
                backgroundPermission()


            } else if (!isLocationEnabled()) {
                ActivityCompat.requestPermissions(
                    this@MainActivity,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    103
                )
                startActivity(Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                Utility.showSnackBar(this@MainActivity, "Please turn on GPS")
            } else {
                if (!isMyServiceRunning(EndlessService::class.java)) {
//                    binding.txtStart.setText("")
                    textViewTimer!!.setText("00:00:00")
                    binding.txtCurrent.setText("")

                    isStartedButton(true)
                    actionOnService(Actions.START)
                    setLocation(true)
                }
            }
        }


//        ActivityCompat.requestPermissions(
//            this@MainActivity, arr, permissionCode
//        )
        checkPermission()

        setLocation(false)
    }

    private val LOCATION_PERMISSION_CODE = 101
    private val BACKGROUND_LOCATION_PERMISSION_CODE = 102
    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(
                this@MainActivity, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Fine Location permission is granted
            // Check if current android version >= 11, if >= 11 check for Background Location permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (ContextCompat.checkSelfPermission(
                        this@MainActivity, Manifest.permission.ACCESS_BACKGROUND_LOCATION
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

    private fun isLocationEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return LocationManagerCompat.isLocationEnabled(locationManager)
    }

    private fun askForLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this@MainActivity, Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            AlertDialog.Builder(this).setTitle("Permission Needed!")
                .setMessage("Location Permission Needed!")
                .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
                    ActivityCompat.requestPermissions(
                        this@MainActivity, arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ), LOCATION_PERMISSION_CODE
                    )
                }).setNegativeButton("CANCEL", DialogInterface.OnClickListener { dialog, which ->
                    // Permission is denied by the user
                }).create().show()
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_CODE
            )
        }
    }

    private fun askPermissionForBackgroundUsage() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this@MainActivity, Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        ) {
            AlertDialog.Builder(this).setTitle("Permission Needed!")
                .setMessage("Background Location Permission Needed!, tap \"Allow all time in the next screen\"")
                .setPositiveButton(
                    "OK"
                ) { dialog, which ->
                    ActivityCompat.requestPermissions(
                        this@MainActivity,
                        arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                        BACKGROUND_LOCATION_PERMISSION_CODE
                    )
                }.setNegativeButton(
                    "CANCEL"
                ) { dialog, which ->
                    // User declined for Background Location Permission.
                }.create().show()
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


        try {
            if (requestCode == LOCATION_PERMISSION_CODE) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // User granted location permission
                    // Now check if android version >= 11, if >= 11 check for Background Location Permission
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        if (ContextCompat.checkSelfPermission(
                                this@MainActivity, Manifest.permission.ACCESS_BACKGROUND_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
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
//            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                // User granted for Background Location Permission.
//            } else {
//                // User declined for Background Location Permission.
//            }
            }

        } catch (e: Exception) {

        }
    }


    var PERMISSION_REQUEST_BACKGROUND_LOCATION = 102
    var PERMISSION_REQUEST_FINE_LOCATION = 103

    fun backgroundPermission() {
        if (checkSelfPermission(ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (checkSelfPermission(ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(ACCESS_BACKGROUND_LOCATION)) {
//                if (false) {
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("This app needs background location access")
                    builder.setMessage("Please grant location access so this app can detect beacons in the background.")
                    builder.setPositiveButton(android.R.string.ok, null)
                    builder.setOnDismissListener {
                        requestPermissions(
                            arrayOf(ACCESS_BACKGROUND_LOCATION),
                            PERMISSION_REQUEST_BACKGROUND_LOCATION
                        )
                    }
                    builder.show()
                } else {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                        val builder = AlertDialog.Builder(this)
                        builder.setTitle("Functionality limited")
                        builder.setMessage("Since background location access has not been granted, this app will not be able to discover beacons in the background.  Please go to Settings -> Applications -> Permissions and grant background location access to this app.")
                        builder.setPositiveButton(android.R.string.ok,
                            DialogInterface.OnClickListener { dialogInterface, i ->
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                val uri: Uri = Uri.fromParts("package", packageName, null)
                                intent.data = uri
                                // This will take the user to a page where they have to click twice to drill down to grant the permission
                                startActivity(intent)
                            })
//
                        builder.show()
                    }
                }
            }
        } else {
            if (!shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION)) {
                requestPermissions(
                    arrayOf(
                        ACCESS_FINE_LOCATION
                        /*Manifest.permission.ACCESS_BACKGROUND_LOCATION*/
                    ), PERMISSION_REQUEST_FINE_LOCATION
                )
            } else {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Functionality limited")
                builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons.  Please go to Settings -> Applications -> Permissions and grant location access to this app.")
                builder.setPositiveButton(android.R.string.ok, null)
                builder.setOnDismissListener {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri: Uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    // This will take the user to a page where they have to click twice to drill down to grant the permission
                    startActivity(intent)
                }
                builder.show()
            }
        }
    }

    fun isStartedButton(it: Boolean) {
        if (it) {
            buttonStop?.visibility = View.VISIBLE
            buttonStart?.visibility = View.GONE
//            binding.log?.visibility = View.GONE
        } else {

            buttonStop?.visibility = View.GONE
            buttonStart?.visibility = View.VISIBLE
//            binding.log?.visibility = View.VISIBLE
        }
    }

    private var progressDialog: SweetAlertDialog? = null

    @NonNull
    private fun createPartFromString(value: String): RequestBody? {
        return RequestBody.create(
            MultipartBody.FORM, value
        )
    }

    var trackingId = 0


    fun sendLocation() {

        val apiInterface: ApiInterface? =
            RetrofitManager().instanceNew(this@MainActivity)?.create(ApiInterface::class.java)

        val userId = MyApplication.ReadIntPreferences(ApiContants.PREF_USER_ID).toString()

        var address = ""
        address = if (locStatus.equals("start", true)) {
            binding.txtStart.text.toString()
        } else {
            currentAddress
        }

        Log.e(
            TAG,
            "sendLocation: -" + userId.toString() + " -" + latitude.toString() + " -" + longitude.toString() + " -" + address + " -" + locStatus
        )
        apiInterface!!.location(
            userId.toString(), latitude.toString(), longitude.toString(), address, locStatus
        ).enqueue(object : Callback<JsonObject> {
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Log.e(TAG, "sendLocation onFailure: " + t.message)

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
                try {
                    if (response.isSuccessful) {


                        val msg = JSONObject(response.body().toString()).getString("message")

                        if (JSONObject(response.body().toString()).has("data")) {
                            trackingId =
                                JSONObject(response.body().toString()).getJSONObject("data")
                                    .getInt("id")
                        }

                        Log.d(TAG, "sendLocation onResponse: " + msg + " " + trackingId)
                        Utility.showSnackBar(this@MainActivity, msg)

                    } else {
                        Utility.showDialog(
                            context, SweetAlertDialog.WARNING_TYPE, "Something went wrong"
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

        runOnUiThread {
            if (locStatus.equals("runing", true)) {
                binding.txtCurrent.text = address
            }
        }
    }

    lateinit var gpsTracker: GPSTracker

    var alertDialog: AlertDialog? = null;

    override fun onResume() {
        try {
            if (alertDialog != null) {
                alertDialog?.dismiss()
            }
            if (progressDialog != null) {
                progressDialog?.dismiss()
            }
        } catch (e: Exception) {
        }
        super.onResume()
    }

    override fun onPause() {
        try {
            if (alertDialog != null) {
                alertDialog?.dismiss()
            }
            if (progressDialog != null) {
                progressDialog?.dismiss()
            }
        } catch (e: Exception) {
        }
        super.onPause()
    }

    override fun onDestroy() {
        try {
            if (alertDialog != null) {
                alertDialog?.dismiss()
            }
            if (progressDialog != null) {
                progressDialog?.dismiss()
            }
        } catch (e: Exception) {
        }
        super.onDestroy()
    }

    fun dialog() {

        val names = list
        val alertDialog = AlertDialog.Builder(this@MainActivity)
        val inflater = layoutInflater
        val convertView: View = inflater.inflate(R.layout.custom, null) as View
        alertDialog.setView(convertView)
        alertDialog.setTitle("Log's")
        alertDialog.setPositiveButton(
            "close",
            DialogInterface.OnClickListener { dialog, which -> dialog.dismiss() })
        val adapter = ArrayAdapter(this, R.layout.activity_list_item, names)
//        convertView.findViewById(R.id.lv).adapter = adapter
        val lv = convertView.findViewById<View>(R.id.lv) as ListView
        lv.setAdapter(adapter);
        alertDialog.show()
    }

    fun logout() {
        AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert)
            .setTitle("Closing Activity").setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, which ->
                MyApplication.clearPrefrences()
                startActivity(Intent(this, SplashScreen::class.java))
                finish()
            }).setNegativeButton("No", null).show()
    }

    fun setLocation(isCallApi: Boolean) {

        gpsTracker = GPSTracker(this)
        var country: String? = null
        if (isLocationPermissionGranted() && gpsTracker.getIsGPSTrackingEnabled()) {

            country = gpsTracker.getCountryName(this)
            val city = gpsTracker.getLocality(this)
            val postalCode = gpsTracker.getPostalCode(this)
            val addressLine = gpsTracker.getAddressLine(this)


            if (country != null) {
                binding.txtStart.text = "$addressLine $city $postalCode $country"
            }
            latitude = gpsTracker.latitude
            longitude = gpsTracker.longitude
        } else {
//            dialogPermission()
        }
        locStatus = "start"
        Log.e(TAG, "setLocation: $locStatus" + country)

        if (callCount < 3) {
            if (country.isNullOrEmpty()) {
                callCount = 0
                Handler().postDelayed(Runnable {
                    setLocation(isCallApi)
                }, 4000)
            } else if (isCallApi) {
                sendLocation()
            }
        }
    }

    var latitude = 0.0
    var longitude = 0.0
    var currentAddress = ""
    var callCount = 0
    fun setCurrent(isStop: Boolean) {
        callCount++
        if (isStop) {
            locStatus = "stop"
        } else {
            locStatus = "runing"
        }

        var country: String? = null
        gpsTracker = GPSTracker(this)
        if (isLocationPermissionGranted() && gpsTracker.getIsGPSTrackingEnabled()) {

            country = gpsTracker.getCountryName(this)
            val city = gpsTracker.getLocality(this)
            val postalCode = gpsTracker.getPostalCode(this)
            val addressLine = gpsTracker.getAddressLine(this)
            latitude = gpsTracker.latitude
            longitude = gpsTracker.longitude

            if ((locStatus.equals("runing", true) || locStatus.equals("stop", true))
                && country != null) {
                currentAddress = "$addressLine $city $postalCode $country"
            }

//            list.add(" - " + Utility.getCurrentTime() + " - $addressLine $city $postalCode $country")
            Log.e(TAG, "setCurrent: " + list.size)
        } else {
//            dialogPermission()
        }

        if (isStop) {
            sendLocation()
        } else if (callCount < 3)
            if (country.isNullOrEmpty()) {
                callCount = 0
                Handler().postDelayed(Runnable {
                    setCurrent(isStop)
                }, 4000)
            } else {
                callCount = 0
                sendLocation()
            }

    }

    private fun isLocationPermissionGranted(): Boolean {
        return if (ActivityCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ), permissionCode2
            )
            false
        } else {
            true
        }
    }


    private fun actionOnService(action: Actions) {
        if (getServiceState(this) == ServiceState.STOPPED && action == Actions.STOP) return
        Intent(this, EndlessService::class.java).also {
            it.action = action.name
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                log("Starting the service in >=26 Mode")
                startForegroundService(it)
                return
            }
            log("Starting the service in < 26 Mode")
            startService(it)
        }
    }

    private fun locationService(action: Actions) {
        if (getServiceState(this) == ServiceState.STOPPED && action == Actions.STOP) return
        Intent(this, GPSTracker::class.java).also {
            it.action = action.name
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                log("Starting the service in >=26 Mode")
                startForegroundService(it)
                return
            }
            log("Starting the service in < 26 Mode")
            startService(it)
        }
    }

    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }


    private val mMessageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // Get extra data included in the Intent
            Log.d("@@", "reciever....")
            val time = intent.getStringExtra("time")
//            val b = intent.getBundleExtra("Location")
            var timeSec = ""
            timeSec = time!!
            val hours = timeSec.toInt() / 3600
            var temp = timeSec.toInt() - hours * 3600
            val mins = temp / 60
            temp = temp - mins * 60
            val secs = temp

            var h = hours.toString()
            var m = mins.toString()
            var s = secs.toString()

            if (hours <= 9) h = "0" + hours.toString()
            if (mins <= 9) m = "0" + mins.toString()
            if (secs <= 9) s = "0" + secs.toString()


            val requiredFormat = "$h:$m:$s"
            Log.e("@@", "updateGUI: " + requiredFormat)

            binding.timeView.setText(requiredFormat)


            if (binding.txtStart.text.toString().contentEquals("null")) {
                setLocation(false)
            }

            val permissionStatus: Boolean =
                if (!isLocationEnabled()) {
                    false
                } else if (ActivityCompat.checkSelfPermission(
                        this@MainActivity, ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this@MainActivity,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        103
                    )
                    false
                } else if (ActivityCompat.checkSelfPermission(
                        this@MainActivity, ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    false
                } else if (ActivityCompat.checkSelfPermission(
                        this@MainActivity, ACCESS_BACKGROUND_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q
                ) {
                    false
                } else {
                    true
                }

            count++
            if (count >= 60) {
                count = 0
                if (permissionStatus) {
                    setCurrent(false)
                } else {

                    actionOnService(Actions.STOP)
                    isStartedButton(false)
                    setCurrent(true)

                    val builder = AlertDialog.Builder(this@MainActivity, AlertDialog.THEME_HOLO_LIGHT)
                    builder.setTitle("Warning!")
                    builder.setMessage(
                        "During tracking we are not able to get Your location." +
                                " that's by tracking is finished."
                    )
                    builder.setPositiveButton("ok", object : DialogInterface.OnClickListener {
                        override fun onClick(p0: DialogInterface?, p1: Int) {
                            p0?.cancel()
//                            val mainIntent = Intent(this@MainActivity, SplashScreen::class.java)
//                            intent.flags =
//                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//                            startActivity(mainIntent)
//                            finish()
                        }
                    })
                    builder.show()
                }
            }
        }
    }

    private val permissionCode = 101
    private val permissionCode2 = 102


    fun dialogPermission(msg: String) {
        val builder = AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT)
        builder.setTitle("Need Permission")
        builder.setMessage("This app needs $msg permission.")
        builder.setPositiveButton("Grant", object : DialogInterface.OnClickListener {
            override fun onClick(p0: DialogInterface?, p1: Int) {
                p0?.cancel()
                val intent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", packageName, null)
                )
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }

        })
        builder.setNegativeButton("Cancel", object : DialogInterface.OnClickListener {
            override fun onClick(p0: DialogInterface?, p1: Int) {
                p0!!.cancel()
            }

        })
        builder.show()
    }
}