package com.i4evercai.luban.demo

import android.app.Activity
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import me.iwf.photopicker.PhotoPicker
import android.content.Intent
import io.reactivex.android.schedulers.AndroidSchedulers
import com.i4evercai.luban.Luban
import io.reactivex.schedulers.Schedulers
import io.reactivex.Flowable
import org.reactivestreams.Subscription
import java.io.File
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.Toast
import java.util.*


class MainActivity : AppCompatActivity() {
    private val TAG = "Luban"
    private val imageList = ArrayList<ImageBean>()
    private val adapter by lazy { ImageAdapter(this, imageList) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViews()
    }

    private fun initViews() {
        rvContent.layoutManager = LinearLayoutManager(this)
        rvContent.adapter = adapter
        fab.setOnClickListener {
            PhotoPicker.builder()
                    .setPhotoCount(9)
                    .setShowCamera(true)
                    .setShowGif(true)
                    .setPreviewEnabled(false)
                    .start(this@MainActivity, PhotoPicker.REQUEST_CODE);
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == PhotoPicker.REQUEST_CODE) {
            if (data != null) {
                imageList.clear()

                val photos = data.getStringArrayListExtra(PhotoPicker.KEY_SELECTED_PHOTOS)
                //        compressWithLs(photos);
                compressWithRx(photos)
            }
        }
    }

    private fun compressWithRx(photos: List<String>) {
        Log.d(TAG, "compressWithRx")
        Flowable.just(photos)
                .observeOn(Schedulers.io())
                .map { list ->

                    Luban.with(this@MainActivity)
                            .compressQuality(85)
                            .ignoreBy(150)
                            .load(list).get()
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ files: List<File> ->
                    Log.d(TAG, "Flowable subscribe : ${files.size}")
                    for (file in files) {
                        showResult(photos, file);
                    }
                }, { t: Throwable ->
                    t.printStackTrace()
                    Log.d(TAG, "Flowable error : ${t.localizedMessage}")
                    Toast.makeText(this, t.localizedMessage, Toast.LENGTH_SHORT).show()
                })
    }

    private fun showResult(photos: List<String>, file: File) {
        val originSize = computeSize(photos[adapter.getItemCount()])
        val thumbSize = computeSize(file.absolutePath)
        val originArg = String.format(Locale.CHINA, "原图参数：%d*%d, %dk", originSize[0], originSize[1], File(photos[adapter.getItemCount()]).length() shr 10)
        val thumbArg = String.format(Locale.CHINA, "压缩后参数：%d*%d, %dk", thumbSize[0], thumbSize[1], file.length() shr 10)
        Log.d(TAG, "showResult : ${originArg}")
        Log.d(TAG, "showResult : ${thumbArg}")
        val imageBean = ImageBean(originArg, thumbArg, file.absolutePath)
        imageList.add(imageBean)
        adapter.notifyDataSetChanged()
    }

    private fun computeSize(srcImg: String): IntArray {
        val size = IntArray(2)

        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        options.inSampleSize = 1

        BitmapFactory.decodeFile(srcImg, options)
        size[0] = options.outWidth
        size[1] = options.outHeight

        return size
    }
}
