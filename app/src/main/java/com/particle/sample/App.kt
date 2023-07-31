package com.particle.sample

import android.app.Activity
import android.app.Application
import android.webkit.WebView
import com.blankj.utilcode.util.SPUtils
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.DynamicColorsOptions
import com.particle.base.BuildConfig
import com.particle.base.Env
import com.particle.base.LanguageEnum
import com.particle.base.ParticleNetwork
import network.particle.chains.ChainInfo

/**
 * Created by chaichuanfa on 2023/5/18
 */
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(
            this,
            DynamicColorsOptions.Builder()
                .setPrecondition { activity: Activity?, theme: Int ->
                    SPUtils.getInstance().getBoolean("isDynamicColor", true)
                }
                .build())
        ParticleNetwork.init(this, Env.DEV, ChainInfo.EthereumGoerli)
        ParticleNetwork.setLanguage(LanguageEnum.EN)
        // for testing web view
        WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG)
    }
}