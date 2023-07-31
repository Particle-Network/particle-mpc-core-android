package com.particle.sample.utils

import com.particle.base.ParticleNetwork
import com.particle.base.isTron

object TestAccount {
    fun getReceiverAddress(): String {
        if (ParticleNetwork.isEvmChain()) {
            if (ParticleNetwork.chainInfo.isTron()) {
                return "TS6cq695vin8g7go7xm9zkGz7hJC8Puo8z"
            } else {
                return "0xB0e8d0a26920DF63AD2be1b21269b798846183d6"
            }
        } else {
            return "EVtyrThKGyHofRZAaGxS7oys4L4yudb6rXUcJwyqSW4M"
        }
    }
}