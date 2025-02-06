package com.plznoanr.web3wallet.core.ethereum

import com.plznoanr.web3wallet.core.base.BaseWallet
import org.bitcoinj.crypto.DeterministicKey
import org.web3j.crypto.Credentials
import org.web3j.crypto.ECDSASignature
import org.web3j.crypto.ECKeyPair
import org.web3j.crypto.Hash
import org.web3j.crypto.Keys
import org.web3j.crypto.Sign
import org.web3j.crypto.StructuredDataEncoder
import org.web3j.utils.Numeric
import java.math.BigInteger
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.util.Locale
import java.util.regex.Pattern

/**
 * 니모닉 생성
 * 주소 생성
 * 서명
 */
class EthereumWallet : BaseWallet() {

    override fun getAddress(mnemonicList: List<String>, addressIndex: Int): String? {
        val credentials = getCredentials(mnemonicList, addressIndex)
        return credentials?.let { changeToCheckedAddress(it.address) }
    }

    companion object {

        private fun getEthereumMessageHash(message: ByteArray): ByteArray {
            val prefixMessage = "\u0019Ethereum Signed Message:\n"
            val prefix = prefixMessage.plus(message.size.toString()).toByteArray()
            val result = ByteArray(prefix.size + message.size)
            System.arraycopy(prefix, 0, result, 0, prefix.size)
            System.arraycopy(message, 0, result, prefix.size, message.size)
            return Hash.sha3(result)
        }


        fun checkAddressValidation(address: String): Boolean {
            if (!Numeric.containsHexPrefix(address) || address.length != 42) {
                return false
            }
            val pattern = Pattern.compile("^[0-9a-zA-Z]*$")
            return pattern.matcher(address).matches()
        }

        fun verifyAddressOwner(address: String, signMessage: String?, message: String): Boolean {
            signMessage?.let {
                val signedMessage = Numeric.hexStringToByteArray(signMessage)
                if (signedMessage.size == 65) {
                    val messageHash =
                        getEthereumMessageHash(message.toByteArray(StandardCharsets.UTF_8))

                    val r = signedMessage.copyOfRange(0, 32)
                    val s = signedMessage.copyOfRange(32, 64)
                    val v = signedMessage.copyOfRange(64, 65)

                    val signatureData = Sign.SignatureData(v, r, s)
                    val ecdsaSignature = ECDSASignature(
                        BigInteger(1, signatureData.r),
                        BigInteger(1, signatureData.s)
                    )

                    for (i in 0 until 4) {
                        val publicKey = Sign.recoverFromSignature(i, ecdsaSignature, messageHash)
                        if (publicKey != null) {
                            val recoveredAddress = "0x" + Keys.getAddress(publicKey)
                            if (address.equals(recoveredAddress, ignoreCase = true)) {
                                return true
                            }
                        }
                    }
                }
            }
            return false
        }
    }

    fun getCredentials(mnemonicList: List<String>, addressIndex: Int): Credentials? {
        val accountNo = 0
        val coinIndex = 60
        val externalYn = true

        if (checkMnemonicValidation(mnemonicList)) {
            val deterministicKey = getBip44DeterministicKey(
                mnemonicList,
                coinIndex,
                accountNo,
                externalYn,
                addressIndex
            )
            deterministicKey?.let {
                return createCredentials(it)
            }
        }

        return null
    }

    fun ethSign(ecKeyPair: ECKeyPair, message: String): String {
        val decodeHex = Numeric.hexStringToByteArray(message)
        val decodedMessage = String(decodeHex)

        val signatureData = if (decodedMessage.contains("Ethereum Signed Message")) {
            Sign.signMessage(Numeric.hexStringToByteArray(message), ecKeyPair, true)
        } else {
            Sign.signPrefixedMessage(Numeric.hexStringToByteArray(message), ecKeyPair)
        }
        return Numeric.toHexString(getSignatureDataToByte(signatureData))
    }

    fun ethSignPersonalWithPrefix(ecKeyPair: ECKeyPair, message: String): String {
        val signatureData =
            Sign.signPrefixedMessage(Numeric.hexStringToByteArray(message), ecKeyPair)
        return Numeric.toHexString(getSignatureDataToByte(signatureData))
    }

    fun ethSignWithoutPrefix(ecKeyPair: ECKeyPair, message: String): String {
        val signatureData = Sign.signMessage(Numeric.hexStringToByteArray(message), ecKeyPair, true)
        return Numeric.toHexString(getSignatureDataToByte(signatureData))
    }

    fun signEip712StructuredData(ecKeyPair: ECKeyPair, message: String): String {
        val dataEncoder = StructuredDataEncoder(message)
        val hashStructuredData = dataEncoder.hashStructuredData()
        val sig = messageSign(ecKeyPair, hashStructuredData, false)
        return Numeric.toHexString(sig)
    }

    fun hashEip712StructuredData(message: String): String {
        val dataEncoder = StructuredDataEncoder(message)
        val hashStructuredData = dataEncoder.hashStructuredData()
        return Numeric.toHexString(hashStructuredData)
    }

    private fun messageSign(
        ecKeyPair: ECKeyPair,
        label: ByteArray,
        needToHash: Boolean
    ): ByteArray {
        val buffer = ByteBuffer.allocate(label.size)
        buffer.put(label)
        val array = buffer.array()
        val signature = Sign.signMessage(array, ecKeyPair, needToHash)
        return getSignatureDataToByte(signature)
    }

    private fun getSignatureDataToByte(signature: Sign.SignatureData): ByteArray {
        val sigBuffer = ByteBuffer.allocate(signature.r.size + signature.s.size + 1)
        sigBuffer.put(signature.r)
        sigBuffer.put(signature.s)
        sigBuffer.put(signature.v)
        return sigBuffer.array()
    }


    private fun changeToCheckedAddress(address: String): String? {
        return try {
            val cleanAddress = Numeric.cleanHexPrefix(address).lowercase(Locale.getDefault())
            val cs = cleanAddress.toCharArray()
            val keccak = Hash.sha3String(cleanAddress).substring(2).toCharArray()

            val result = StringBuilder()
            for (i in cs.indices) {
                val c = if (Character.digit(
                        keccak[i],
                        16
                    ) > 7
                ) cs[i].uppercaseChar() else cs[i].lowercaseChar()
                result.append(c)
            }
            Numeric.prependHexPrefix(result.toString())
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun createCredentials(deterministicKey: DeterministicKey): Credentials {
        val ecKeyPair = ECKeyPair.create(deterministicKey.privKey)
        return Credentials.create(ecKeyPair)
    }
}