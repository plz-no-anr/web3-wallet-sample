package com.plznoanr.web3wallet.core.crypto

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.NoSuchProviderException
import java.security.SecureRandom
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.InvalidKeySpecException
import java.security.spec.MGF1ParameterSpec
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.Base64
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.OAEPParameterSpec
import javax.crypto.spec.PSource
import javax.crypto.spec.SecretKeySpec

class WalletCryptoManager {

    val ivKey: ByteArray
        get() = "".toByteArray() // todo

    val crtKey: ByteArray
        get() = "".toByteArray() // todo

    // AES/CBC/256 Encryption
    @Throws(
        IOException::class,
        NoSuchAlgorithmException::class,
        NoSuchPaddingException::class,
        InvalidKeyException::class,
        InvalidAlgorithmParameterException::class,
        IllegalBlockSizeException::class,
        BadPaddingException::class
    )
    fun encryptAesCbc(
        plainText: String,
        key: ByteArray,
        iv: ByteArray
    ): ByteArray {
        val textBytes = plainText.toByteArray(StandardCharsets.UTF_8)
        val ivSpec = IvParameterSpec(iv)
        val newKey = SecretKeySpec(key, "AES")
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding") // PKCS5Padding == PKCS7Padding
        cipher.init(Cipher.ENCRYPT_MODE, newKey, ivSpec)

        ByteArrayOutputStream().use { byteOutputStream ->
            CipherOutputStream(byteOutputStream, cipher).use { cipherOutputStream ->
                cipherOutputStream.write(textBytes)
                cipherOutputStream.flush()
            }
            return byteOutputStream.toByteArray()
        }
    }

    // AES/CBC/256 Decryption
    @Throws(
        IOException::class,
        NoSuchAlgorithmException::class,
        NoSuchPaddingException::class,
        InvalidKeyException::class,
        InvalidAlgorithmParameterException::class,
        IllegalBlockSizeException::class,
        BadPaddingException::class
    )
    fun decryptAesCbc(
        textBytes: ByteArray,
        key: ByteArray,
        iv: ByteArray
    ): ByteArray {
        val ivSpec = IvParameterSpec(iv)
        val newKey = SecretKeySpec(key, "AES")
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding").apply {
            init(Cipher.DECRYPT_MODE, newKey, ivSpec)
        }

