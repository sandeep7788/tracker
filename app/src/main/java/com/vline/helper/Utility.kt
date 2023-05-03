package com.vline.helper

import android.Manifest
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Point
import android.net.ConnectivityManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import android.text.Html
import android.text.format.Formatter
import android.util.Log
import android.view.*
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import cn.pedant.SweetAlert.SweetAlertDialog
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.vline.R

import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class Utility {
    companion object {
        var CAMERA_PERMISSION_CODE=101
        var STORAGE_PERMISSION_CODE=102
        private val REQUEST_IMAGE_CAPTURE = 1
        private val REQUEST_PICK_IMAGE = 2

        fun changeStatusBarColor(activity: Activity) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val window = activity.window
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.statusBarColor = Color.BLACK
            }
        }

        fun convertInMils12(strDate: String): Boolean {

            var isCorrectTime = false;
            val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm");
            val date = sdf.parse(strDate);
            val millis = date.getTime();

            val now = Calendar.getInstance();
            now.add(Calendar.HOUR, 12);
            if (millis > now.timeInMillis) {
                isCorrectTime = true
            }
            return isCorrectTime
        }
        open fun showKeyboard(editText: EditText,context: Context) {
            editText.post {
                editText.requestFocus()
                val imm = editText.context
                    .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
            }
        }

