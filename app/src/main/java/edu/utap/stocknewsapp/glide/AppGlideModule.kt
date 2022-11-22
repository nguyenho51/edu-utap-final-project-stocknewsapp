package edu.utap.stocknewsapp.glide

import android.content.Context
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.os.Build
import android.text.Html
import android.util.Log
import android.widget.ImageView
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestOptions
import android.os.ParcelFileDescriptor.open
import edu.utap.stocknewsapp.R
import java.io.InputStream

@GlideModule
class AppGlideModule : AppGlideModule() {
    override fun applyOptions(context: Context, builder: GlideBuilder) {
        // You can change this to make Glide more verbose
        builder.setLogLevel(Log.ERROR)
    }
    override fun isManifestParsingEnabled(): Boolean {
        return false
    }
}

// Calling glideapp.with with the most specific Activity/Fragment
// context allows it to track lifecycles for your fetch
// https://stackoverflow.com/questions/31964737/glide-image-loading-with-application-context
object Glide {
    private val width = Resources.getSystem().displayMetrics.widthPixels
    private val height = Resources.getSystem().displayMetrics.heightPixels
    private var glideOptions = RequestOptions ()
        // Options like CenterCrop are possible, but I like this one best
        // Evidently you need fitCenter or dontTransform.  If you use centerCrop, your
        // list disappears.  I think that was an old bug.
        .fitCenter()
        // Rounded corners are so lovely.
        .transform(RoundedCorners (10))

    private fun fromHtml(source: String): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY).toString()
        } else {
            @Suppress("DEPRECATION")
            return Html.fromHtml(source).toString()
        }
    }

    fun glideFetch(urlString: String, imageView: ImageView) {
        GlideApp.with(imageView.context)
            .asBitmap() // Try to display animated Gifs and video still
            .load(fromHtml(urlString))
            .apply(glideOptions)
            .error(R.drawable.no_image_found)
            .override(width, height)
            .into(imageView)
    }
}
