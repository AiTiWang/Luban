package com.i4evercai.luban

import android.graphics.BitmapFactory
import android.media.ExifInterface
import java.io.File
import android.graphics.Bitmap
import android.graphics.Matrix
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import java.io.IOException


/**
 * Responsible for starting compress and managing active and cached resources.
 */
class Engine {
    private var srcExif: ExifInterface? = null
    private val srcImg: String
    private val tagImg: File
    private var srcWidth: Int
    private var srcHeight: Int

    constructor(srcImg: String, tagImg: File) {
        this.srcImg = srcImg
        this.tagImg = tagImg

        if (Checker.isJPG(srcImg)) {
            srcExif = ExifInterface(srcImg)
        }

        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        options.inSampleSize = 1

        BitmapFactory.decodeFile(srcImg, options)
        this.srcWidth = options.outWidth
        this.srcHeight = options.outHeight
    }

    private fun computeSize(): Int {
        srcWidth = if (srcWidth % 2 == 1) srcWidth + 1 else srcWidth;
        srcHeight = if (srcHeight % 2 == 1) srcHeight + 1 else srcHeight

        val longSide = Math.max(srcWidth, srcHeight)
        val shortSide = Math.min(srcWidth, srcHeight)

        val scale = shortSide.toFloat() / longSide.toFloat()
        if (scale <= 1 && scale > 0.5625) {
            if (longSide < 1664) {
                return 1
            } else if (longSide >= 1664 && longSide < 4990) {
                return 2
            } else if (longSide > 4990 && longSide < 10240) {
                return 4
            } else {
                return if (longSide / 1280 == 0) 1 else longSide / 1280
            }
        } else if (scale <= 0.5625 && scale > 0.5) {
            return if (longSide / 1280 == 0) 1 else longSide / 1280
        } else {
            return Math.ceil(longSide / (1280.0 / scale)).toInt();
        }
    }

    private fun rotatingImage(bitmap: Bitmap): Bitmap {
        val exif = srcExif
        if (exif == null) return bitmap

        val matrix = Matrix()
        var angle = 0.0f
        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> angle = 90.0f
            ExifInterface.ORIENTATION_ROTATE_180 -> angle = 180.0f
            ExifInterface.ORIENTATION_ROTATE_270 -> angle = 270.0f
        }

        matrix.postRotate(angle)

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }


    @Throws(IOException::class)
    fun compress(compressQuality: Int): File {
        val options = BitmapFactory.Options()
        options.inSampleSize = computeSize()

        var tagBitmap = BitmapFactory.decodeFile(srcImg, options)
        val stream = ByteArrayOutputStream()

        tagBitmap = rotatingImage(tagBitmap)
        tagBitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, stream)
        tagBitmap.recycle()

        val fos = FileOutputStream(tagImg)
        fos.write(stream.toByteArray())
        fos.flush()
        fos.close()
        stream.close()

        return tagImg
    }
}