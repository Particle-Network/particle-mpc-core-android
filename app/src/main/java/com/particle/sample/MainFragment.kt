package com.particle.sample

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItems
import com.blankj.utilcode.util.ClipboardUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.ToastUtils
import com.particle.auth.AuthCore
import com.particle.auth.data.MasterPwdServiceCallback
import com.particle.auth.data.SyncUserInfoStatus
import com.particle.auth.data.req.JsonRpcRequest
import com.particle.base.ParticleNetwork
import com.particle.base.data.ErrorInfo
import com.particle.base.isTron
import com.particle.base.model.ResultCallback
import com.particle.base.utils.toTronBase58
import com.particle.mpc.data.ServerException
import com.particle.sample.databinding.FragmentMainBinding
import com.particle.sample.utils.formatDisplayAddress
import com.particle.sample.utils.hexAmount2UiAmount
import com.particle.sample.utils.showMsg
import com.particle.sample.utils.toast
import kotlinx.coroutines.launch
import network.particle.chains.ChainInfo

private const val SYNC_USER_INFO = "sync_user_info"

class MainFragment : Fragment() {
    private var syncUserInfo: Boolean = false

    private lateinit var binding: FragmentMainBinding

    companion object {
        @JvmStatic
        fun newInstance(sync: Boolean = false) = MainFragment().apply {
            arguments = Bundle().apply {
                putBoolean(SYNC_USER_INFO, sync)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            syncUserInfo = it.getBoolean(SYNC_USER_INFO)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        if (syncUserInfo) {
            syncUserInfo()
        }
    }

    private var currChain: ChainInfo? = null
    private val allChains = ChainInfo.getAllChains()
    private fun initView() {
        val index = SPUtils.getInstance().getInt("selected_chain_index", 0)
        currChain = allChains[index]
        ParticleNetwork.setChainInfo(currChain!!)
        refreshUI()
        setListeners()
        syncBalance()
    }


    @SuppressLint("CheckResult")
    fun setListeners() {
        binding.addressLayout.setOnClickListener {
            if (AuthCore.getChainInfo().isEvmChain()) {
                if (ParticleNetwork.chainInfo.isTron()) {
                    ClipboardUtils.copyText(AuthCore.evm.getAddress()!!.toTronBase58())
                } else {
                    ClipboardUtils.copyText(AuthCore.evm.getAddress()!!)
                }
            } else {
                ClipboardUtils.copyText(AuthCore.solana.getAddress()!!)
            }
            ToastUtils.showShort("Copied")
        }
        binding.tvChainInfo.setOnClickListener {
            MaterialDialog(requireContext()).show {
                listItems(items = allChains.map { "${it.fullname} - ${it.id}" }) { _, index, _ ->
                    currChain = allChains[index]
                    if (AuthCore.isConnected()) {
                        AuthCore.switchChain(currChain!!, object : ResultCallback {
                            override fun success() {
                                SPUtils.getInstance().put("selected_chain_index", index)
                                syncBalance()
                                refreshUI()
                            }

                            override fun failure() {
                                showMsg("Switch Chain", "Switch Chain Failed")
                            }

                        })
                    } else {
                        ParticleNetwork.setChainInfo(currChain!!)
                    }

                }
            }
        }
        binding.ivRefresh.setOnClickListener {
            syncBalance(true)
        }
    }

    private fun getCurrentWalletAddress(): String {
        if (!AuthCore.isConnected()) {
            return ""
        }
        return if (AuthCore.getChainInfo().isEvmChain()) {
            AuthCore.evm.getAddress()!!
        } else {
            AuthCore.solana.getAddress()!!
        }
    }

    private fun refreshUI() {
        LogUtils.d("refreshUI")
        try {
            AuthCore.getUserInfo()?.apply {
                LogUtils.d("getUserInfo", this)
                var address = getCurrentWalletAddress()
                if (ParticleNetwork.chainInfo.isTron()) {
                    address = address.toTronBase58()
                }
                binding.tvAddress.text = address.formatDisplayAddress()
            }
            binding.tvChainInfo.text = "${currChain!!.fullname} - ${currChain!!.id} "
            TransitionManager.beginDelayedTransition(binding.rlContainer, AutoTransition())

            if (ParticleNetwork.isEvmChain()) {
                childFragmentManager.beginTransaction().replace(R.id.rlContainer, EvmTestFragment())
                    .commit()
            } else {
                childFragmentManager.beginTransaction()
                    .replace(R.id.rlContainer, SolanaTestFragment()).commit()
            }


        } catch (e: Exception) {
            //ignore
        }
    }

    private fun syncUserInfo() {
        lifecycleScope.launch {
            try {
                val state = AuthCore.syncUserInfo()
                LogUtils.d("syncUserInfo: $state")
                if (state == SyncUserInfoStatus.USER_NOT_LOGIN) {
                    replaceWithLogin()
                }
//                else if (state == SyncUserInfoStatus.MASTER_PASSWORD_CHANGED) {
//                    recoverWallet()
//                }
            } catch (e: Exception) {
                e.printStackTrace()
                if (e is ServerException) {
                    AuthCore.disconnect(object : ResultCallback {
                        override fun success() {
                            replaceWithLogin()
                        }

                        override fun failure() {
                        }

                    })
                    toast("Server Error: code(${e.code}), ${e.message}")
                }
            }

        }
    }

    private fun recoverWallet() {
        AuthCore.recoverWallet(object : MasterPwdServiceCallback {
            override fun success() {

            }

            override fun failure(errMsg: ErrorInfo) {
                toast("Error: ${errMsg.message}")
            }
        })
    }

    private fun replaceWithLogin() {
        (requireActivity() as MainActivity).replaceWithLogin()
    }

    private fun syncBalance(forceRefresh: Boolean = false) {
        if (forceRefresh) {
            if (binding.ivRefresh.tag != null) {
                return
            }
            binding.ivRefresh.tag = "loading"
        } else {
            binding.balance.text = "---"
        }
        val animator = ObjectAnimator.ofFloat(binding.ivRefresh, "rotation", 0f, 360f)
            .setDuration(1000)
        animator.repeatCount = -1
        animator.start()
        lifecycleScope.launch {
            try {
                if (ParticleNetwork.isEvmChain()) {
                    val rs = AuthCore.evm.request(
                        JsonRpcRequest(
                            "eth_getBalance",
                            listOf(
                                getCurrentWalletAddress(),
                                "latest"
                            )
                        )
                    )
                    if (rs.result != null) {
                        val balance = rs.result!!.asString
                        LogUtils.d("balance: $balance")
                        binding.balance.text = balance.hexAmount2UiAmount()
                    }
                } else {
                    val rs = AuthCore.solana.request(
                        JsonRpcRequest(
                            "getBalance",
                            listOf(
                                getCurrentWalletAddress()
                            )
                        )
                    )
                    if (rs.result != null) {
                        val result = rs.result!!.asJsonObject
                        binding.balance.text = (result.get("value").asLong / 1e9).toString().dropLastWhile { it == '0' }
                            .dropLastWhile { it == '.' }
                    }
                }
                animator.cancel()
                binding.ivRefresh.rotation = 0f
                binding.ivRefresh.tag = null
            } catch (e: Exception) {
                e.printStackTrace()
                animator.cancel()
                binding.ivRefresh.rotation = 0f
                binding.ivRefresh.tag = null
            }
        }
    }


}