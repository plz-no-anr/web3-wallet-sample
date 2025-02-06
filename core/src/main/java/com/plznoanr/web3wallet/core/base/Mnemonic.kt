package com.plznoanr.web3wallet.core.base

import org.bitcoinj.crypto.MnemonicCode
import org.bitcoinj.crypto.MnemonicException
import java.io.IOException
import java.security.SecureRandom

class Mnemonic {

    companion object {
        const val BIP39_ENTROPY_LEN_128: Int = 16
        const val BIP39_ENTROPY_LEN_160: Int = 20
        const val BIP39_ENTROPY_LEN_192: Int = 24
        const val BIP39_ENTROPY_LEN_224: Int = 28
        const val BIP39_ENTROPY_LEN_256: Int = 32
    }

    fun generateMnemonic(entropyLength: Int): List<String>? {
        val seed = ByteArray(entropyLength)
        val sr = SecureRandom()
        sr.nextBytes(seed)

        return try {
            val mc = MnemonicCode()
            mc.toMnemonic(seed)
        } catch (e: MnemonicException) {
            e.printStackTrace()
            null
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    fun getSeedFromMnemonic(mnemonic: List<String>): ByteArray? = try {
        MnemonicCode.toSeed(mnemonic, "")
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }

    fun checkMnemonicValidation(mnemonic: List<String>): Boolean {
        try {
            val mc = MnemonicCode()
            mc.check(mnemonic)
        } catch (e: MnemonicException) {
            e.printStackTrace()
            return false
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }
        return true
    }

}