package com.particle.sample

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.appcompat.app.AppCompatActivity
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItems
import com.particle.auth.AuthCore
import com.particle.auth.data.MasterPwdServiceCallback
import com.particle.base.LanguageEnum
import com.particle.base.ParticleNetwork
import com.particle.base.ThemeEnum
import com.particle.base.data.ErrorInfo
import com.particle.sample.databinding.ActivityMainBinding
import com.particle.sample.databinding.SettingsMenuBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        ParticleNetwork.setAppearence(ParticleNetwork.getAppearence())
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().replace(
                R.id.fragmentContainer, if (AuthCore.isConnected()) MainFragment.newInstance(true) else LoginFragment.newInstance()
            ).commit()
        }
        refreshSetting()
        binding.settings.setOnClickListener {
            popupMenu()
        }
    }


    fun replaceWithMain() {
        refreshSetting()
        supportFragmentManager.beginTransaction().replace(
            R.id.fragmentContainer, MainFragment.newInstance()
        ).commit()
    }

    fun replaceWithLogin() {
        refreshSetting()
        supportFragmentManager.beginTransaction().replace(
            R.id.fragmentContainer, LoginFragment.newInstance()
        ).commit()
    }

    fun refreshSetting() {
        binding.settings.visibility = if (AuthCore.isConnected()) View.VISIBLE else View.GONE
    }

    private fun popupMenu() {
        if (!AuthCore.isConnected()) {
            replaceWithLogin()
            return
        }
        val popBinding = SettingsMenuBinding.inflate(layoutInflater, null, false)
        val popWindow = PopupWindow(
            popBinding.root,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        if (AuthCore.hasMasterPassword()) {
            popBinding.setMasterPassword.visibility = View.GONE
            popBinding.changeMasterPassword.visibility = View.VISIBLE
        } else {
            popBinding.setMasterPassword.visibility = View.VISIBLE
            popBinding.changeMasterPassword.visibility = View.GONE
        }

        popBinding.accountSecurity.setOnClickListener {
            popWindow.dismiss()
            AuthCore.openAccountAndSecurity(this)
        }
        popBinding.setMasterPassword.setOnClickListener {
            popWindow.dismiss()
            AuthCore.setMasterPassword(object : MasterPwdServiceCallback {
                override fun success() {
                }

                override fun failure(errMsg: ErrorInfo) {
                }

            })
        }
        popBinding.changeMasterPassword.setOnClickListener {
            popWindow.dismiss()
            AuthCore.changeMasterPassword(object : MasterPwdServiceCallback {
                override fun success() {
                }

                override fun failure(errMsg: ErrorInfo) {
                }

            })
        }
        popBinding.language.setOnClickListener {
            val languageEnums = listOf(
                LanguageEnum.EN,
                LanguageEnum.ZH_CN,
                LanguageEnum.ZH_TW,
                LanguageEnum.KO,
                LanguageEnum.JA,
            )
            MaterialDialog(this@MainActivity).show {
                listItems(items = languageEnums.map { it.value }) { _, index, _ ->
                    val selected = languageEnums[index]
                    ParticleNetwork.setLanguage(selected, true)
                }
            }
        }
        popBinding.apperance.setOnClickListener {
            val apperances = listOf(
                ThemeEnum.SYSTEM,
                ThemeEnum.DARK,
                ThemeEnum.LIGHT,
            )
            MaterialDialog(this@MainActivity).show {
                listItems(items = apperances.map { it.value }) { _, index, _ ->
                    val selected = apperances[index]
                    ParticleNetwork.setAppearence(selected)
                    popWindow.dismiss()
                    (this@MainActivity as Activity).recreate()
                }
            }
        }
        popBinding.disconnect.setOnClickListener {
            AuthCore.disconnect(object : com.particle.base.model.ResultCallback {
                override fun success() {
                    replaceWithLogin()
                    popWindow.dismiss()
                }

                override fun failure() {
                }
            })

        }


        popWindow.showAsDropDown(binding.settings, 0, 0)
    }
}