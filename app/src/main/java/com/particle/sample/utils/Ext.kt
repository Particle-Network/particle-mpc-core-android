package com.particle.sample.utils

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.afollestad.materialdialogs.MaterialDialog
import com.particle.base.ParticleNetwork
import com.particle.base.isTron
import com.particle.base.utils.toTronBase58
import java.math.BigDecimal
import java.math.BigInteger
import java.util.regex.Pattern
import kotlin.math.pow

/**
 * Created by chaichuanfa on 2023/6/9
 */

fun Fragment.toast(message: String) {
    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
}

fun AppCompatActivity.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun Fragment.showMsg(title: String, message: String) {
    MaterialDialog(this.requireContext()).show {
        title(text = title)
        message(text = message)
        positiveButton(text = "ok")
    }
}

fun String.hex2Decimal(): BigInteger {
    return this.substring(2, this.length).toBigInteger(16)
}

fun BigInteger.toUIAmount(decimals: Int): BigDecimal {
    val mi = 10.0.pow(decimals.toDouble())
    return this.toBigDecimal().divide(BigDecimal.valueOf(mi))
}

fun String.hexAmount2UiAmount(): String {
    return this.hex2Decimal().toUIAmount(ParticleNetwork.chainInfo.nativeCurrency.decimals).toPlainString().dropLastWhile { it == '0' }
        .dropLastWhile { it == '.' }
}

fun String.uiAmount2HexAmount(): String {
    val result = this.toBigDecimal().multiply(BigDecimal.valueOf(10.0.pow(ParticleNetwork.chainInfo.nativeCurrency.decimals)))
    val hex = result.toBigInteger().toString(16)
    return "0x$hex"
}

fun String.uiAmount2Amount(): BigInteger {
    val result = this.toBigDecimal().multiply(BigDecimal.valueOf(10.0.pow(ParticleNetwork.chainInfo.nativeCurrency.decimals)))
    return result.toBigInteger()
}

fun String.formatDisplayAddress(formatCount: Int = 5): String {
    var currAddress = this
    if (currAddress.length < formatCount) return currAddress
    if (ParticleNetwork.chainInfo.isTron()) {
        try {
            currAddress = toTronBase58()
        } catch (_: Exception) {

        }
    }
    val startStr = currAddress.substring(0, formatCount)
    val endStr = currAddress.substring(currAddress.length - formatCount, currAddress.length)
    return "$startStr...$endStr"
}

private var pattern = "^[1-9A-HJ-NP-Za-km-z]{32,44}\$"
internal fun String.isSolPublicKey(): Boolean {
    val isMatch = Pattern.matches(pattern, this);
    return isMatch
}
