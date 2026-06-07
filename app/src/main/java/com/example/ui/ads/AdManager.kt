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
    private var isInitialized = false
    private var isPreloaded = false

    fun initialize(context: Context) {
        if (isInitialized) return
        Log.d("AdManager", "Initializing Unity Ads with Game ID: $GAME_ID")
        
        UnityAds.initialize(
            context.applicationContext,
            GAME_ID,
            false, // Real ads as requested (false to serve real ads)
            object : IUnityAdsInitializationListener {
                override fun onInitializationComplete() {
                    isInitialized = true
                    Log.d("AdManager", "Unity Ads initialization completed successfully.")
                    preloadAd()
                }

                override fun onInitializationFailed(
                    error: UnityAds.UnityAdsInitializationError?,
                    id: String?
                ) {
                    Log.e("AdManager", "Unity Ads failed to initialize: code=$error, msg=$id")
                }
            }
        )
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

        Log.d("AdManager", "Loading and showing video ad...")
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
                            Log.e("AdManager", "Ad failed to show: error=$error, message=$message")
                            onAdFailed("Ad playback failed: $message")
                        }

                        override fun onUnityAdsShowStart(placementId: String?) {
                            Log.d("AdManager", "Ad started playing.")
                        }

                        override fun onUnityAdsShowClick(placementId: String?) {
                            Log.d("AdManager", "Ad was clicked.")
                        }

                        override fun onUnityAdsShowComplete(
                            placementId: String?,
                            state: UnityAds.UnityAdsShowCompletionState?
                        ) {
                            Log.d("AdManager", "Ad finished playing with state: $state")
                            preloadAd() // preload for next round
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
                Log.e("AdManager", "Failed to load ad: error=$error, msg=$message")
                onAdFailed("Failed to load video ad: $message. Please check your internet connection.")
            }
        })
    }
}
