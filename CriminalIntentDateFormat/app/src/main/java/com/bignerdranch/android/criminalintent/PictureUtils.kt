package com.bignerdranch.android.criminalintent

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Point
import android.media.ExifInterface
import android.os.Build
import kotlin.math.roundToInt


fun getScaledBitmap(path: String, destWidth: Int, destHeight: Int) : Bitmap {
    //Read in dimensions of the image on disk
    var options = BitmapFactory.Options()
    options.inJustDecodeBounds = true
    BitmapFactory.decodeFile(path, options)

    // Make sure it is rotated the correct way!
    val ei = ExifInterface(path)
    val orientation = ei?.getAttributeInt(
        ExifInterface.TAG_ORIENTATION,
        ExifInterface.ORIENTATION_UNDEFINED
    )

    val srcWidth = if (orientation == ExifInterface.ORIENTATION_NORMAL
        || orientation == ExifInterface.ORIENTATION_ROTATE_180) {
        options.outWidth.toFloat()
    } else {
        options.outHeight.toFloat()
    }
    val srcHeight = if (orientation == ExifInterface.ORIENTATION_NORMAL
        || orientation == ExifInterface.ORIENTATION_ROTATE_180) {
        options.outHeight.toFloat()
    } else {
        options.outWidth.toFloat()
    }



    // Figure out how much to scale down by
    var inSampleSize = 1
    if (srcHeight > destHeight || srcWidth > destWidth) {
        val heightScale = srcHeight / destHeight
        val widthScale = srcWidth / destWidth

        val sampleScale = if (heightScale >  widthScale) {
            heightScale
        } else {
            widthScale
        }
        inSampleSize = sampleScale.roundToInt()
    }

    options = BitmapFactory.Options()
    options.inSampleSize = inSampleSize


    var rawBitmap = BitmapFactory.decodeFile(path, options)

    return when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 ->
            rotateImage(rawBitmap, 90.toFloat())
        ExifInterface.ORIENTATION_ROTATE_180 ->
            rotateImage(rawBitmap, 180.toFloat())
        ExifInterface.ORIENTATION_ROTATE_270 ->
            rotateImage(rawBitmap, 270.toFloat())
        else -> rotateImage(rawBitmap, 0.toFloat())
    }

}

fun rotateImage(source: Bitmap, angle: Float): Bitmap {
    val matrix = Matrix()
    matrix.postRotate(angle)
    return Bitmap.createBitmap(source, 0,0, source.width, source.height, matrix, true)
}


fun getScaledBitmap(path: String, activity: Activity, context: Context): Bitmap {
    val size = Point()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        context.display?.getRealSize(size)
    } else {
        activity.windowManager.defaultDisplay.getSize(size)
    }

    return getScaledBitmap(path, size.x, size.y)
}