        ByteArrayInputStream(textBytes).use { byteInputStream ->
            ByteArrayOutputStream().use { byteOutputStream ->
                CipherInputStream(byteInputStream, cipher).use { cipherInputStream ->
                    val buffer = ByteArray(1024)
                    var numberOfByteRead: Int
                    while (cipherInputStream.read(buffer).also { numberOfByteRead = it } >= 0) {
                        byteOutputStream.write(buffer, 0, numberOfByteRead)
                    }
                }
                return byteOutputStream.toByteArray()
            }
        }
    }

    private fun encode(textBytes: ByteArray): String = Base64.getEncoder().encodeToString(textBytes)

    private fun decode(text: String): ByteArray = Base64.getDecoder().decode(text)

    // AES256 Encryption
    @Throws(
        IOException::class,
        NoSuchAlgorithmException::class,
        NoSuchPaddingException::class,
        InvalidKeyException::class,
        InvalidAlgorithmParameterException::class,
        IllegalBlockSizeException::class,
        BadPaddingException::class
    )
    fun encryptAes(plainText: String, key: ByteArray, iv: ByteArray): String {
        return encode(encryptAesCbc(plainText, key, iv))
    }

    // AES256 Decryption
    @Throws(
        IOException::class,
        NoSuchAlgorithmException::class,
        NoSuchPaddingException::class,
        InvalidKeyException::class,
        InvalidAlgorithmParameterException::class,
        IllegalBlockSizeException::class,
        BadPaddingException::class
    )
    fun decryptAes(encryptedText: String, key: ByteArray, iv: ByteArray): String {
        return String(decryptAesCbc(decode(encryptedText), key, iv))
    }

    @Throws(NoSuchAlgorithmException::class, NoSuchProviderException::class)
    fun generateRsaKeyPair(keySize: Int): Map<String, String> {
        val secureRandom = SecureRandom()
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")

        keyPairGenerator.initialize(keySize, secureRandom)
        val keyPair = keyPairGenerator.genKeyPair()

        val publicKey = keyPair.public
        val privateKey = keyPair.private

        return HashMap<String, String>().apply {
            put("publicKey", encode(publicKey.encoded))
            put("privateKey", encode(privateKey.encoded))
        }
    }

    @Throws(
        InvalidAlgorithmParameterException::class,
        NoSuchPaddingException::class,
        IllegalBlockSizeException::class,
        NoSuchAlgorithmException::class,
        InvalidKeySpecException::class,
        BadPaddingException::class,
        IOException::class,
        InvalidKeyException::class
    )
    fun encryptRsa(plainText: String, encodedPubKey: String): String {
        return encode(encryptRsaEcb(plainText, encodedPubKey))
    }

    @Throws(
        InvalidAlgorithmParameterException::class,
        NoSuchPaddingException::class,
        IllegalBlockSizeException::class,
        NoSuchAlgorithmException::class,
        InvalidKeySpecException::class,
        BadPaddingException::class,
        IOException::class,
        InvalidKeyException::class
    )
    fun decryptRsa(encryptedText: String, encodedPrivKey: String): String {
        return String(decryptRsaEcb(encryptedText, encodedPrivKey))
    }

    @Throws(
        NoSuchAlgorithmException::class,
        InvalidKeySpecException::class,
        NoSuchPaddingException::class,
        InvalidKeyException::class,
        BadPaddingException::class,
        IllegalBlockSizeException::class,
        InvalidAlgorithmParameterException::class,
        IOException::class
    )
    fun encryptRsaEcb(plainText: String, encodedPubKey: String): ByteArray {
        val textBytes = plainText.toByteArray(StandardCharsets.UTF_8)
        val keyFactory = KeyFactory.getInstance("RSA")
        val bytePublicKey = decode(encodedPubKey)

        val publicKeySpec = X509EncodedKeySpec(bytePublicKey)
        val publicKey = keyFactory.generatePublic(publicKeySpec) as RSAPublicKey

        val cipher = Cipher.getInstance("RSA/ECB/OAEPwithSHA-256andMGF1Padding")
        val oaepParameterSpec = OAEPParameterSpec(
            "SHA-256",
            "MGF1",
            MGF1ParameterSpec.SHA256,
            PSource.PSpecified.DEFAULT
        )
        cipher.init(Cipher.ENCRYPT_MODE, publicKey, oaepParameterSpec)

        ByteArrayOutputStream().use { byteOutputStream ->
            CipherOutputStream(byteOutputStream, cipher).use { cipherOutputStream ->
                cipherOutputStream.write(textBytes)
                cipherOutputStream.flush()
            }
            return byteOutputStream.toByteArray()
        }
    }

    @Throws(
        NoSuchAlgorithmException::class,
        InvalidKeySpecException::class,
        NoSuchPaddingException::class,
        InvalidKeyException::class,
        BadPaddingException::class,
        IllegalBlockSizeException::class,
        InvalidAlgorithmParameterException::class,
        IOException::class
    )
    fun decryptRsaEcb(base64EncryptedText: String, base64PrivKey: String): ByteArray {
        val keyFactory = KeyFactory.getInstance("RSA")
        val textBytes = decode(base64EncryptedText)
        val bytePrivateKey = decode(base64PrivKey)

        val privKeySpec = PKCS8EncodedKeySpec(bytePrivateKey)
        val privateKey = keyFactory.generatePrivate(privKeySpec) as RSAPrivateKey

        val cipher = Cipher.getInstance("RSA/ECB/OAEPwithSHA-256andMGF1Padding")
        val oaepParameterSpec = OAEPParameterSpec(
            "SHA-256",
            "MGF1",
            MGF1ParameterSpec.SHA256,
            PSource.PSpecified.DEFAULT
        )
        cipher.init(Cipher.DECRYPT_MODE, privateKey, oaepParameterSpec)

        ByteArrayInputStream(textBytes).use { byteInputStream ->
            ByteArrayOutputStream().use { byteOutputStream ->
                CipherInputStream(byteInputStream, cipher).use { cipherInputStream ->
                    val buffer = ByteArray(1024)
                    var numberOfByteRead: Int
                    while (cipherInputStream.read(buffer).also { numberOfByteRead = it } >= 0) {
                        byteOutputStream.write(buffer, 0, numberOfByteRead)
                    }
                }
                return byteOutputStream.toByteArray()
            }
        }
    }

    @Throws(NoSuchAlgorithmException::class)
    fun getDuplicatedSha256(msg: String, count: Int): ByteArray {
        var key = msg.toByteArray()
        repeat(count) {
            key = MessageDigest.getInstance("SHA-256").digest(key)
        }
        return key
    }

}