//Old Code

        ///////////////////////////////////////////////////////////////
        fun convertInMilsCancel2(strDate: String, minute: String): Boolean {

            var isCorrectTime = false;
            val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm");
            val date = sdf.parse(strDate);
            val millis = date.getTime();

            val now15 = Calendar.getInstance();
            now15.time = date;
            now15.add(Calendar.MINUTE, -15);

            val current = Calendar.getInstance();

            val now30 = Calendar.getInstance();
            now30.time = date;

            //    if (current.timeInMillis <= now30.timeInMillis && current.timeInMillis >= now15.timeInMillis) {
            /*if (millis <= now30.timeInMillis && millis >= now15.timeInMillis) {
                isCorrectTime = true
            }*/
            if (current.after(now15)&&current.before(now30)) {
                isCorrectTime = true
            }
            Log.e("time valid", isCorrectTime.toString())
            return isCorrectTime
        }





        fun checkCurrentDate(createDate: String, todaysDate: String): Boolean {

            val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm")
            val strDate = sdf.parse(createDate)
            val strDateToday = sdf.parse(todaysDate)

            return (strDate.time == strDateToday.time)
        }


        fun getCurrentStamp(): String {
            val c = Calendar.getInstance()
            val df = SimpleDateFormat("dd-MM-yyyy HH:mm:ss")

            Log.e("Current time => ", "" + df.format(c.time))
            return df.format(c.time)
        }

        fun OpenCamera(activity: Activity) {
            val takePicture = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            activity.startActivityForResult(takePicture, 0) //zero can be replaced with any action code (called requestCode)
        }
        fun selectImage(activity: Activity) {
            val options = arrayOf<CharSequence>("Use Camera", "Choose from Gallery", "Cancel")
            val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
            builder.setTitle("Add Image!")
            builder.setItems(options, DialogInterface.OnClickListener { dialog, item ->
                if (options[item] == "Use Camera") {
                    checkPermissionCamera(activity, CAMERA_PERMISSION_CODE)
                } else if (options[item] == "Choose from Gallery") {
//                checkPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE,STORAGE_PERMISSION_CODE)
                    checkPermission(activity,Manifest.permission.READ_EXTERNAL_STORAGE,
                        STORAGE_PERMISSION_CODE
                    )
                } else if (options[item] == "Cancel") {
                    dialog.dismiss()
                }
            })
            builder.show()
        }
        fun checkPermission(activity: Activity,permission: String, requestCode: Int) {

            // Checking if permission is not granted
            if (ContextCompat.checkSelfPermission(
                    activity,
                    permission)
                == PackageManager.PERMISSION_DENIED
            ) {
                ActivityCompat
                    .requestPermissions(
                        activity, arrayOf(permission),
                        requestCode)
            } else {
                when (requestCode) {
                    CAMERA_PERMISSION_CODE ->  openCamera(activity)
                    STORAGE_PERMISSION_CODE ->  openGallery(activity)
                }
            }
        }
        fun openCamera(activity: Activity) {
            Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { intent ->
                intent.resolveActivity(activity.packageManager)?.also {
                    activity.startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
                }
            }
        }
        fun openGallery(activity: Activity) {
            Intent(Intent.ACTION_GET_CONTENT).also { intent ->
                intent.type = "image/*"
                intent.resolveActivity(activity.packageManager)?.also {
                    activity!!.startActivityForResult(intent, REQUEST_PICK_IMAGE)
                }
            }
        }

        fun checkPermissionCamera(activity: Activity,requestCode: Int) {

            if(ContextCompat.checkSelfPermission(activity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED ||
                ContextCompat.checkSelfPermission(activity,Manifest.permission.CAMERA)==PackageManager.PERMISSION_DENIED){

                    ActivityCompat
                    .requestPermissions(
                        activity, arrayOf(android.Manifest.permission.CAMERA,android.Manifest.permission.READ_EXTERNAL_STORAGE,android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        requestCode)
            } else {
                when (requestCode) {
                    CAMERA_PERMISSION_CODE ->  openCamera(activity)
                    STORAGE_PERMISSION_CODE ->  openGallery(activity)
                }
            }
        }


/*
                            lastAction = MotionEvent.ACTION_MOVE
                            val layoutParams: ViewGroup.MarginLayoutParams = view.layoutParams as ViewGroup.MarginLayoutParams

                            val viewWidth = view.width
                            val viewHeight = view.height

                            val viewParent = view.parent as View
                            val parentWidth = viewParent.width
                            val parentHeight = viewParent.height

                            var newX: Float = event.getRawX() + dX
                            newX = Math.max(layoutParams.leftMargin.toFloat(), newX) // Don't allow the FAB past the left hand side of the parent

                            newX = Math.min(parentWidth - viewWidth - layoutParams.rightMargin.toFloat(), newX) // Don't allow the FAB past the right hand side of the parent

                            var newY: Float = event.getRawY() + dY
                            newY = Math.max(layoutParams.topMargin.toFloat(), newY.toFloat()) // Don't allow the FAB past the top of the parent

                            newY = Math.min(parentHeight - viewHeight - layoutParams.bottomMargin.toFloat(), newY) // Don't allow the FAB past the bottom of the parent
                            view.animate()
                                    .x(newX)
                                    .y(newY)
                                    .setDuration(0)
                                    .start();
                            return true;

                            Log.e("lastCor", (event.rawY + dY).toString() + " " + (event.rawX + dX))
                        }
                        MotionEvent.ACTION_UP -> if (lastAction === android.view.MotionEvent.ACTION_DOWN)
                            openWhatsApp(activity)

                        else -> return false
                    }
                    return true
                }
            })

        }*/

        fun setClickMoveablefab(llFab: ImageView, activity: Activity) {
            var lastAction: Int = 0
            var dX: Float = 0.0f
            var dY: Float = 0.0f
            llFab.setOnTouchListener(object : View.OnTouchListener {
                override fun onTouch(view: View, event: MotionEvent): Boolean {
                    Log.d("mask", event.actionMasked.toString())
                    when (event.actionMasked) {
                        MotionEvent.ACTION_DOWN -> {
                            dX = view.x - event.rawX
                            dY = view.y - event.rawY
                            lastAction = MotionEvent.ACTION_DOWN
                        }
                        /*  MotionEvent.ACTION_MOVE -> {
                              view.y = event.rawY + dY
                              view.x = event.rawX + dX
                              lastAction = MotionEvent.ACTION_MOVE
                              Log.e("lastCor", (event.rawY + dY).toString() + " " + (event.rawX + dX))
                          }*/
                        MotionEvent.ACTION_MOVE -> {
                            /*  if ((event.rawY+dY)<1477.0093&&(event.rawX+dX)<832.0){
                              view.y = event.rawY + dY
                          view.x = event.rawX + dX
                          }*/
                            lastAction = MotionEvent.ACTION_MOVE
                            val layoutParams: ViewGroup.MarginLayoutParams = view.layoutParams as ViewGroup.MarginLayoutParams

                            val viewWidth = view.width
                            val viewHeight = view.height

                            val viewParent = view.parent as View
                            val parentWidth = viewParent.width
                            val parentHeight = viewParent.height

                            var newX: Float = event.getRawX() + dX
                            newX = Math.max(layoutParams.leftMargin.toFloat(), newX) // Don't allow the FAB past the left hand side of the parent

                            newX = Math.min(parentWidth - viewWidth - layoutParams.rightMargin.toFloat(), newX) // Don't allow the FAB past the right hand side of the parent

                            var newY: Float = event.getRawY() + dY
                            newY = Math.max(layoutParams.topMargin.toFloat(), newY.toFloat()) // Don't allow the FAB past the top of the parent

                            newY = Math.min(parentHeight - viewHeight - layoutParams.bottomMargin.toFloat(), newY) // Don't allow the FAB past the bottom of the parent
                            view.animate()
                                .x(newX)
                                .y(newY)
                                .setDuration(0)
                                .start();
                            return true;

                            Log.e("lastCor", (event.rawY + dY).toString() + " " + (event.rawX + dX))
                        }
                        MotionEvent.ACTION_UP -> {
                            lastAction = MotionEvent.ACTION_UP
                            if (lastAction == MotionEvent.ACTION_UP) {
                                openWhatsApp(activity)
                            }
                        }
                        else -> return false
                    }
                    return true
                }
            })

        }

        fun openWhatsApp(context: Context) {
            try {
                val whatsAppRoot = "http://api.whatsapp.com/"
                val number = "send?phone=" + ApiContants.PREF_WhatsAppNumber //here the mobile number with its international prefix
                val text = "&text=Hello there,\n"

                val uri = whatsAppRoot + number + text
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(uri)
                context.startActivity(intent)
            } catch (e: java.lang.Exception) {
                Toast.makeText(context,
                    "WhatsApp cannot be opened", Toast.LENGTH_SHORT).show()
            }
        }

        fun openWhatsApp(context: Context,number: String) {
            try {
                val whatsAppRoot = "http://api.whatsapp.com/"
                val number = "send?phone=" + "+91"+number.trim() //here the mobile number with its international prefix
                val text = "&text=भाई साहब राम-राम \uD83D\uDEA9 \uD83D\uDEA9,\n"

                val uri = whatsAppRoot + number + text
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(uri)
                context.startActivity(intent)


            } catch (e: java.lang.Exception) {
                Toast.makeText(context,
                    "WhatsApp cannot be opened", Toast.LENGTH_SHORT).show()
            }
        }

        fun setHtmlText(tvText: TextView, content: String) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                tvText.text = Html.fromHtml(content, Html.FROM_HTML_MODE_COMPACT)
            } else {
                tvText.text = Html.fromHtml(content)
            }
        }

        fun changeDateFormateForApi(date: String, currentFormate: String, changeFormate: String): String {
            var date = date
            var spf = SimpleDateFormat(currentFormate)
            var newDate: Date? = null
            try {
                newDate = spf.parse(date)
                spf = SimpleDateFormat(changeFormate)
                date = spf.format(newDate)
            } catch (e: Exception) {
                Log.e("changeDateteForApi: ", e.localizedMessage)
            }
            return "" + date
        }

        fun setAnimation(itemView: View, i: Int) {
            var i = i
            val on_attach = true
            val DURATION = 500
            i = i
            if (!on_attach) {
                i = -1
            }
            val isNotFirstItem = i == -1
            i++
            itemView.alpha = 0f
            val animatorSet = AnimatorSet()
            val animator = ObjectAnimator.ofFloat(itemView, "alpha", 0f, 0.5f, 1.0f)
            ObjectAnimator.ofFloat(itemView, "alpha", 0f).start()

            val dur: Long
            if (isNotFirstItem) {
                dur = (DURATION / 2).toLong()
            } else {
                dur = (i * DURATION / 3).toLong()
            }
            animator.startDelay = dur
            animator.duration = 500
            animatorSet.play(animator)
            animator.start()
        }

        fun hideKeyboard(activity: Activity) {
            val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            //Find the currently focused view, so we can grab the correct window token from it.
            var view = activity.currentFocus
            //If no view currently has focus, create a new one, just so we can grab a window token from it
            if (view == null) {
                view = View(activity)
            }
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }

        fun shakeAnimation(context: Context, view: View, errorMsg: String) {
            hideKeyboard(context as Activity)
            if (errorMsg != "") {
                showSnackBar(context, errorMsg)
            }
            val shake = AnimationUtils.loadAnimation(context, R.anim.shakeanim)
            view.startAnimation(shake)
        }

        fun showSnackBar(context: Activity, msg: String) {
            hideKeyboard(context)
            Snackbar.make(context.findViewById(android.R.id.content), msg, Snackbar.LENGTH_SHORT).show()
        }


        fun fullScreenDialog(layoutId: Int, activity: Activity): Dialog {
            val dialog = Dialog(activity)
            dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(layoutId)
            dialog.setCancelable(false)
            val window = dialog.window
            window!!.setGravity(Gravity.CENTER)
            val display = activity.windowManager.defaultDisplay
            val size = Point()
            display.getSize(size)
            val width = (size.x * 0.94).toInt()
            val height = (size.y * 0.94).toInt()
            dialog.show()
            dialog.window!!.setLayout(width, height)

            return dialog
        }


        fun getParmMap(): MutableMap<String, String> {


            return HashMap<String, String>()

        }




        fun makeCall(context: Context, mob: String) {
            try {
                val intent = Intent(Intent.ACTION_DIAL)

                intent.data = Uri.parse("tel:$mob")
                context.startActivity(intent)
            } catch (e: java.lang.Exception) {
                Toast.makeText(context,
                    "Unable to call at this time", Toast.LENGTH_SHORT).show()
            }
        }

        fun explainSetting(activity: Activity) {
            val dialogS = AlertDialog.Builder(activity)
            dialogS.setMessage("Go to setting and give some mandatory permissions to continue. Do you want to go to app settings?")
                .setPositiveButton("Yes") { paramDialogInterface, paramInt ->
                    val intent = Intent()
                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    val uri = Uri.fromParts("package", activity.packageName, null)
                    intent.data = uri
                    activity.startActivity(intent)
                    activity.finish()
                }
                .setNegativeButton("Cancel") { paramDialogInterface, paramInt ->
//                    activity.finish()
                }
            dialogS.show()
        }
        
        fun getIPaddress(activity: Activity): String {
            val wifiManager = activity.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val ipAddress = Formatter.formatIpAddress(wifiManager.connectionInfo.ipAddress)
            Log.e("id", ipAddress)
            return ipAddress
        }

        fun setImage(context: Context, path: String, ivImage: ImageView) {
//            Picasso.with(context).load(path).placeholder(R.drawable.add_icon).into(ivImage)
            Glide.with(context)
                .load(path)
                .into(ivImage)
        }

        /*fun shareAppIntent(activity: Activity) {
            val sendIntent = Intent()
            sendIntent.action = Intent.ACTION_SEND
            sendIntent.putExtra(Intent.EXTRA_TEXT, "Hey check out my app at: https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID)
            sendIntent.type = "text/plain"
            activity.startActivity(sendIntent)
        }*/

        fun getOtpCode(): String {
            val rnd = Random();
            val number = rnd.nextInt(999999);
            return String.format("%06d", number);
        }
        fun setDate(txtView:TextView,context: Context) {
            var date: Calendar = Calendar.getInstance()
            var thisAYear = date.get(Calendar.YEAR).toInt()
            var thisAMonth = date.get(Calendar.MONTH).toInt()
            var thisADay = date.get(Calendar.DAY_OF_MONTH).toInt()

            val dpd = DatePickerDialog(
                context,
                R.style.DialogTheme,
                DatePickerDialog.OnDateSetListener { view2, thisYear, thisMonth, thisDay ->
                    thisAMonth = thisMonth + 1
                    thisADay = thisDay
                    thisAYear = thisYear

                    txtView.setText(thisDay.toString()+"/" + thisAMonth + "/" + thisYear)
                    val newDate: Calendar = Calendar.getInstance()
                    newDate.set(thisYear, thisMonth, thisDay)
//                mh.entryDate = newDate.timeInMillis // setting new date
//                    Log.e("@@date1", newDate.timeInMillis.toString() + " ")
                },
                thisAYear,
                thisAMonth,
                thisADay
            )
            dpd.datePicker.spinnersShown = true
            dpd.datePicker.calendarViewShown = false
            dpd.show()
        }

