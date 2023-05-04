package com.vline


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
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Images
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
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


class MainActivity : AppCompatActivity(), OnMapReadyCallback {

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


        buttonStop!!.setOnClickListener {
            actionOnService(Actions.STOP)
            textViewTimer!!.setText("00:00:00")
            isStartedButton(false)


            setCurrent(true)
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(
            mMessageReceiver, IntentFilter("GPSLocationUpdates")
        )





        binding.logout.setOnClickListener { logout() }

        setLocation()
        getImageList()

        buttonStart!!.setOnClickListener {

            if (ActivityCompat.checkSelfPermission(
                    this, ACCESS_BACKGROUND_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(
                        ACCESS_BACKGROUND_LOCATION
                    ), 100
                )

                dialogPermission("background location")


            } else if (ActivityCompat.checkSelfPermission(
                    this, READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                dialogPermission("storage")
            } else if (ActivityCompat.checkSelfPermission(
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
            } else {
                if (!isMyServiceRunning(EndlessService::class.java)) {
                    setLocation()
                    actionOnService(Actions.START)
                    locStatus = "start"
                    sendLocation()
                }
                isStartedButton(true)
            }


        }

        binding.log.setOnClickListener {
            dialog()
        }
        captureImage()

        ActivityCompat.requestPermissions(
            this@MainActivity, arr, permissionCode
        )


        binding.name.setText(MyApplication.ReadStringPreferences(ApiContants.PREF_USER_NAME))
        binding.number.setText(MyApplication.ReadStringPreferences(ApiContants.PREF_WhatsAppNumber))


        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        mAdapter = ShakhaListAdapter()
        val mLayoutManager = LinearLayoutManager(applicationContext)
        mLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
        recyclerView.layoutManager = mLayoutManager
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = mAdapter
    }

    fun isStartedButton(it: Boolean) {
        if (it) {
            buttonStop?.visibility = View.VISIBLE
            buttonStart?.visibility = View.GONE
            binding.imageView4?.visibility = View.VISIBLE
            binding.log?.visibility = View.GONE
        } else {

            buttonStop?.visibility = View.GONE
            buttonStart?.visibility = View.VISIBLE
            binding.imageView4?.visibility = View.GONE
            binding.log?.visibility = View.VISIBLE
        }
    }

    lateinit var imageUri: Uri
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
        if (requestCode == 1 && data !=null && data!!.getExtras() !=null && data!!.getExtras()!!.get("data") != null) {
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
            userId, tracking_id,parts
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

                        Utility.showSnackBar(this@MainActivity, msg)
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
        address = if (locStatus == "start") {
            binding.txtStart.text.toString()
        } else {
            binding.txtCurrent.text.toString()
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
                            trackingId = JSONObject(response.body().toString()).getJSONObject("data").getInt("id")
                        }

                        Log.d(TAG, "sendLocation onResponse: " + msg + " "+trackingId)
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


    }


    var imageList = ArrayList<String>()
    private var mAdapter: ShakhaListAdapter? = null
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


                        val image_path = JSONObject(response.body().toString()).getString("image_path")

                        if (JSONObject(response.body().toString()).has("data")) {

                            var imgList = JSONObject(response.body().toString()).getJSONArray("data")

                            val size: Int = imgList.length()
                            for (i in 0 until size) {
                                val json: JSONObject = imgList.getJSONObject(i)
                                imageList.add(image_path+json.optString("selfie"))
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

    fun setLocation() {

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
//        sendLocation()
    }

    var latitude = 0.0
    var longitude = 0.0
    fun setCurrent(isStop:Boolean) {

        gpsTracker = GPSTracker(this)
        if (isLocationPermissionGranted() && gpsTracker.getIsGPSTrackingEnabled()) {

            val country = gpsTracker.getCountryName(this)
            val city = gpsTracker.getLocality(this)
            val postalCode = gpsTracker.getPostalCode(this)
            val addressLine = gpsTracker.getAddressLine(this)
            latitude = gpsTracker.latitude
            longitude = gpsTracker.longitude
            binding.txtCurrent.text = "$addressLine $city $postalCode $country"

            list.add(" - " + Utility.getCurrentTime() + " - $addressLine $city $postalCode $country")
            Log.e(TAG, "setCurrent: " + list.size)
        } else {
//            dialogPermission()
        }

        if (isStop){
            locStatus = "stop"
        } else {
            locStatus = "runing"
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


    //    override fun onMapReady(googleMap: GoogleMap?) {
//        val latLng = LatLng(currentLocation.latitude, currentLocation.longitude)
////        val markerOptions = MarkerOptions().position(latLng).title("I am here!")
////        googleMap?.animateCamera(CameraUpdateFactory.newLatLng(latLng))
////        googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 5f))
////        googleMap?.addMarker(markerOptions)
//
////        fetchLocation()
//    }
//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>,
//                                            grantResults: IntArray) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        when (requestCode) {
//            permissionCode −> {
//            if (grantResults.isNotEmpty() && grantResults[0] ==
//                PackageManager.PERMISSION_GRANTED) {
//                fetchLocation()
//            }
//            }
//        }
//    }
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == permissionCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
            } else {

                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun dialogPermission(msg:String) {
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

    override fun onMapReady(p0: GoogleMap?) {
        Log.e(TAG, ">>>>>>>>>>>>>>>>>>>>>>>>>>onMapReady: ")
//        setLocation()
    }

//    private lateinit var locationManager: LocationManager
//    private lateinit var tvGpsLocation: TextView
//    private val locationPermissionCode = 2
//
//    private fun getLocation() {
//        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
//        if ((ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
//            ActivityCompat.requestPermissions(this, arrayOf(ACCESS_FINE_LOCATION), locationPermissionCode)
//        }
//        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5f, this)
//    }
//
//
//    override fun onLocationChanged(location: Location) {
//        tvGpsLocation = findViewById(R.id.txtCurrent)
//        tvGpsLocation.text = "Latitude: " + location?.latitude + " , Longitude: " + location.longitude
//        Log.e(TAG, "onLocationChanged: "+ location?.latitude)
//        Log.e(TAG, "onLocationChanged: "+ location.longitude)
//    }

}