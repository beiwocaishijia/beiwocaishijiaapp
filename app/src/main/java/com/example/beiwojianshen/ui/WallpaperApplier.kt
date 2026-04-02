package com.example.beiwojianshen.ui

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.view.View
import android.widget.ImageView
import com.example.beiwojianshen.data.WallpaperPrefs

object WallpaperApplier {
    private var cachedUri: String? = null
    private var cachedDrawable: Drawable? = null

    fun apply(context: Context, imageView: ImageView) {
        val uriString = WallpaperPrefs.get(context)
        if (uriString.isNullOrBlank()) {
            cachedUri = null
            cachedDrawable = null
            imageView.setImageDrawable(null)
            imageView.visibility = View.GONE
            return
        }

        if (cachedUri == uriString && cachedDrawable != null) {
            imageView.setImageDrawable(cachedDrawable)
            imageView.visibility = View.VISIBLE
            return
        }

        val uri = runCatching { Uri.parse(uriString) }.getOrNull()
        if (uri == null) {
            imageView.setImageDrawable(null)
            imageView.visibility = View.GONE
            return
        }

        val drawable = runCatching { loadDrawable(context, uri) }.getOrNull()
        cachedUri = uriString
        cachedDrawable = drawable
        imageView.setImageDrawable(drawable)
        imageView.visibility = if (drawable == null) View.GONE else View.VISIBLE
    }

    private fun loadDrawable(context: Context, uri: Uri): Drawable? {
        return if (Build.VERSION.SDK_INT >= 28) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeDrawable(source)
        } else {
            context.contentResolver.openInputStream(uri)?.use { input ->
                val bitmap = BitmapFactory.decodeStream(input) ?: return null
                android.graphics.drawable.BitmapDrawable(context.resources, bitmap)
            }
        }
    }
}
