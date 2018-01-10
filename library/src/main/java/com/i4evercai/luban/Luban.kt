package com.i4evercai.luban

import android.content.Context
import java.io.File
import java.io.IOException
import android.text.TextUtils
import android.os.*
import android.util.Log
import android.support.annotation.UiThread
import android.support.annotation.WorkerThread


/**
 * Created by Fitz on 2018/1/9 0009.
 */
class Luban : Handler.Callback {

    private var mTargetDir: String
    private val mPaths: ArrayList<String>
    private val mLeastCompressSize: Int
    private val mCompressQuality: Int
    private val mCompressListener: OnCompressListener?

    private val mHandler: Handler

    companion object {
        private val TAG = "Luban"
        private val DEFAULT_DISK_CACHE_DIR = "luban_disk_cache"

        private val MSG_COMPRESS_SUCCESS = 0
        private val MSG_COMPRESS_START = 1
        private val MSG_COMPRESS_ERROR = 2

        fun with(context: Context): Builder {
            return Builder(context)
        }
    }

    private constructor(builder: Builder) {
        this.mPaths = builder.getPath()
        this.mTargetDir = builder.getTargetDir()
        this.mCompressListener = builder.getCompressListener()
        this.mLeastCompressSize = builder.getLeastCompressSize()
        this.mCompressQuality = builder.getCompressQuality()
        mHandler = Handler(Looper.getMainLooper(), this)
    }


    /**
     * Returns a mFile with a cache audio name in the private cache directory.
     *
     * @param context
     * A context.
     */
    @Throws(IOException::class)
    private fun getImageCacheFile(context: Context, suffix: String): File {
        if (TextUtils.isEmpty(mTargetDir)) {
            mTargetDir = getImageCacheDir(context).getAbsolutePath()
        }

        val cacheBuilder = mTargetDir + "/" +
                System.currentTimeMillis() +
                (Math.random() * 1000).toInt() +
                if (TextUtils.isEmpty(suffix)) ".jpg" else suffix

        return File(cacheBuilder)
    }

    /**
     * Returns a directory with a default name in the private cache directory of the application to
     * use to store retrieved audio.
     *
     * @param context
     * A context.
     *
     * @see .getImageCacheDir
     */
    @Throws(IOException::class)
    private fun getImageCacheDir(context: Context): File {
        return getImageCacheDir(context, DEFAULT_DISK_CACHE_DIR)
    }

    /**
     * Returns a directory with the given name in the private cache directory of the application to
     * use to store retrieved media and thumbnails.
     *
     * @param context
     * A context.
     * @param cacheName
     * The name of the subdirectory in which to store the cache.
     *
     * @see .getImageCacheDir
     */
    @Throws(IOException::class)
    private fun getImageCacheDir(context: Context, cacheName: String): File{
        val cacheDir = context.externalCacheDir
        if (cacheDir != null) {
            val result = File(cacheDir, cacheName)
            return if (!result.mkdirs() && (!result.exists() || !result.isDirectory)) {
                // File wasn't able to create a directory, or the result exists but not a directory
                throw IOException("File wasn't able to create a directory, or the result exists but not a directory")
            } else result
        }else{
            if (Log.isLoggable(TAG, Log.ERROR)) {
                Log.e(TAG, "default disk cache dir is null")
            }
            throw IOException("default disk cache dir is null")
        }

    }


    /**
     * start asynchronous compress thread
     */
    @UiThread
    private fun launch(context: Context) {
        if ( mPaths.size == 0 && mCompressListener != null) {
            mCompressListener!!.onError(NullPointerException("image file cannot be null"))
        }

        val iterator = mPaths.iterator()
        while (iterator.hasNext()) {
            val path = iterator.next()
            if (Checker.isImage(path)) {
                AsyncTask.SERIAL_EXECUTOR.execute {
                    try {
                        mHandler.sendMessage(mHandler.obtainMessage(MSG_COMPRESS_START))

                        val result = get(path,context)

                        mHandler.sendMessage(mHandler.obtainMessage(MSG_COMPRESS_SUCCESS, result))
                    } catch (e: IOException) {
                        mHandler.sendMessage(mHandler.obtainMessage(MSG_COMPRESS_ERROR, e))
                    }
                }
            } else {
                if (mCompressListener != null) {
                    mCompressListener!!.onError(IllegalArgumentException("can not read the path : " + path))
                }
            }
            iterator.remove()
        }
    }

    /**
     * start compress and return the mFile
     */
    @WorkerThread
    @Throws(IOException::class)
    private operator fun get(path: String, context: Context): File {
        val result = if (Checker.isNeedCompress(mLeastCompressSize, path))
            Engine(path, getImageCacheFile(context, Checker.checkSuffix(path))).compress(mCompressQuality)
        else
            File(path)
        return result
    }

    @WorkerThread
    @Throws(IOException::class)
    private operator fun get(context: Context): List<File> {
        val results = ArrayList<File>()
        val iterator = mPaths.iterator()

        while (iterator.hasNext()) {
            val path = iterator.next()
            if (Checker.isImage(path)) {
                val file = get(path,context)
                results.add(file)
            }
            iterator.remove()
        }
        return results
    }

    override fun handleMessage(msg: Message): Boolean {
        if (mCompressListener == null) return false

        when (msg.what) {
            MSG_COMPRESS_START -> mCompressListener.onStart()
            MSG_COMPRESS_SUCCESS -> mCompressListener.onSuccess(msg.obj as File)
            MSG_COMPRESS_ERROR -> mCompressListener.onError(msg.obj as Throwable)
        }
        return false
    }

    class Builder internal constructor(private val context: Context) {
        private var mTargetDir: String = ""
        private val mPaths = ArrayList<String>()
        private var mLeastCompressSize = 100
        private var mCompressListener: OnCompressListener? = null
        private var mCompressQuality = 60



        fun getPath(): ArrayList<String> {
            return mPaths
        }

        fun getTargetDir(): String {
            return mTargetDir
        }

        fun getCompressQuality(): Int {
            return mCompressQuality
        }

        fun getCompressListener(): OnCompressListener? {
            return mCompressListener
        }

        fun getLeastCompressSize(): Int {
            return mLeastCompressSize
        }

        private fun build(): Luban {
            return Luban(this)
        }

        fun load(file: File): Builder {
            this.mPaths.add(file.absolutePath)
            return this
        }

        fun load(string: String): Builder {
            this.mPaths.add(string)
            return this
        }

        fun load(list: List<String>): Builder {
            this.mPaths.addAll(list)
            return this
        }



        fun compressListener(listener: OnCompressListener): Builder {
            this.mCompressListener = listener
            return this
        }

        fun compressQuality(compressQuality: Int): Builder {
            this.mCompressQuality = compressQuality
            return this
        }

        fun targetDir(targetDir: String): Builder {
            this.mTargetDir = targetDir
            return this
        }

        /**
         * do not compress when the origin image file size less than one value
         *
         * @param size
         * the value of file size, unit KB, default 100K
         */
        fun ignoreBy(size: Int): Builder {
            this.mLeastCompressSize = size
            return this
        }

        /**
         * begin compress image with asynchronous
         */
        fun launch() {
            build().launch(context)
        }

        @Throws(IOException::class)
        operator fun get(path: String): File {
            return build().get(path, context)
        }

        /**
         * begin compress image with synchronize
         *
         * @return the thumb image file list
         */
        @Throws(IOException::class)
        fun get(): List<File> {
            return build().get(context)
        }
    }
}