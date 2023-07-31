package com.particle.sample.utils

import com.particle.api.logic.SolanaRpcRepository
import com.particle.auth.AuthCore
import com.particle.auth.data.req.JsonRpcRequest
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.core.Transaction
import org.p2p.solanaj.core.TransactionInstruction
import org.p2p.solanaj.kits.TokenTransaction
import org.p2p.solanaj.model.types.ConfigObjects
import org.p2p.solanaj.programs.SystemProgram
import org.p2p.solanaj.programs.TokenProgram
import java.math.BigDecimal
import java.math.BigInteger


object SolanaTransactionManager {
    suspend fun buildSerializedNativeTransaction(from: String, to: String, amount: BigDecimal): String? {
        val map = mapOf("sender" to from, "receiver" to to, "lamports" to amount.toBigInteger())
        val resp = AuthCore.solana.request(
            JsonRpcRequest(
                "enhancedSerializeTransaction",
                arrayListOf("transfer-sol", map)
            )
        )
        val serialized = resp.result!!.asJsonObject.getAsJsonObject("transaction")["serialized"].asString
        return serialized
    }

    suspend fun buildSerializedSplTransaction(from: String, to: String, mint: String, amount: BigDecimal): String? {
        val map = mapOf("sender" to from, "receiver" to to, "mint" to mint, "amount" to amount.toBigInteger())
        val resp = AuthCore.solana.request(
            JsonRpcRequest(
                "enhancedSerializeTransaction",
                arrayListOf("transfer-token", map)
            )
        )
        val serialized = resp.result!!.asJsonObject.getAsJsonObject("transaction")["serialized"].asString
        return serialized
    }

    suspend fun transferNativeToken(
        fromAddress: String,
        destinationAddress: String,
        lamports: BigInteger,
        recentBlockhash: String? = null,
        feePayerPublicKey: String? = null,
    ): Transaction {
        if (fromAddress == destinationAddress) {
            error("You can not send tokens to yourself")
        }

        val transaction = Transaction()
        transaction.addInstruction(
            SystemProgram.transfer(
                PublicKey(fromAddress),
                PublicKey(destinationAddress),
                lamports
            )
        )
        transaction.feePayer = PublicKey(feePayerPublicKey ?: fromAddress)
        val response = AuthCore.solana.request(JsonRpcRequest("getRecentBlockhash", listOf(ConfigObjects.Commitment("finalized"))))
        val getRecentBlockhash = response.result?.asJsonObject?.getAsJsonObject("value")?.get("blockhash")?.asString
        transaction.recentBlockhash = recentBlockhash ?: getRecentBlockhash
        return transaction
    }

    suspend fun transferSplToken(
        fromAddress: String,
        destinationAddress: String,
        mintAddress: String,
        lamports: BigInteger,
        recentBlockhash: String? = null,
        feePayerAddress: String? = null,
    ): Transaction {
        val transaction = Transaction()
        val destinationPublicKey = PublicKey(destinationAddress)
        val feePayer = PublicKey(feePayerAddress ?: fromAddress)
        val senderPublicKey = PublicKey(fromAddress)

        val senderAta = TokenTransaction.getAssociatedTokenAddress(
            PublicKey(mintAddress),
            senderPublicKey
        )

        val splDestinationAddress = SolanaRpcRepository.findSplTokenAddressData(
            destinationAddress = destinationPublicKey,
            mintAddress = mintAddress
        )
        // get address
        val toPublicKey = splDestinationAddress.associatedAddress
        val instructions = mutableListOf<TransactionInstruction>()

        // create associated token address
        if (splDestinationAddress.shouldCreateAssociatedInstruction) {
            val createAccount = TokenProgram.createAssociatedTokenAccountInstruction(
                TokenProgram.ASSOCIATED_TOKEN_PROGRAM_ID,
                TokenProgram.PROGRAM_ID,
                PublicKey(mintAddress),
                toPublicKey,
                destinationPublicKey,
                feePayer
            )

            instructions += createAccount
        }

        instructions += TokenProgram.transferInstruction(
            TokenProgram.PROGRAM_ID,
            senderAta,
            toPublicKey,
            senderPublicKey,
            lamports
        )

        transaction.addInstruction(*instructions.toTypedArray())
        transaction.recentBlockhash = recentBlockhash ?: SolanaRpcRepository.getRecentBlockhash()
        transaction.feePayer = feePayer

        return transaction
    }
}