package com.i4evercai.luban

import android.text.TextUtils
import java.io.File


/**
 * Created by Fitz on 2018/1/9 0009.
 */
object  Checker {

    private val JPG = "jpg"
    private val JPEG = "jpeg"
    private val PNG = "png"
    private val WEBP = "webp"
    private val GIF = "gif"

    private val format = arrayListOf(JPG, JPEG, PNG,WEBP,GIF)

    fun isImage(path: String?): Boolean {
        if (TextUtils.isEmpty(path)) {
            return false
        }

        val suffix = path!!.substring(path.lastIndexOf(".") + 1, path.length)
        return format.contains(suffix.toLowerCase())
    }

    fun isJPG(path: String): Boolean {
        if (TextUtils.isEmpty(path)) {
            return false
        }

        val suffix = path.substring(path.lastIndexOf("."), path.length).toLowerCase()
        return suffix.contains(JPG) || suffix.contains(JPEG)
    }

    fun checkSuffix(path: String): String {
        return if (TextUtils.isEmpty(path)) {
            ".jpg"
        } else path.substring(path.lastIndexOf("."), path.length)

    }

    fun isNeedCompress(leastCompressSize: Int, path: String): Boolean {
        if (leastCompressSize > 0) {
            val source = File(path)
            if (!source.exists()) {
                return false
            }

            if (source.length() <= leastCompressSize*1024) {
                return false
            }
        }
        return true
    }

}