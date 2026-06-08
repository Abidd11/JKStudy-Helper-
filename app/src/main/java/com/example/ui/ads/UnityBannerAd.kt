package com.example.ui.ads

import android.app.Activity
import android.util.Log
import android.widget.FrameLayout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.unity3d.services.banners.BannerErrorInfo
import com.unity3d.services.banners.BannerView
import com.unity3d.services.banners.UnityBannerSize

@Composable
fun UnityBannerAd(
    modifier: Modifier = Modifier,
    placementId: String = "Banner_Android"
) {
    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        factory = { context ->
            val activity = context as? Activity
            val frameLayout = FrameLayout(context).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
                )
            }
            if (activity != null) {
                try {
                    val bannerView = BannerView(
                        activity,
                        placementId,
                        UnityBannerSize(320, 50)
                    )
                    bannerView.listener = object : BannerView.IListener {
                        override fun onBannerLoaded(bannerAdView: BannerView?) {
                            Log.d("UnityBannerAd", "Banner loaded successfully: $placementId")
                        }

                        override fun onBannerFailedToLoad(bannerAdView: BannerView?, errorInfo: BannerErrorInfo?) {
                            Log.e("UnityBannerAd", "Banner failed to load: ${errorInfo?.errorMessage}")
                            // Try loading with different placement case (e.g. lowercase "banner") as dynamic fallback!
                            if (placementId == "Banner_Android") {
                                Log.d("UnityBannerAd", "Primary failed. Swapping to fallback 'banner' placement...")
                                try {
                                    val fallbackBanner = BannerView(activity, "banner", UnityBannerSize(320, 50))
                                    fallbackBanner.listener = object : BannerView.IListener {
                                        override fun onBannerLoaded(b: BannerView?) {
                                            Log.d("UnityBannerAd", "Fallback Banner loaded successfully")
                                        }
                                        override fun onBannerFailedToLoad(b: BannerView?, err: BannerErrorInfo?) {
                                            Log.e("UnityBannerAd", "Fallback Banner failed to load too: ${err?.errorMessage}")
                                        }
                                        override fun onBannerClick(b: BannerView?) {}
                                        override fun onBannerLeftApplication(b: BannerView?) {}
                                        override fun onBannerShown(b: BannerView?) {
                                            Log.d("UnityBannerAd", "Fallback Banner shown!")
                                        }
                                    }
                                    frameLayout.removeAllViews()
                                    frameLayout.addView(fallbackBanner)
                                    fallbackBanner.load()
                                } catch (e: Throwable) {
                                    Log.e("UnityBannerAd", "Fallback BannerView error: ${e.message}")
                                }
                            }
                        }

                        override fun onBannerClick(bannerAdView: BannerView?) {
                            Log.d("UnityBannerAd", "Banner clicked!")
                        }

                        override fun onBannerLeftApplication(bannerAdView: BannerView?) {
                            Log.d("UnityBannerAd", "Banner left application!")
                        }

                        override fun onBannerShown(bannerAdView: BannerView?) {
                            Log.d("UnityBannerAd", "Banner shown successfully: $placementId")
                        }
                    }
                    frameLayout.addView(bannerView)
                    bannerView.load()
                } catch (e: Throwable) {
                    Log.e("UnityBannerAd", "Error creating BannerView: ${e.message}", e)
                }
            }
            frameLayout
        },
        update = { _ -> }
    )
}
