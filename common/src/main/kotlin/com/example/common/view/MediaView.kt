package com.example.common.view

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.common.BASE_IMAGE_URL_ORIGINAL
import com.example.common.R
import com.example.common.extensions.changeVisibility

class MediaView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0):
    FrameLayout(context, attrs, defStyleAttr) {

    private val imageView: ImageView by lazy { findViewById(R.id.media_image) }
    private val imageLoading: ProgressBar by lazy { findViewById(R.id.media_loading) }

    private val listener: RequestListener<Drawable> = object: RequestListener<Drawable> {
        override fun onLoadFailed(
            e: GlideException?,
            model: Any?,
            target: Target<Drawable?>,
            isFirstResource: Boolean
        ): Boolean {
            imageLoading.changeVisibility(false)
            imageView.setImageDrawable(
                ResourcesCompat.getDrawable(context.resources, R.drawable.ic_error_load, null))
            return true
        }

        override fun onResourceReady(
            resource: Drawable,
            model: Any,
            target: Target<Drawable?>?,
            dataSource: DataSource,
            isFirstResource: Boolean
        ): Boolean {
            imageLoading.changeVisibility(false)
            return false
        }
    }

    init {
        inflate(context, R.layout.media_view, this)
    }

    fun loadImage(url: String?) {
        imageLoading.changeVisibility(true)
        Glide.with(this)
            .load(BASE_IMAGE_URL_ORIGINAL + url)
            .listener(listener)
            .into(imageView)
    }

}