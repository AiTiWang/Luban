package com.i4evercai.luban.demo

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_image.view.*

/**
 * Created by Fitz on 2018/1/9 0009.
 */
class ImageAdapter : RecyclerView.Adapter<ImageAdapter.ImageHolder> {
    private val context: Context
    private val imageList: ArrayList<ImageBean>

    constructor(context: Context, imageList: ArrayList<ImageBean>) {
        this.context = context
        this.imageList = imageList
    }

    override fun onBindViewHolder(holder: ImageHolder?, position: Int) {
        if (holder != null) {
            holder.setData(imageList[position])
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ImageHolder {
        return ImageHolder(context,LayoutInflater.from(context).inflate(R.layout.item_image, parent, false))
    }

    override fun getItemCount(): Int = imageList.size

    class ImageHolder constructor(context: Context, view: View) : RecyclerView.ViewHolder(view) {
        private val context: Context
        init {
            this.context =context
        }
        fun setData(data: ImageBean) {
            with(itemView) {
                tvOriginArg.setText(data.originArg)
                tvThumbArg.setText(data.thumbArg)
                Glide.with(context)
                        .load(data.image)
                        .into(image);
            }
        }
    }
}