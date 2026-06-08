package com.example.ui.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.unity3d.ads.IUnityAdsInitializationListener
import com.unity3d.ads.IUnityAdsLoadListener
import com.unity3d.ads.IUnityAdsShowListener
import com.unity3d.ads.UnityAds
import com.unity3d.ads.UnityAdsShowOptions

object AdManager {
    private const val GAME_ID = "6131288"
    private const val PLACEMENT_ID = "Rewarded_Android" // Standard Video/Rewarded Video Placement
    private const val INTERSTITIAL_ID = "Interstitial_Android" // Standard Non-rewarded Video placement
    private var isInitialized = false
    private var isPreloaded = false
    private var isInterstitialPreloaded = false

    fun initialize(context: Context) {
        // Re-initialization if initialized state differs can be bypassed, but UnityAds accepts initialize once.
        if (isInitialized) return
        Log.d("AdManager", "Initializing Unity Ads with Game ID: $GAME_ID (Live Mode Always)")
        try {
            UnityAds.initialize(
                context.applicationContext,
                GAME_ID,
                false, // Real ads as requested (false to serve real ads)
                object : IUnityAdsInitializationListener {
                    override fun onInitializationComplete() {
                        isInitialized = true
                        Log.d("AdManager", "Unity Ads initialization completed successfully for live ads on $GAME_ID.")
                        preloadAd()
                        preloadInterstitialAd()
                    }

                    override fun onInitializationFailed(
                        error: UnityAds.UnityAdsInitializationError?,
                        id: String?
                    ) {
                        Log.e("AdManager", "Unity Ads failed to initialize: code=$error, msg=$id")
                    }
                }
            )
        } catch (e: Throwable) {
            Log.e("AdManager", "Throwable during UnityAds.initialize: ${e.message}", e)
        }
    }

    fun preloadInterstitialAd() {
        if (!isInitialized) return
        Log.d("AdManager", "Preloading Interstitial Ad for placement: $INTERSTITIAL_ID")
        UnityAds.load(INTERSTITIAL_ID, object : IUnityAdsLoadListener {
            override fun onUnityAdsAdLoaded(placementId: String?) {
                isInterstitialPreloaded = true
                Log.d("AdManager", "Unity Interstitial Ad preloaded successfully")
            }

            override fun onUnityAdsFailedToLoad(
                placementId: String?,
                error: UnityAds.UnityAdsLoadError?,
                message: String?
            ) {
                isInterstitialPreloaded = false
                Log.e("AdManager", "Unity Interstitial ad failed to preload: $message")
            }
        })
    }

    fun preloadAd() {
        if (!isInitialized) return
        Log.d("AdManager", "Preloading Unity Ad for placement: $PLACEMENT_ID")
        UnityAds.load(PLACEMENT_ID, object : IUnityAdsLoadListener {
            override fun onUnityAdsAdLoaded(placementId: String?) {
                isPreloaded = true
                Log.d("AdManager", "Unity Ad preloaded successfully: $placementId")
            }

            override fun onUnityAdsFailedToLoad(
                placementId: String?,
                error: UnityAds.UnityAdsLoadError?,
                message: String?
            ) {
                isPreloaded = false
                Log.e("AdManager", "Unity Ad failed to preload: error=$error, msg=$message")
            }
        })
    }

    fun showAd(activity: Activity, onAdComplete: () -> Unit, onAdFailed: (String) -> Unit) {
        if (!isInitialized) {
            Log.d("AdManager", "Ad show skipped: Unity Ads not initialized yet.")
            onAdFailed("Unity Ads is not initialized yet. Please check your internet connection or try again shortly.")
            return
        }

        Log.d("AdManager", "Loading primary video ad ($PLACEMENT_ID)...")
        UnityAds.load(PLACEMENT_ID, object : IUnityAdsLoadListener {
            override fun onUnityAdsAdLoaded(placementId: String?) {
                UnityAds.show(
                    activity,
                    PLACEMENT_ID,
                    UnityAdsShowOptions(),
                    object : IUnityAdsShowListener {
                        override fun onUnityAdsShowFailure(
                            placementId: String?,
                            error: UnityAds.UnityAdsShowError?,
                            message: String?
                        ) {
                            Log.e("AdManager", "Primary ad show failed: $message. Fetching fallback...")
                            // Immediately fallback on show failure too!
                            showFallbackVideoAd(activity, onAdComplete, onAdFailed)
                        }

                        override fun onUnityAdsShowStart(placementId: String?) {
                            Log.d("AdManager", "Primary ad started.")
                        }

                        override fun onUnityAdsShowClick(placementId: String?) {
                            Log.d("AdManager", "Primary ad clicked.")
                        }

                        override fun onUnityAdsShowComplete(
                            placementId: String?,
                            state: UnityAds.UnityAdsShowCompletionState?
                        ) {
                            preloadAd()
                            if (state == UnityAds.UnityAdsShowCompletionState.COMPLETED) {
                                onAdComplete()
                            } else {
                                onAdFailed("Ad skipped. Please watch the full ad to unlock downloads!")
                            }
                        }
                    }
                )
            }

            override fun onUnityAdsFailedToLoad(
                placementId: String?,
                error: UnityAds.UnityAdsLoadError?,
                message: String?
            ) {
                Log.w("AdManager", "Primary placement failed: $message. Swapping to fallback: rewardedVideo...")
                showFallbackVideoAd(activity, onAdComplete, onAdFailed)
            }
        })
    }