/*
        fun pickImage(activity: Activity?) {
            ImagePicker.with(activity) //  Initialize ImagePicker with activity or fragment context
                    .setToolbarColor("#212121") //  Toolbar color
                    .setStatusBarColor("#000000") //  StatusBar color (works with SDK >= 21  )
                    .setToolbarTextColor("#FFFFFF") //  Toolbar text color (Title and Done button)
                    .setToolbarIconColor("#FFFFFF") //  Toolbar icon color (Back and Camera button)
                    .setProgressBarColor("#4CAF50") //  ProgressBar color
                    .setBackgroundColor("#212121") //  Background color
                    .setCameraOnly(false) //  Camera mode
                    .setMultipleMode(false) //  Select multiple images or single image
                    .setFolderMode(false) //  Folder mode
                    .setShowCamera(true) //  Show camera button
                    .setFolderTitle("Albums") //  Folder title (works with FolderMode = true)
                    .setImageTitle("Galleries") //  Image title (works with FolderMode = false)
                    .setDoneTitle("Done") //  Done button title
                    .setLimitMessage("") // Selection limit message
                    .setMaxSize(1) //  Max images can be selected
                    .setSavePath("ImagePicker") //  Selected images
                    .setAlwaysShowDoneButton(true) //  Set always show done button in multiple mode
                    .setRequestCode(100) //  Set request code, default Config.RC_PICK_IMAGES
                    .setKeepScreenOn(true) //  Keep screen on when selecting images
                    .start()
        }
*/

        /*    fun compressImage(filepath: String): File {
                var file: File? = null
                file = try {
                    com.id.zelory.compressor.Compressor(AntimatterApp.appContext).compressToFile(File(filepath))
                } catch (e: java.lang.Exception) {
                    File(filepath)
                }
                return file
            }*/

        /*fun latlangInLocation(target: LatLng): Location {
            val temp = Location(LocationManager.GPS_PROVIDER)
            temp.latitude = target.latitude
            temp.longitude = target.longitude

            return temp
        }*/
        fun getFileName(bitmap: Bitmap,context: Context):String {
            val imgPath: String = ImageUtils.createFile(context, bitmap)!!
            val imageFile:String = File(imgPath).toString()
            val filename: String = imageFile.substring(imageFile.lastIndexOf("/") + 1)
            Log.d("filename", "onActivityResult: " + filename)
            return filename
        }
        fun showDialog(context: Context,type: Int,title: String){

            val pDialog = SweetAlertDialog(context, type)
            pDialog.setTitleText(title)
            pDialog.confirmText="OK"
            pDialog.progressHelper.barColor = Color.parseColor("#02639C")
            pDialog.setCancelable(false)
            pDialog.setConfirmClickListener {
                pDialog.dismiss()
            }
            pDialog.show()
//            pDialog.getButton(SweetAlertDialog.BUTTON_CONFIRM).setBackgroundColor(Color.parseColor("#FFE8560D"))

        }
        @SuppressLint("MissingPermission")
        fun isNetworkAvailable(context: Context): Boolean {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            return connectivityManager.activeNetworkInfo != null && connectivityManager.activeNetworkInfo!!
                .isConnected
        }
        fun getCurrentTimeStamp(): String? {
            return try {
                val dateFormat =
                    SimpleDateFormat("yyyy_MM_dd_HH_mm_ss")
                dateFormat.format(Date())
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                null
            }
        }


        fun getCurrentTime(): String? {
            return try {
                val dateFormat =
                    SimpleDateFormat("dd: HH:mm:ss")
                dateFormat.format(Date())
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                null
            }
        }

        fun toast(context: Context, msg: String) {
            Toast.makeText(context,msg,Toast.LENGTH_SHORT).show()
        }
    }

}