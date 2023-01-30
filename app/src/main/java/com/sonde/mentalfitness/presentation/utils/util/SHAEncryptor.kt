package com.sonde.mentalfitness.presentation.utils.util

import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

@Throws(NoSuchAlgorithmException::class)
fun getSHA(input: String): String? {
    val md = MessageDigest.getInstance("SHA-256")
    val messageDigest = md.digest(input.toByteArray())
    return bytesToHex(messageDigest)
}

private fun bytesToHex(hashInBytes: ByteArray): String? {
    val sb = StringBuilder()
    for (b in hashInBytes) {
        sb.append(String.format("%02x", b))
    }
    return sb.toString()
}
