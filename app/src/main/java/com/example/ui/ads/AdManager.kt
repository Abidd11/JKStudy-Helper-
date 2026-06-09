package com.example.ui.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.startapp.sdk.adsbase.Ad
import com.startapp.sdk.adsbase.StartAppAd
import com.startapp.sdk.adsbase.StartAppSDK
import com.startapp.sdk.adsbase.adlisteners.AdDisplayListener
import com.startapp.sdk.adsbase.adlisteners.AdEventListener

object AdManager {
    private const val APP_ID = "205164600"
    private var isInitialized = false

    fun initialize(context: Context) {
        if (isInitialized) return
        Log.d("AdManager", "Initializing Start.io Ads with App ID: $APP_ID")
        try {
            StartAppSDK.init(context.applicationContext, APP_ID, false)
            StartAppAd.disableSplash() // Disables start.io auto splash ad since we have our custom premium splash screen
            isInitialized = true
            Log.d("AdManager", "Start.io Ads initialization completed successfully.")
        } catch (e: Throwable) {
            Log.e("AdManager", "Error during StartAppSDK.init: ${e.message}", e)
        }
    }

    fun showAd(activity: Activity, onAdComplete: () -> Unit, onAdFailed: (String) -> Unit) {
        initialize(activity)
        Log.d("AdManager", "Showing Start.io video/rewarded ad...")
        try {
            val startAppAd = StartAppAd(activity)
            startAppAd.loadAd(StartAppAd.AdMode.REWARDED_VIDEO, object : AdEventListener {
                override fun onReceiveAd(ad: Ad) {
                    startAppAd.showAd(object : AdDisplayListener {
                        override fun adDisplayed(ad: Ad?) {
                            Log.d("AdManager", "Start.io rewarded video adDisplayed")
                        }
                        override fun adNotDisplayed(ad: Ad?) {
                            Log.w("AdManager", "Start.io rewarded video adNotDisplayed")
                            onAdFailed("Ad display failed. Please try again.")
                        }
                        override fun adClicked(ad: Ad?) {
                            Log.d("AdManager", "Start.io rewarded video adClicked")
                        }
                        override fun adHidden(ad: Ad?) {
                            Log.d("AdManager", "Start.io rewarded video adHidden")
                            onAdComplete()
                        }
                    })
                }

                override fun onFailedToReceiveAd(ad: Ad?) {
                    Log.w("AdManager", "Failed to receive Start.io rewarded video ad, fallback to interstitial...")
                    showInterstitialAd(activity, onAdComplete, onAdFailed)
                }
            })
        } catch (e: Throwable) {
            Log.e("AdManager", "Exception in showAd: ${e.message}", e)
            onAdFailed("Ad playback failed.")
        }
    }

    fun showInterstitialAd(activity: Activity, onAdComplete: () -> Unit, onAdFailed: (String) -> Unit) {
        initialize(activity)
        Log.d("AdManager", "Showing Start.io Interstitial Ad...")
        try {
            val startAppAd = StartAppAd(activity)
            startAppAd.loadAd(StartAppAd.AdMode.AUTOMATIC, object : AdEventListener {
                override fun onReceiveAd(ad: Ad) {
                    startAppAd.showAd(object : AdDisplayListener {
                        override fun adDisplayed(ad: Ad?) {
                            Log.d("AdManager", "Start.io interstitial adDisplayed")
                        }
                        override fun adNotDisplayed(ad: Ad?) {
                            Log.w("AdManager", "Start.io interstitial adNotDisplayed")
                            onAdFailed("Ad display failed.")
                        }
                        override fun adClicked(ad: Ad?) {}
                        override fun adHidden(ad: Ad?) {
                            Log.d("AdManager", "Start.io interstitial adHidden")
                            onAdComplete()
                        }
                    })
                }

                override fun onFailedToReceiveAd(ad: Ad?) {
                    Log.e("AdManager", "Failed to receive Start.io interstitial ad.")
                    onAdFailed("Failed to load ad: ${ad?.errorMessage ?: "No internet connection"}")
                }
            })
        } catch (e: Throwable) {
            Log.e("AdManager", "Exception in showInterstitialAd: ${e.message}", e)
            onAdFailed("Interstitial playback failed.")
        }
    }

    fun setTestMode(context: Context, isTestMode: Boolean) {
        // Startapp SDK handles test ads automatically based on emulator/live or configuration,
        // so we don't need dynamic toggling.
    }
}
