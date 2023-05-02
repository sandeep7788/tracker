package com.vline.helper

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import java.io.ByteArrayOutputStream
import java.io.IOException

open class ImageUtils {

    companion object {
    fun getBitmapFromIntent(context: Context, data: Intent): Bitmap? {
        var bitmap: Bitmap? = null
        if (data.data == null) {
            bitmap = data.extras!!["data"] as Bitmap?
        } else {
            try {
                bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, data.data)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return bitmap
    }
    fun createFile(context: Context, data: Bitmap?): String? {
        val selectedImage: Uri = getImageUri(context, data!!)!!
        val filePath = arrayOf(MediaStore.Images.Media.DATA)
        val c: Cursor =
            context.getContentResolver().query(selectedImage, filePath, null, null, null)!!
        c.moveToFirst()
        c.getColumnIndex(filePath[0])
        val columnIndex = c.getColumnIndex(filePath[0])
        val picturePath = c.getString(columnIndex)
        c.close()
        return picturePath
    }
    fun getImageUri(context: Context, inImage: Bitmap): Uri? {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path =
            MediaStore.Images.Media.insertImage(context.contentResolver, inImage, "Pet_Image", null)
        return Uri.parse(path)
    }
}
}