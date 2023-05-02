package com.vline


import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.ActivityManager
import android.app.AlertDialog
import android.content.*
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.tasks.OnSuccessListener
import com.vline.activity.SplashScreen
import com.vline.databinding.ActivityMainBinding
import com.vline.endless.*
import java.util.*


class MainActivity : AppCompatActivity() , OnMapReadyCallback {

    lateinit var binding: ActivityMainBinding

    // Use seconds, running and wasRunning respectively
    // to record the number of seconds passed,
    // whether the stopwatch is running and
    // whether the stopwatch was running
    // before the activity was paused.
    private var seconds = 0

    // Is the stopwatch running?
    private var running = false
    private var wasRunning = false


    private val TAG = "MainActivity"
    var textViewTimer: TextView? = null
    var buttonStart: Button? = null
    var buttonStop: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        textViewTimer = binding.timeView
        buttonStart = binding.startButton
        buttonStop = binding.imageView4



        buttonStop!!.setOnClickListener {
            actionOnService(Actions.STOP)
            textViewTimer!!.setText("00:00:00")
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(
            mMessageReceiver, IntentFilter("GPSLocationUpdates")
        )

        gpsTracker = GPSTracker(this)




        binding.logout.setOnClickListener { logout() }

        setLocation()
        buttonStart!!.setOnClickListener {

            if (!isMyServiceRunning(EndlessService::class.java)) {
                setLocation()
                actionOnService(Actions.START)

//                val restartServiceIntent = Intent(applicationContext, GPSTracker::class.java).also {
//                    it.setPackage(packageName)
//                }
//                startService(restartServiceIntent)
            }
        }
    }
    lateinit var gpsTracker:GPSTracker

    fun logout() {
        AlertDialog.Builder(this)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setTitle("Closing Activity")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes",
                DialogInterface.OnClickListener { dialog, which -> startActivity(Intent(this, SplashScreen::class.java))
                    finish() })
            .setNegativeButton("No", null)
            .show()
    }

    fun setLocation() {

//        val gpsTracker = GPSTracker(this)
        if (isLocationPermissionGranted() && gpsTracker.getIsGPSTrackingEnabled()) {

            val country = gpsTracker.getCountryName(this)
            val city = gpsTracker.getLocality(this)
            val postalCode = gpsTracker.getPostalCode(this)
            val addressLine = gpsTracker.getAddressLine(this)

            binding.txtStart.text = "$addressLine $city $postalCode $country"
        }
    }
    fun setCurrent() {


        if (isLocationPermissionGranted() && gpsTracker.getIsGPSTrackingEnabled()) {

            val country = gpsTracker.getCountryName(this)
            val city = gpsTracker.getLocality(this)
            val postalCode = gpsTracker.getPostalCode(this)
            val addressLine = gpsTracker.getAddressLine(this)

            binding.txtCurrent.text = "$addressLine $city $postalCode $country"
        }
    }

    private fun isLocationPermissionGranted(): Boolean {
        return if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                100
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


    var count = 0
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

            var h=hours.toString()
            var m=mins.toString()
            var s=secs.toString()

            if (hours<=9)
                h = "0"+hours.toString()
            if (mins<=9)
                m = "0"+mins.toString()
            if (secs<=9)
                s = "0"+secs.toString()


            val requiredFormat = "$h:$m:$s"
            Log.e("@@", "updateGUI: " + requiredFormat)

            binding.timeView.setText(requiredFormat)

            count++
            if (count >= 10) {
                count=0
                setCurrent()
            }
        }
    }

    private lateinit var currentLocation: Location
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val permissionCode = 101

    private fun fetchLocation() {
        if (ActivityCompat.checkSelfPermission(
                this, ACCESS_FINE_LOCATION
            ) !=
            PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, ACCESS_COARSE_LOCATION
            ) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(ACCESS_FINE_LOCATION), permissionCode
            )
            return
        }
        val task = fusedLocationProviderClient.lastLocation
        task.addOnSuccessListener(OnSuccessListener {
            if (it != null) {
                currentLocation = it
                Toast.makeText(
                    applicationContext, currentLocation.latitude.toString() + "" +
                            currentLocation.longitude, Toast.LENGTH_SHORT
                ).show()


                val geocoder: Geocoder
                val addresses: List<Address>?
                geocoder = Geocoder(this, Locale.getDefault())

                addresses = geocoder.getFromLocation(
                    currentLocation.latitude,
                    currentLocation.longitude,
                    1
                ) // Here 1 represent max location result to returned, by documents it recommended 1 to 5


                val address: String =
                    addresses!![0].getAddressLine(0) // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()

                val city: String = addresses!![0].locality
                val state: String = addresses!![0].adminArea
                val country: String = addresses!![0].countryName
                val postalCode: String = addresses!![0].postalCode
                val knownName: String = addresses!![0].featureName

                binding.txtCurrent.text =
                    knownName + " " + city + " " + state + " " + country + " " + postalCode + " "
//                val supportMapFragment = (supportFragmentManager.findFragmentById(R.id.myMap) as
//                        SupportMapFragment?)!!
//                supportMapFragment.getMapAsync(this@MainActivity)
            }

        })

    }

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
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
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