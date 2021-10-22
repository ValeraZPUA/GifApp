package com.example.gifapp.ui.fragments.oneGif.tools

import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide
import com.example.gifapp.R
import com.example.gifapp.db.entities.GifItemEntity

class GifPagerAdapter(private val gifList: ArrayList<GifItemEntity>,
                      private val onEndOfListReached: OnEndOfListReached): PagerAdapter() {

    override fun instantiateItem(container: ViewGroup, position: Int): Any {

            if (position == count - 1) {
                onEndOfListReached.onEndReached(count)
            }

            val linearLayout = LinearLayout(container.context)
            val layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            linearLayout.layoutParams = layoutParams

            val imageView = AppCompatImageView(container.context)
            imageView.scaleType = ImageView.ScaleType.CENTER
            val layoutParamsImageView = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            imageView.layoutParams = layoutParamsImageView
            val gifUrl = gifList[position].image_url

            Glide
                .with(container.context)
                .load(gifUrl)
                .fitCenter()
                //.apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE).skipMemoryCache(true))
                .thumbnail(Glide
                    .with(container.context)
                    .load(R.raw.loader))
                .into(imageView)

            linearLayout.addView(imageView)

            container.addView(linearLayout)

            return linearLayout

    }



    override fun getCount(): Int = gifList.size

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View?)
    }

    fun addItems(newGifsList: ArrayList<GifItemEntity>) {
        gifList.addAll(newGifsList)
        notifyDataSetChanged()

    }
}