package com.particle.sample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.particle.auth.AuthCore
import com.particle.auth.data.AuthCoreSignCallback
import com.particle.base.data.ErrorInfo
import com.particle.base.data.SignAllOutput
import com.particle.base.data.SignOutput
import com.particle.base.utils.Base58Utils
import com.particle.sample.databinding.FragmentSolanaTestBinding
import com.particle.sample.utils.SolanaTransactionManager.buildSerializedNativeTransaction
import com.particle.sample.utils.SolanaTransactionManager.buildSerializedSplTransaction
import com.particle.sample.utils.showMsg
import kotlinx.coroutines.launch

class SolanaTestFragment : Fragment() {

    private lateinit var binding: FragmentSolanaTestBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentSolanaTestBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpInitData()
        setListeners()
    }

    private fun setListeners() {
        binding.sendNative.setOnClickListener {
            if (!AuthCore.isConnected()) {
                showMsg("Error", "Failed: ${"not connected"}")
                return@setOnClickListener
            }
            val from = AuthCore.solana.getAddress()!!
            val to = binding.nativeReceiver.text.toString().trim()
            val amount = binding.nativeAmount.text.toString().trim().toBigDecimal().multiply(10.toBigDecimal().pow(9))
            lifecycleScope.launch {
                val serializedTrans = buildSerializedNativeTransaction(from, to, amount)!!
                AuthCore.solana.signAndSendTransaction(serializedTrans, object : AuthCoreSignCallback<SignOutput> {
                    override fun success(output: SignOutput) {
                        showMsg("SendNative Success", output.signature!!)
                    }

                    override fun failure(errMsg: ErrorInfo) {
                        showMsg("SendNative Failure", errMsg.toString())
                    }
                })
            }
        }
        binding.signTransactionNative.setOnClickListener {
            if (!AuthCore.isConnected()) {
                showMsg("Error", "Failed: ${"not connected"}")
                return@setOnClickListener
            }
            val from = AuthCore.solana.getAddress()!!
            val to = binding.nativeReceiver.text.toString().trim()
            val amount = binding.nativeAmount.text.toString().trim().toBigDecimal().multiply(10.toBigDecimal().pow(9))
            lifecycleScope.launch {
                val serializedTrans = buildSerializedNativeTransaction(from, to, amount)!!
                AuthCore.solana.signTransaction(serializedTrans, object : AuthCoreSignCallback<SignOutput> {
                    override fun success(output: SignOutput) {
                        showMsg("SignNative Success", output.signature!!)
                    }

                    override fun failure(errMsg: ErrorInfo) {
                        showMsg("SignNative Failure", errMsg.toString())
                    }

                })
            }
        }
        binding.sendNativeX2.setOnClickListener {
            if (!AuthCore.isConnected()) {
                showMsg("Error", "Failed: ${"not connected"}")
                return@setOnClickListener
            }
            val from = AuthCore.solana.getAddress()!!
            val to = binding.nativeReceiver.text.toString().trim()
            val amount = binding.nativeAmount.text.toString().trim().toBigDecimal().multiply(10.toBigDecimal().pow(9))
            lifecycleScope.launch {
                val serializedTrans1 = buildSerializedNativeTransaction(from, to, amount)!!
                val serializedTrans2 = buildSerializedNativeTransaction(from, to, amount)!!
                AuthCore.solana.signAllTransactions(listOf(serializedTrans1, serializedTrans2), object : AuthCoreSignCallback<SignAllOutput> {

                    override fun success(output: SignAllOutput) {
                        showMsg("SignAllTransactions Success", output.signatures.toString())
                    }

                    override fun failure(errMsg: ErrorInfo) {
                        showMsg("SignAllTransactions Failure", errMsg.toString())
                    }

                })
            }
        }
        binding.sendSplToken.setOnClickListener {
            if (!AuthCore.isConnected()) {
                showMsg("Error", "Failed: ${"not connected"}")
                return@setOnClickListener
            }
            val from = AuthCore.solana.getAddress()!!
            val to = binding.splTokenReceiver.text.toString().trim()
            val amount = binding.splTokenAmount.text.toString().trim().toBigDecimal().multiply(10.toBigDecimal().pow(9))
            val tokenContractAddress = binding.tokenContractAddress.text.toString().trim()
            lifecycleScope.launch {
                try {
                    val serializedTransaction = buildSerializedSplTransaction(from, to, tokenContractAddress, amount)!!
                    AuthCore.solana.signAndSendTransaction(serializedTransaction, object : AuthCoreSignCallback<SignOutput> {

                        override fun success(output: SignOutput) {
                            showMsg("SendSplToken Success", output.signature!!)
                        }

                        override fun failure(errMsg: ErrorInfo) {
                            showMsg("SendSplToken Failure", errMsg.toString())
                        }

                    })
                } catch (e: Exception) {
                    e.printStackTrace()
                    showMsg("Error", "Failed: ${e.message}")
                }


            }
        }
        binding.signTransactionToken.setOnClickListener {
            if (!AuthCore.isConnected()) {
                showMsg("Error", "Failed: ${"not connected"}")
                return@setOnClickListener
            }
            val from = AuthCore.solana.getAddress()!!
            val to = binding.splTokenReceiver.text.toString().trim()
            val amount = binding.splTokenAmount.text.toString().trim().toBigDecimal().multiply(10.toBigDecimal().pow(9))
            val tokenContractAddress = binding.tokenContractAddress.text.toString().trim()
            lifecycleScope.launch {
                val serializedTransaction = buildSerializedSplTransaction(from, to, tokenContractAddress, amount)!!
                AuthCore.solana.signTransaction(serializedTransaction, object : AuthCoreSignCallback<SignOutput> {
                    override fun success(output: SignOutput) {
                        showMsg("SignTransaction Success", output.signature!!)
                    }

                    override fun failure(errMsg: ErrorInfo) {
                        showMsg("SignTransaction Failure", errMsg.toString())
                    }

                })
            }
        }
        binding.sendSplTokenX2.setOnClickListener {
            if (!AuthCore.isConnected()) {
                showMsg("Error", "Failed: ${"not connected"}")
                return@setOnClickListener
            }
            val from = AuthCore.solana.getAddress()!!
            val to = binding.splTokenReceiver.text.toString().trim()
            val amount = binding.splTokenAmount.text.toString().trim().toBigDecimal().multiply(10.toBigDecimal().pow(9))
            val tokenContractAddress = binding.tokenContractAddress.text.toString().trim()
            lifecycleScope.launch {
                val serializedTransaction1 = buildSerializedSplTransaction(from, to, tokenContractAddress, amount)!!
                val serializedTransaction2 = buildSerializedSplTransaction(from, to, tokenContractAddress, amount)!!
                AuthCore.solana.signAllTransactions(
                    listOf(serializedTransaction1, serializedTransaction2),
                    object : AuthCoreSignCallback<SignAllOutput> {

                        override fun success(output: SignAllOutput) {
                            showMsg("signAllTransactions Success", output.signatures.toString())

                        }

                        override fun failure(errMsg: ErrorInfo) {
                            showMsg("signAllTransactions Failure", errMsg.toString())
                        }

                    })
            }
        }
        binding.btSignMessage.setOnClickListener {
            if (!AuthCore.isConnected()) {
                showMsg("Error", "Failed: ${"not connected"}")
                return@setOnClickListener
            }
            val message = binding.etSignMessage.text.toString().trim()
            lifecycleScope.launch {
                AuthCore.solana.signMessage(Base58Utils.encode(message.toByteArray(Charsets.UTF_8)), object : AuthCoreSignCallback<SignOutput> {
                    override fun success(output: SignOutput) {
                        showMsg("SignMessage Success", output.signature!!)
                    }

                    override fun failure(errMsg: ErrorInfo) {
                        showMsg("SignMessage failure", "errMsg:${errMsg.message}  ${errMsg.code}")
                    }
                })
            }
        }
    }


    private fun setUpInitData() {
        binding.etSignMessage.setText("Hello World")
    }
}