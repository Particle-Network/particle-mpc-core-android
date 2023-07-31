package com.particle.sample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.particle.auth.AuthCore
import com.particle.auth.data.AuthCoreSignCallback
import com.particle.auth.data.req.JsonRpcRequest
import com.particle.auth.utils.EthersUtils
import com.particle.base.ParticleNetwork
import com.particle.base.data.ErrorInfo
import com.particle.base.data.SignOutput
import com.particle.base.isTron
import com.particle.base.utils.HexUtils
import com.particle.sample.databinding.FragmentEvmTestBinding
import com.particle.sample.utils.MockManger
import com.particle.sample.utils.StreamUtils
import com.particle.sample.utils.TestAccount
import com.particle.sample.utils.isSolPublicKey
import com.particle.sample.utils.showMsg
import com.particle.sample.utils.toast
import com.particle.sample.utils.uiAmount2Amount
import com.particle.sample.utils.uiAmount2HexAmount
import kotlinx.coroutines.launch

class EvmTestFragment : Fragment() {
    private lateinit var binding: FragmentEvmTestBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentEvmTestBinding.inflate(inflater, container, false)
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
            if (ParticleNetwork.isEvmChain()) lifecycleScope.launch {
                val from = AuthCore.evm.getAddress()!!
                val to = binding.nativeReceiver.text.toString().trim()
                if (ParticleNetwork.chainInfo.isTron()) {
                    if (!to.isSolPublicKey()) {
                        showMsg("Error", "Failed: ${"to address is not valid"}")
                        return@launch
                    }
                }
                val uiAmount = binding.nativeAmount.text.toString().trim()
                val value = uiAmount.uiAmount2HexAmount()
                val trans = MockManger.mockCreateTransaction(from, to, value)
                AuthCore.evm.sendTransaction(trans, object : AuthCoreSignCallback<SignOutput> {
                    override fun success(output: SignOutput) {
                        showMsg("Send Native", "Success:${output.signature}")
                    }

                    override fun failure(errMsg: ErrorInfo) {
                        showMsg("Send Native", "Failed: ${errMsg.message}")
                    }

                })
            }

        }
        binding.sendERC20.setOnClickListener {
            if (!AuthCore.isConnected()) {
                showMsg("Error", "Failed: ${"not connected"}")
                return@setOnClickListener
            }
            lifecycleScope.launch {
                val from = AuthCore.evm.getAddress()!!
                val to = binding.erc20Receiver.text.toString().trim()
                if (ParticleNetwork.chainInfo.isTron()) {
                    if (!to.isSolPublicKey()) {
                        showMsg("Error", "Failed: ${"to address is not valid"}")
                        return@launch
                    }
                }
                val uiAmount = binding.erc20Amount.text.toString().trim()
                val contractAddress = binding.erc20ContractAddress.text.toString().trim()

                val list2 = arrayListOf(to, uiAmount.uiAmount2Amount())
                val list1 = arrayListOf(contractAddress, "erc20_transfer", list2)
                val rs = AuthCore.evm.request(
                    JsonRpcRequest(
                        "particle_abi_encodeFunctionCall", list1
                    )
                )
                if (rs.result != null) {
                    val data = rs.result!!.asString
                    val trans =
                        MockManger.mockCreateTransaction(from, contractAddress, "0x0", data = data)
                    AuthCore.evm.sendTransaction(trans, object : AuthCoreSignCallback<SignOutput> {
                        override fun success(output: SignOutput) {
                            showMsg("Send ERC20", "Success:${output.signature}")
                        }

                        override fun failure(errMsg: ErrorInfo) {
                            showMsg("Send ERC20", "Failed: ${errMsg.message}")
                        }

                    })
                } else {
                    showMsg("Send ERC20", "Failed: ${rs.error!!.message} ${rs.error!!.data}")
                }
            }

        }
        binding.approveERC20.setOnClickListener {
            if (!AuthCore.isConnected()) {
                showMsg("Error", "Failed: ${"not connected"}")
                return@setOnClickListener
            }
            lifecycleScope.launch {
                val from = AuthCore.evm.getAddress()!!
                val to = binding.approveERC20Receiver.text.toString().trim()
                if (ParticleNetwork.chainInfo.isTron()) {
                    if (!to.isSolPublicKey()) {
                        showMsg("Error", "Failed: ${"to address is not valid"}")
                        return@launch
                    }
                }
                val uiAmount = binding.approveERC20Amount.text.toString().trim()
                val contractAddress = binding.approveERC20ContractAddress.text.toString().trim()
                if (uiAmount.isEmpty() || contractAddress.isEmpty() || to.isEmpty()) {
                    toast("Please input all params")
                    return@launch
                }
                val list2 = arrayListOf(from, uiAmount.uiAmount2Amount())
                val list1 = arrayListOf(contractAddress, "erc20_approve", list2)
                val rs = AuthCore.evm.request(
                    JsonRpcRequest(
                        "particle_abi_encodeFunctionCall", list1
                    )
                )
                if (rs.result != null) {
                    val data = rs.result!!.asString
                    val trans = MockManger.mockCreateTransaction(from, to, "0x0", data = data)
                    AuthCore.evm.sendTransaction(trans, object : AuthCoreSignCallback<SignOutput> {
                        override fun success(output: SignOutput) {
                            showMsg("Approve ERC20", "Success:${output.signature}")
                        }

                        override fun failure(errMsg: ErrorInfo) {
                            showMsg("Approve ERC20", "Failed: ${errMsg.message}")
                        }

                    })
                } else {
                    showMsg("Approve ERC20", "Failed: ${rs.error!!.message}")
                }
            }
        }
        binding.sendERC721.setOnClickListener {
            if (!AuthCore.isConnected()) {
                showMsg("Error", "Failed: ${"not connected"}")
                return@setOnClickListener
            }
            lifecycleScope.launch {
                val from = AuthCore.evm.getAddress()!!
                val to = binding.erc721Receiver.text.toString().trim()
                if (ParticleNetwork.chainInfo.isTron()) {
                    if (!to.isSolPublicKey()) {
                        showMsg("Error", "Failed: ${"to address is not valid"}")
                        return@launch
                    }
                }
                val erc721TokenId = binding.erc721TokenId.text.toString().trim()
                val contractAddress = binding.erc721ContractAddress.text.toString().trim()

                if (erc721TokenId.isEmpty() || contractAddress.isEmpty() || to.isEmpty()) {
                    toast("Please input all params")
                    return@launch
                }

                val list2 = arrayListOf(from, to, erc721TokenId)
                val list1 = arrayListOf(contractAddress, "erc721_safeTransferFrom", list2)
                val rs = AuthCore.evm.request(
                    JsonRpcRequest(
                        "particle_abi_encodeFunctionCall", list1
                    )
                )
                if (rs.result != null) {
                    val data = rs.result!!.asString
                    val trans = MockManger.mockCreateTransaction(from, to, "0x0", data = data)
                    AuthCore.evm.sendTransaction(trans, object : AuthCoreSignCallback<SignOutput> {
                        override fun success(output: SignOutput) {
                            showMsg("Send ERC721", "Success:${output.signature}")
                        }

                        override fun failure(errMsg: ErrorInfo) {
                            showMsg("Send ERC721", "Failed: ${errMsg.message}")
                        }

                    })
                } else {
                    showMsg("Send ERC721", "Failed: ${rs.error!!.message}")
                }
            }
        }
        binding.sendERC1155.setOnClickListener {
            if (!AuthCore.isConnected()) {
                showMsg("Error", "Failed: ${"not connected"}")
                return@setOnClickListener
            }
            lifecycleScope.launch {
                val from = AuthCore.evm.getAddress()!!
                val to = binding.erc1155Receiver.text.toString().trim()
                if (ParticleNetwork.chainInfo.isTron()) {
                    if (!to.isSolPublicKey()) {
                        showMsg("Error", "Failed: ${"to address is not valid"}")
                        return@launch
                    }
                }
                val erc1155TokenId = binding.erc1155TokenId.text.toString().trim()
                val contractAddress = binding.erc1155ContractAddress.text.toString().trim()

                if (erc1155TokenId.isEmpty() || contractAddress.isEmpty() || to.isEmpty()) {
                    toast("Please input all params")
                    return@launch
                }

                val list2 = arrayListOf(from, to, erc1155TokenId, "1", "0x")
                val list1 = arrayListOf(contractAddress, "erc1155_safeTransferFrom", list2)
                val rs = AuthCore.evm.request(
                    JsonRpcRequest(
                        "particle_abi_encodeFunctionCall", list1
                    )
                )
                if (rs.result != null) {
                    val data = rs.result!!.asString
                    val trans = MockManger.mockCreateTransaction(from, to, "0x0", data = data)
                    AuthCore.evm.sendTransaction(trans, object : AuthCoreSignCallback<SignOutput> {
                        override fun success(output: SignOutput) {
                            showMsg("Send ERC1155", "Success:${output.signature}")
                        }

                        override fun failure(errMsg: ErrorInfo) {
                            showMsg("Send ERC1155", "Failed: ${errMsg.message}")
                        }
                    })
                } else {
                    showMsg("Send ERC1155", "Failed: ${rs.error!!.message}")
                }
            }
        }
        binding.personalSign.setOnClickListener {
            if (!AuthCore.isConnected()) {
                showMsg("Error", "Failed: ${"not connected"}")
                return@setOnClickListener
            }
            val isChecked = binding.personalSignUniq.isChecked
            lifecycleScope.launch {
                val msg = binding.personalSignMessage.text.toString().trim()
                HexUtils.encodeWithPrefix(msg.toByteArray(Charsets.UTF_8))
                if (isChecked) {
                    AuthCore.evm.personalSign(msg, signMessageCallBack)
                } else {
                    AuthCore.evm.personalSignUnique(msg, signMessageCallBack)
                }
            }
        }
        binding.signTypedData.setOnClickListener {
            if (!AuthCore.isConnected()) {
                showMsg("Error", "Failed: ${"not connected"}")
                return@setOnClickListener
            }
            if (!ParticleNetwork.chainInfo.isEvmChain()) {
                ToastUtils.showLong("only support evm chain")
                return@setOnClickListener
            }
            val isChecked = binding.signTypedDataUniq.isChecked
            val typedDate = binding.signTypedDataMessage.text.toString().trim()
            lifecycleScope.launch {
                val hexMessage = HexUtils.encodeWithPrefix(typedDate.toByteArray(Charsets.UTF_8))
                if (isChecked) AuthCore.evm.signTypedDataUnique(hexMessage, typedDataCallback)
                else AuthCore.evm.signTypedData(hexMessage, typedDataCallback)
            }
        }

        binding.writeContract.setOnCheckedChangeListener { buttonView, isChecked ->
            binding.contractCall.text = if (isChecked) "Write" else "Query"
        }
        binding.contractCall.setOnClickListener {
            contractCall()
        }
    }

    private fun contractCall() {
        val contractAddress = binding.contractAddress.text.trim().toString()
        if (contractAddress.isEmpty() && !EthersUtils.isValidAddress(contractAddress)) {
            toast("Please input correct contract address")
            return
        }

        val contractMethod = binding.contractMethod.text.trim()
        if (contractMethod.isEmpty()) {
            toast("Please input correct contract method")
            return
        }

        val contractABI = binding.contractABI.text.trim()
        if (contractABI.isEmpty()) {
            toast("Please input correct contract ABI")
            return
        }

        val methodParams = binding.methodParams.text.trim()
        val params = mutableListOf<String>()
        if (methodParams.isNotEmpty()) {
            params.addAll(methodParams.split(','))
        }

        val isWrite = binding.writeContract.isChecked

        //https://github.com/web3j/web3j/blob/master/abi/src/main/java/org/web3j/abi/datatypes/Function.java
        //https://docs.web3j.io/4.8.7/transactions/transactions_and_smart_contracts/#transacting-with-a-smart-contract
        //todo read or write contract
    }

    private val signMessageCallBack = object : AuthCoreSignCallback<SignOutput> {
        override fun success(output: SignOutput) {
            LogUtils.d("AuthCore EVM personalSign ${output.signature}")
            val result = EthersUtils.recoverPersonalSignature("Hello World", output.signature!!)
            LogUtils.d("AuthCore EVM personalSign recover $result")
            showMsg("Personal Sign", "Success:${output.signature} \n recover:\n $result")
        }

        override fun failure(errMsg: ErrorInfo) {
            showMsg("Personal Sign", "Failed: ${errMsg.message}")
        }
    }

    private val typedDataCallback = object : AuthCoreSignCallback<SignOutput> {
        override fun success(output: SignOutput) {
            LogUtils.d("AuthCore EVM signTypedData ${output.signature}")
            showMsg("signTypedData success", "Success:${output.signature}")
        }

        override fun failure(errMsg: ErrorInfo) {
            LogUtils.d("AuthCore EVM signTypedData failure ${errMsg.message}")
            showMsg("signTypedData failure", errMsg.message!!)
        }
    }

    private fun setUpInitData() {
        val typedData =
            "{\"types\":{\"OrderComponents\":[{\"name\":\"offerer\",\"type\":\"address\"},{\"name\":\"zone\",\"type\":\"address\"},{\"name\":\"offer\",\"type\":\"OfferItem[]\"},{\"name\":\"consideration\",\"type\":\"ConsiderationItem[]\"},{\"name\":\"orderType\",\"type\":\"uint8\"},{\"name\":\"startTime\",\"type\":\"uint256\"},{\"name\":\"endTime\",\"type\":\"uint256\"},{\"name\":\"zoneHash\",\"type\":\"bytes32\"},{\"name\":\"salt\",\"type\":\"uint256\"},{\"name\":\"conduitKey\",\"type\":\"bytes32\"},{\"name\":\"counter\",\"type\":\"uint256\"}],\"OfferItem\":[{\"name\":\"itemType\",\"type\":\"uint8\"},{\"name\":\"token\",\"type\":\"address\"},{\"name\":\"identifierOrCriteria\",\"type\":\"uint256\"},{\"name\":\"startAmount\",\"type\":\"uint256\"},{\"name\":\"endAmount\",\"type\":\"uint256\"}],\"ConsiderationItem\":[{\"name\":\"itemType\",\"type\":\"uint8\"},{\"name\":\"token\",\"type\":\"address\"},{\"name\":\"identifierOrCriteria\",\"type\":\"uint256\"},{\"name\":\"startAmount\",\"type\":\"uint256\"},{\"name\":\"endAmount\",\"type\":\"uint256\"},{\"name\":\"recipient\",\"type\":\"address\"}],\"EIP712Domain\":[{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"version\",\"type\":\"string\"},{\"name\":\"chainId\",\"type\":\"uint256\"},{\"name\":\"verifyingContract\",\"type\":\"address\"}]},\"domain\":{\"name\":\"Seaport\",\"version\":\"1.1\",\"chainId\":80001,\"verifyingContract\":\"0x00000000006c3852cbef3e08e8df289169ede581\"},\"primaryType\":\"OrderComponents\",\"message\":{\"offerer\":\"0x6fc702d32e6cb268f7dc68766e6b0fe94520499d\",\"zone\":\"0x0000000000000000000000000000000000000000\",\"offer\":[{\"itemType\":\"2\",\"token\":\"0xd15b1210187f313ab692013a2544cb8b394e2291\",\"identifierOrCriteria\":\"33\",\"startAmount\":\"1\",\"endAmount\":\"1\"}],\"consideration\":[{\"itemType\":\"0\",\"token\":\"0x0000000000000000000000000000000000000000\",\"identifierOrCriteria\":\"0\",\"startAmount\":\"9750000000000000\",\"endAmount\":\"9750000000000000\",\"recipient\":\"0x6fc702d32e6cb268f7dc68766e6b0fe94520499d\"},{\"itemType\":\"0\",\"token\":\"0x0000000000000000000000000000000000000000\",\"identifierOrCriteria\":\"0\",\"startAmount\":\"250000000000000\",\"endAmount\":\"250000000000000\",\"recipient\":\"0x66682e752d592cbb2f5a1b49dd1c700c9d6bfb32\"}],\"orderType\":\"0\",\"startTime\":\"1669188008\",\"endTime\":\"115792089237316195423570985008687907853269984665640564039457584007913129639935\",\"zoneHash\":\"0x3000000000000000000000000000000000000000000000000000000000000000\",\"salt\":\"48774942683212973027050485287938321229825134327779899253702941089107382707469\",\"conduitKey\":\"0x0000000000000000000000000000000000000000000000000000000000000000\",\"counter\":\"0\"}}"
        binding.personalSignMessage.setText("Hello World")
        val message = StreamUtils.getRawString(resources, R.raw.typed_data)
        binding.signTypedDataMessage.setText(message)
        binding.apply {
            nativeReceiver.setText(TestAccount.getReceiverAddress())
            erc20Receiver.setText(TestAccount.getReceiverAddress())
            approveERC20Receiver.setText(TestAccount.getReceiverAddress())
            erc721Receiver.setText(TestAccount.getReceiverAddress())
            erc1155Receiver.setText(TestAccount.getReceiverAddress())
        }
    }

}