    private fun showFallbackVideoAd(activity: Activity, onAdComplete: () -> Unit, onAdFailed: (String) -> Unit) {
        Log.d("AdManager", "Loading and showing fallback rewardedVideo...")
        UnityAds.load("rewardedVideo", object : IUnityAdsLoadListener {
            override fun onUnityAdsAdLoaded(placementId: String?) {
                UnityAds.show(
                    activity,
                    "rewardedVideo",
                    UnityAdsShowOptions(),
                    object : IUnityAdsShowListener {
                        override fun onUnityAdsShowFailure(pid: String?, err: UnityAds.UnityAdsShowError?, msg: String?) {
                            Log.e("AdManager", "Fallback ad show failure: $msg")
                            onAdFailed("Ad playback failed: $msg")
                        }
                        override fun onUnityAdsShowStart(pid: String?) {}
                        override fun onUnityAdsShowClick(pid: String?) {}
                        override fun onUnityAdsShowComplete(pid: String?, state: UnityAds.UnityAdsShowCompletionState?) {
                            if (state == UnityAds.UnityAdsShowCompletionState.COMPLETED) {
                                onAdComplete()
                            } else {
                                onAdFailed("Ad skipped. Please watch the full ad to unlock downloads!")
                            }
                        }
                    }
                )
            }

            override fun onUnityAdsFailedToLoad(pid: String?, err: UnityAds.UnityAdsLoadError?, msg: String?) {
                Log.e("AdManager", "Fallback placement also failed to load: $msg")
                onAdFailed("Failed to load live video ad: $msg.")
            }
        })
    }

    fun showInterstitialAd(activity: Activity, onAdComplete: () -> Unit, onAdFailed: (String) -> Unit) {
        if (!isInitialized) {
            Log.d("AdManager", "Interstitial ad skipped: Unity Ads not initialized yet.")
            onAdFailed("Unity Ads is not initialized yet.")
            return
        }

        Log.d("AdManager", "Loading primary Interstitial Ad ($INTERSTITIAL_ID)...")
        UnityAds.load(INTERSTITIAL_ID, object : IUnityAdsLoadListener {
            override fun onUnityAdsAdLoaded(placementId: String?) {
                UnityAds.show(
                    activity,
                    INTERSTITIAL_ID,
                    UnityAdsShowOptions(),
                    object : IUnityAdsShowListener {
                        override fun onUnityAdsShowFailure(
                            placementId: String?,
                            error: UnityAds.UnityAdsShowError?,
                            message: String?
                        ) {
                            Log.e("AdManager", "Primary Interstitial show failed: $message. Swapping to fallback...")
                            showFallbackInterstitialAd(activity, onAdComplete, onAdFailed)
                        }

                        override fun onUnityAdsShowStart(placementId: String?) {}
                        override fun onUnityAdsShowClick(placementId: String?) {}
                        override fun onUnityAdsShowComplete(
                            placementId: String?,
                            state: UnityAds.UnityAdsShowCompletionState?
                        ) {
                            preloadInterstitialAd()
                            onAdComplete()
                        }
                    }
                )
            }

            override fun onUnityAdsFailedToLoad(
                placementId: String?,
                error: UnityAds.UnityAdsLoadError?,
                message: String?
            ) {
                Log.w("AdManager", "Primary Interstitial fetch failed: $message. Swapping to fallback...")
                showFallbackInterstitialAd(activity, onAdComplete, onAdFailed)
            }
        })
    }

    private fun showFallbackInterstitialAd(activity: Activity, onAdComplete: () -> Unit, onAdFailed: (String) -> Unit) {
        Log.d("AdManager", "Loading and showing fallback video...")
        UnityAds.load("video", object : IUnityAdsLoadListener {
            override fun onUnityAdsAdLoaded(placementId: String?) {
                UnityAds.show(
                    activity,
                    "video",
                    UnityAdsShowOptions(),
                    object : IUnityAdsShowListener {
                        override fun onUnityAdsShowFailure(pid: String?, err: UnityAds.UnityAdsShowError?, msg: String?) {
                            Log.w("AdManager", "Fallback Interstitial show failed: $msg. Trying rewarded fallback.")
                            showAd(activity, onAdComplete, onAdFailed)
                        }
                        override fun onUnityAdsShowStart(pid: String?) {}
                        override fun onUnityAdsShowClick(pid: String?) {}
                        override fun onUnityAdsShowComplete(pid: String?, state: UnityAds.UnityAdsShowCompletionState?) {
                            onAdComplete()
                        }
                    }
                )
            }

            override fun onUnityAdsFailedToLoad(pid: String?, err: UnityAds.UnityAdsLoadError?, msg: String?) {
                Log.w("AdManager", "Fallback Interstitial load failed: $msg. Trying rewarded fallback.")
                showAd(activity, onAdComplete, onAdFailed)
            }
        })
    }

    fun setTestMode(context: Context, isTestMode: Boolean) {
        val sharedPrefs = context.getSharedPreferences("jk_settings", Context.MODE_PRIVATE)
        sharedPrefs.edit().putBoolean("unity_ads_test_mode", false).apply()
        try {
            UnityAds.initialize(
                context.applicationContext,
                GAME_ID,
                false, // Real ads strictly
                object : IUnityAdsInitializationListener {
                    override fun onInitializationComplete() {
                        isInitialized = true
                        Log.d("AdManager", "Unity Ads testMode updated dynamically: live strict")
                        preloadAd()
                        preloadInterstitialAd()
                    }
                    override fun onInitializationFailed(
                        error: UnityAds.UnityAdsInitializationError?,
                        id: String?
                    ) {
                        Log.e("AdManager", "Dynamic Unity Ads initialization failed: $error")
                    }
                }
            )
        } catch (e: Throwable) {
            Log.e("AdManager", "Throwable during dynamic re-init: ${e.message}")
        }
    }
}
