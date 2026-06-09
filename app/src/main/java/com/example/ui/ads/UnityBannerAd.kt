package com.example.ui.ads

import android.util.Log
import android.widget.FrameLayout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.startapp.sdk.ads.banner.Banner

@Composable
fun UnityBannerAd(
    modifier: Modifier = Modifier,
    placementId: String = "Banner_Android" // Maintained signature representing placement container placeholder
) {
    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        factory = { context ->
            Log.d("StartioBannerAd", "Loading Start.io Banner ad with App ID 205164600")
            // Ensure the SDK is fully initialized
            AdManager.initialize(context)

            val frameLayout = FrameLayout(context).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
                )
            }
            try {
                // Instantiating Startapp dynamic banner view
                val banner = Banner(context).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    )
                }
                frameLayout.addView(banner)
                Log.d("StartioBannerAd", "Start.io Banner loaded in layout successfully")
            } catch (e: Throwable) {
                Log.e("StartioBannerAd", "Exception loading Start.io banner: ${e.message}", e)
            }
            frameLayout
        },
        update = { _ -> }
    )
}
