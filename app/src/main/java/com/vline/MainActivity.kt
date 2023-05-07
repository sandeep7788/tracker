package com.vline


import android.Manifest.permission
import android.Manifest.permission.*
import android.app.ActivityManager
import android.app.AlertDialog
import android.content.*
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.MediaStore.Images
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.gson.JsonObject
import com.vline.activity.SplashScreen
import com.vline.databinding.ActivityMainBinding
import com.vline.endless.*
import com.vline.helper.*
import com.vline.helper.MyApplication.Companion.count
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File


class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    private val TAG = "@@MainActivity"
    var textViewTimer: TextView? = null
    var buttonStart: Button? = null
    var buttonStop: Button? = null
    var arr = arrayOf(
        READ_EXTERNAL_STORAGE,
        ACCESS_COARSE_LOCATION,
        ACCESS_FINE_LOCATION,
        ACCESS_BACKGROUND_LOCATION,
        CAMERA
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



//        getImageList()
        captureImage()

        buttonStart!!.setOnClickListener {

//            if (ActivityCompat.checkSelfPermission(
//                    this, READ_EXTERNAL_STORAGE
//                ) != PackageManager.PERMISSION_GRANTED
//            )

            if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
                && ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(READ_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_CODE)
            }

            /*if (!checkPermission()
            ) {
                requestPermission()
//                dialogPermission("storage")


            }*/ else if (ActivityCompat.checkSelfPermission(
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
                    this, CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                dialogPermission("camera")
            } else if (ActivityCompat.checkSelfPermission(
                    this, ACCESS_BACKGROUND_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
                && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
//                ActivityCompat.requestPermissions(
//                    this, arrayOf(
//                        ACCESS_BACKGROUND_LOCATION
//                    ), permissionCode
//                )

                backgroundPermission()
//                dialogPermission("background location")


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

//        binding.log.setOnClickListener {
//            dialog()
//        }


        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        mAdapter = ShakhaListAdapter()
        val mLayoutManager = LinearLayoutManager(applicationContext)
        mLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
        recyclerView.layoutManager = mLayoutManager
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = mAdapter

        ActivityCompat.requestPermissions(
            this@MainActivity, arr, permissionCode
        )

        setLocation(false)
    }

    private fun checkPermission(): kotlin.Boolean {



        var statusPermission=false
        if (VERSION.SDK_INT >= Build.VERSION_CODES.R) {

            if (ActivityCompat.checkSelfPermission(
                    this,
                    READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                if (!(VERSION.SDK_INT >= Build.VERSION_CODES.R)) {
                    statusPermission=true
                }
            }
            Environment.isExternalStorageManager()
            return statusPermission
        } else {
            val result: Int =
                ContextCompat.checkSelfPermission(this@MainActivity, READ_EXTERNAL_STORAGE)
            return result == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermission() {
//        if (SDK_INT >= Build.VERSION_CODES.R || !statusStorage) {
//            try {
//                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
//                intent.addCategory("android.intent.category.DEFAULT")
//                intent.data =
//                    Uri.parse(String.format("package:%s", applicationContext.packageName))
//                startActivityForResult(intent, 2296)
//            } catch (e: java.lang.Exception) {
//                val intent = Intent()
//                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
//                startActivityForResult(intent, 2296)
//            }
//        } else {
//            //below android 11
//            ActivityCompat.requestPermissions(
//                this@MainActivity,
//                arrayOf(WRITE_EXTERNAL_STORAGE,READ_EXTERNAL_STORAGE),
//                PERMISSION_REQUEST_CODE
//            )
//        }
        ActivityCompat.requestPermissions(
            this@MainActivity,
            arrayOf(WRITE_EXTERNAL_STORAGE,READ_EXTERNAL_STORAGE),
            PERMISSION_REQUEST_CODE
        )
    }

    var PERMISSION_REQUEST_BACKGROUND_LOCATION = 102
    var PERMISSION_REQUEST_FINE_LOCATION = 103
    var PERMISSION_REQUEST_CODE = 104

    fun backgroundPermission() {
        if (checkSelfPermission(ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (checkSelfPermission(ACCESS_BACKGROUND_LOCATION)
                != PackageManager.PERMISSION_GRANTED
            ) {
//                if (shouldShowRequestPermissionRationale(ACCESS_BACKGROUND_LOCATION)) {
                if (false) {
                    val builder =
                        AlertDialog.Builder(this)
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
                        val builder =
                            AlertDialog.Builder(this)
                        builder.setTitle("Functionality limited")
                        builder.setMessage("Since background location access has not been granted, this app will not be able to discover beacons in the background.  Please go to Settings -> Applications -> Permissions and grant background location access to this app.")
                        builder.setPositiveButton(android.R.string.ok, DialogInterface.OnClickListener { dialogInterface, i ->
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
                    ),
                    PERMISSION_REQUEST_FINE_LOCATION
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
            binding.imageView4?.visibility = View.VISIBLE
//            binding.log?.visibility = View.GONE
        } else {

            buttonStop?.visibility = View.GONE
            buttonStart?.visibility = View.VISIBLE
            binding.imageView4?.visibility = View.GONE
//            binding.log?.visibility = View.VISIBLE
        }
    }

    private var progressDialog: SweetAlertDialog? = null

    fun captureImage() {
        binding.imageView4.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, 1)
        }
    }

    var file: File? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && data != null && data!!.getExtras() != null && data!!.getExtras()!!
                .get("data") != null
        ) {
            val photo: Bitmap = data!!.getExtras()!!.get("data") as Bitmap
//            binding.imageView4.setImageBitmap(photo)
            val tempUri: Uri = getImageUri(applicationContext, photo)!!
            file = File(getRealPathFromURI(tempUri))
//            System.out.println(finalFile.absolutePath)
            sendImage()
        }
    }

    fun getImageUri(inContext: Context, inImage: Bitmap): Uri? {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = Images.Media.insertImage(
            inContext.contentResolver, inImage, "Title", null
        )
        return Uri.parse(path)
    }

    fun getRealPathFromURI(uri: Uri?): String? {
        var path = ""
        if (contentResolver != null) {
            val cursor: Cursor? = contentResolver.query(uri!!, null, null, null, null)
            if (cursor != null) {
                cursor.moveToFirst()
                val idx: Int = cursor.getColumnIndex(Images.ImageColumns.DATA)
                path = cursor.getString(idx)
                cursor.close()
            }
        }
        return path
    }

    @NonNull
    private fun createPartFromString(value: String): RequestBody? {
        return RequestBody.create(
            MultipartBody.FORM, value
        )
    }

    var trackingId = 0
    fun sendImage() {

//        val parts = MultipartBody.Part.createFormData(
//            "photo", file!!.name, RequestBody.create(
//                MediaType.parse("image/*"), file
//            )
//        )
//        val file = File(FileUtils.getPath(this@MainActivity, imageUri))
        val requestFile: RequestBody =
            RequestBody.create(MediaType.parse("multipart/form-data"), file)
        val parts = MultipartBody.Part.createFormData("selfie", file!!.name, requestFile)
        val userId = createPartFromString(
            MyApplication.ReadIntPreferences(ApiContants.PREF_USER_ID).toString()
        )
        val tracking_id = createPartFromString(
            trackingId.toString()
        )


        val apiInterface: ApiInterface? =
            RetrofitManager().instanceNew(this@MainActivity)?.create(ApiInterface::class.java)

        apiInterface!!.image(
            userId, tracking_id, parts
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
                        val msg = JSONObject(response.body().toString()).getString("message")

//                        Utility.showSnackBar(this@MainActivity, msg)
                        getImageList()
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


    }


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


    var imageList = ArrayList<String>()
    private var mAdapter: ShakhaListAdapter? = null


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
        alertDialog.setPositiveButton("close",
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

    fun setLocation(isCallApi:Boolean) {

        gpsTracker = GPSTracker(this)
        if (isLocationPermissionGranted() && gpsTracker.getIsGPSTrackingEnabled()) {

            val country = gpsTracker.getCountryName(this)
            val city = gpsTracker.getLocality(this)
            val postalCode = gpsTracker.getPostalCode(this)
            val addressLine = gpsTracker.getAddressLine(this)

            binding.txtStart.text = "$addressLine $city $postalCode $country"
            latitude = gpsTracker.latitude
            longitude = gpsTracker.longitude
        } else {
//            dialogPermission()
        }
        locStatus = "start"

        if (isCallApi) {
            sendLocation()
        }
    }

    var latitude = 0.0
    var longitude = 0.0
    var currentAddress = ""
    fun setCurrent(isStop: Boolean) {

        if (isStop) {
            locStatus = "stop"
        } else {
            locStatus = "runing"
        }

        gpsTracker = GPSTracker(this)
        if (isLocationPermissionGranted() && gpsTracker.getIsGPSTrackingEnabled()) {

            val country = gpsTracker.getCountryName(this)
            val city = gpsTracker.getLocality(this)
            val postalCode = gpsTracker.getPostalCode(this)
            val addressLine = gpsTracker.getAddressLine(this)
            latitude = gpsTracker.latitude
            longitude = gpsTracker.longitude

            if (locStatus.equals("runing", true) || locStatus.equals("stop", true)) {
                currentAddress = "$addressLine $city $postalCode $country"
            }

//            list.add(" - " + Utility.getCurrentTime() + " - $addressLine $city $postalCode $country")
            Log.e(TAG, "setCurrent: " + list.size)
        } else {
//            dialogPermission()
        }




        sendLocation()
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
                ), 100
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

            count++
            if (count >= 60) {
                count = 0
                setCurrent(false)
            }
        }
    }

    private lateinit var currentLocation: Location
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val permissionCode = 101

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == permissionCode) {
//            if (ActivityCompat.checkSelfPermission(
//                    this,
//                    ACCESS_BACKGROUND_LOCATION
//                ) != PackageManager.PERMISSION_GRANTED
//            ) {
////                dialogPermission("background location")
//                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
//            } else if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
//            } else {
//
//                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
//            }
        }

        if (grantResults != null && requestCode == PERMISSION_REQUEST_CODE) {

            if (ActivityCompat.checkSelfPermission(
                    this,
                    READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
//                if (!(VERSION.SDK_INT >= Build.VERSION_CODES.R)) {
//                    statusStorage = false
//
//                    dialogPermission("storage")
//                }
                dialogPermission("storage")
            } else {
                Toast.makeText(this, "Storage permission is granted!", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }
    var statusStorage = true

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

    fun getImageList() {

        imageList.clear()
        val apiInterface: ApiInterface? =
            RetrofitManager().instanceNew(this@MainActivity)?.create(ApiInterface::class.java)

        val userId = MyApplication.ReadIntPreferences(ApiContants.PREF_USER_ID).toString()
        apiInterface!!.tracking_selfielist(
            userId.toString()
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


                        val image_path =
                            JSONObject(response.body().toString()).getString("image_path")

                        if (JSONObject(response.body().toString()).has("data")) {

                            var imgList =
                                JSONObject(response.body().toString()).getJSONArray("data")

                            val size: Int = imgList.length()
                            for (i in 0 until size) {
                                val json: JSONObject = imgList.getJSONObject(i)
                                imageList.add(image_path + json.optString("selfie"))
                            }
                        }
                        imageList.reverse()


//                        mAdapter!!.updateList(imageList)

                        mAdapter!!.setData(imageList)
                        mAdapter?.notifyDataSetChanged()

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


    }
}