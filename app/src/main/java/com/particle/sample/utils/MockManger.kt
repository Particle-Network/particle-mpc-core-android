package com.particle.sample.utils

import com.particle.base.ParticleNetwork
import com.particle.base.isTron
import com.particle.base.model.EIP1559TransactionData
import com.particle.base.model.EVMTransaction
import com.particle.base.model.LegacyTransactionData
import com.particle.base.utils.toTronHex

/**
 * Created by chaichuanfa on 2022/7/26
 */
object MockManger {
    suspend fun mockCreateTransaction(from: String, to: String, value: String, data: String = "0x"): EVMTransaction {
        var toAddress =  to
        if (ParticleNetwork.chainInfo.isTron()) {
            toAddress = to.toTronHex()!!
        }
        return if (ParticleNetwork.chainInfo.isEIP1559Supported()) {
            EIP1559TransactionData(
                chainId = "0x${ParticleNetwork.chainId.toString(16)}",
                from = from,
                to = toAddress,
                value = value,
                gasLimit = if (data == "0x") "0x${Integer.toHexString(25000)}" else "0x${Integer.toHexString(55000)}",
                maxFeePerGas = "0x9502f90e",
                maxPriorityFeePerGas = "0x9502F900",
                data = data,
                nonce = "0x0"
            )
        } else {
            LegacyTransactionData(
                chainId = "0x${ParticleNetwork.chainId.toString(16)}",
                from = from,
                to = toAddress,
                value = value,
                data = data,
                nonce = "0x0",
                gasPrice = "0x25cfcb580",
                gasLimit = if(data == "0x") "0x${Integer.toHexString(25000)}" else "0x${Integer.toHexString(55000)}",
                type = "0x0",
                action = "normal",
                gasLevel = "medium",
            )
        }
    }
}
