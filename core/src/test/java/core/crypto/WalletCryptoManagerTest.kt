package core.crypto

import com.plznoanr.web3wallet.core.crypto.WalletCryptoManager
import com.plznoanr.web3wallet.core.ethereum.EthereumWallet
import core.common.printLog
import core.mock.service.domain.UserWalletKey
import org.bouncycastle.util.encoders.Hex
import org.junit.Test
import org.web3j.crypto.Credentials
import java.security.NoSuchAlgorithmException

class WalletCryptoManagerTest {

    private val userMnemonic =
        "wine vast involve foam smooth grid drill grid lock chair top ginger";

    private val userPassword = "Abc1234!@#$"
    private val userAnswer = "answer1|answer2"

    // AES 지갑 니모닉 암호화 테스트
    @Test
    fun encrypt_aes_test() {
        val walletCryptoManager = WalletCryptoManager()
        try {
            val encryptedText: String = walletCryptoManager.encryptAes(
                userMnemonic,
                walletCryptoManager.crtKey,
                walletCryptoManager.ivKey
            )

            printLog("encryptAesTest", encryptedText)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // AES 지갑 니모닉 암호화 테스트
    @Test
    fun decrypt_aes_test() {
        val encryptedText =
            "YXVecFfcXOoBCnitqy8ckpIa2jbPfWCp3bCBSwS929IV50U6Kgw0FUxwjPEhkZ74PZj7Eq+TZQODNkikvWRJByizJv8/UX+M4uVMh3upi6Q="

        val walletCryptoManager = WalletCryptoManager()
        try {
            val plainText: String = walletCryptoManager.decryptAes(
                encryptedText,
                walletCryptoManager.crtKey,
                walletCryptoManager.ivKey
            )

            printLog("decryptAesTest", plainText)

            assert(userMnemonic == plainText)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Test
    @Throws(NoSuchAlgorithmException::class)
    fun check_duplicated_sha256_test() {
        val userPasswordHashHex = "a1d5a126f6043aef90d529ef37afe5cd6ec91dabf429a3a10d4127b3789db938"

        val walletCryptoManager = WalletCryptoManager()
        val userPasswordResultFromDuplicatedSha =
            Hex.toHexString(walletCryptoManager.getDuplicatedSha256(userPassword, 1))

        printLog("check sha256 function", userPasswordResultFromDuplicatedSha)
        printLog("target sha256 text : ", userPasswordHashHex)

        assert(userPasswordHashHex == userPasswordResultFromDuplicatedSha)
    }

    @Test
    @Throws(NoSuchAlgorithmException::class)
    fun check_duplicated_sha256_test2() {
        val userPasswordHashHex = "d99b3a59fd346b15026cba81016bcdf6bdef6a2edc7c13f1659cca7f54c8b81c"

        val walletCryptoManager = WalletCryptoManager()
        val userPasswordResultFromDuplicatedSha =
            Hex.toHexString(walletCryptoManager.getDuplicatedSha256(userPassword, 1))

        printLog("check sha256 function", userPasswordResultFromDuplicatedSha)
        printLog("target sha256 text : ", userPasswordHashHex)

        assert(userPasswordHashHex == userPasswordResultFromDuplicatedSha)
    }

    @Test
    @Throws(NoSuchAlgorithmException::class)
    fun check_duplicated_sha256_with_count_test() {
        val userPasswordHashHex = "a1d5a126f6043aef90d529ef37afe5cd6ec91dabf429a3a10d4127b3789db938"

        val walletCryptoManager = WalletCryptoManager()
        val userPasswordResultFromDuplicatedSha =
            Hex.toHexString(walletCryptoManager.getDuplicatedSha256(userAnswer, 10))

        printLog("check sha256 function", userPasswordResultFromDuplicatedSha)
        printLog("target sha256 text : ", userPasswordHashHex)

        assert(userPasswordHashHex != userPasswordResultFromDuplicatedSha)
    }

    // AES 지갑 니모닉 암호화 테스트
    @Test
    @Throws(NoSuchAlgorithmException::class)
    fun generate_user_wallet_key() {
        val userPasswordKeyType = 1
        val userAnswerKeyType = 2

        val walletCryptoManager = WalletCryptoManager()
        var userPasswordHash: ByteArray? = walletCryptoManager.getDuplicatedSha256(userPassword, 10)
        printLog("userPasswordHash Hex : ", Hex.toHexString(userPasswordHash))

        var userAnswerHash: ByteArray? = walletCryptoManager.getDuplicatedSha256(userAnswer, 10)
        printLog("userAnswerHash Hex : ", Hex.toHexString(userAnswerHash))

        val userWalletKeyType1 = UserWalletKey(
            type = userPasswordKeyType,
            walletKey = Hex.toHexString(userPasswordHash)
        )

        val userWalletKeyType2 = UserWalletKey(
            type = userAnswerKeyType,
            walletKey = Hex.toHexString(userAnswerHash)
        )

        // 사용자 정보 기반 지갑 암호화
        val walletKeyType1 = userWalletKeyType1.walletKey
        userPasswordHash = Hex.decodeStrict(walletKeyType1)

        val walletKeyType2 = userWalletKeyType2.walletKey
        userAnswerHash = Hex.decodeStrict(walletKeyType2)


        var encryptedWalletType1: String? = null
        var encryptedWalletType2: String? = null
        try {
            encryptedWalletType1 = walletCryptoManager.encryptAes(
                userMnemonic,
                userPasswordHash,
                walletCryptoManager.ivKey
            )
            encryptedWalletType2 = walletCryptoManager.encryptAes(
                userMnemonic,
                userAnswerHash,
                walletCryptoManager.ivKey
            )

            printLog("encryptedWalletType1 : ", encryptedWalletType1)
            printLog("encryptedWalletType2 : ", encryptedWalletType2)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 사용자 정보 기반 지갑 복호화
        if (encryptedWalletType1 != null && encryptedWalletType2 != null) {
            val ethereumWallet: EthereumWallet = EthereumWallet()
            try {
                val walletMnemonic1: String = walletCryptoManager.decryptAes(
                    encryptedWalletType1,
                    userPasswordHash,
                    walletCryptoManager.ivKey
                )
                val walletMnemonic2: String = walletCryptoManager.decryptAes(
                    encryptedWalletType2,
                    userAnswerHash,
                    walletCryptoManager.ivKey
                )

                printLog("walletMnemonic1 : ", walletMnemonic1)
                printLog("walletMnemonic2 : ", walletMnemonic2)

                assert(
                    ethereumWallet.checkMnemonicValidation(
                        listOf(
                            *walletMnemonic1.split(" ".toRegex()).dropLastWhile { it.isEmpty() }
                                .toTypedArray())))
                assert(
                    ethereumWallet.checkMnemonicValidation(
                        listOf(
                            *walletMnemonic2.split(" ".toRegex()).dropLastWhile { it.isEmpty() }
                                .toTypedArray())))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            printLog("warning", "encryptedWalletType is null")
        }
    }

    // RSA API Data 암호화 테스트
    @Test
    fun generate_user_rsa_key() {
        val walletCryptoManager = WalletCryptoManager()
        try {
            val publicKeyString = "publicKey"
            val privateKey = "privateKey"

            val keys: Map<String, String> = walletCryptoManager.generateRsaKeyPair(4096)
            if (keys.containsKey(publicKeyString) && keys.containsKey(privateKey)) {
                printLog(publicKeyString, keys[publicKeyString])
                printLog(privateKey, keys[privateKey])
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // RSA 구성요소
    private val publicKey =
        "MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAy22sxB9eYzNuXNJVvrR2GY9A+olkQdwqSo9yATrnlYDKCn3pf83z9DekyeXcsKVaW/g8uiIEgI6yEVTIezwV5aRZet5ya3O+VGWvvdTyLCiYfQ0qSSQKb2w43qtuJUJY4H4HwJRrO/jcA5aOF+eyplL39iJl9zUn5GdHc4rERVc6PZZnph0UlEVyGgbDwDdUPFSF0PQ29rI2hHiPMUd+K2hdF6DM/53ZXNT2fXV8Axmw43QrG3g9wvqls4s2jXudvv/egBw6HUyjYhVe0bulxNnc4zdlbDeM+o/kYgUnZIYamBUTDUPQ6kv+sjLHwOC9x6G4ovGY4RsnAl0YMj8a7NY4HqOo8G2cSqQ5iCeEHw5Go+CCutKLn5I5+GiW5a3Cy+L4Yv04m43HiKsjNzf8vK+YMg/TzyRN04SOe/MGmRGik8uBu1w11jjFp2/jf71g2puT/ttAMAIRwkqeSmBKQ5yVusjmJ77kIR/0Qj1p9fouFK8yMfFIBXkjhOhn/oZGsFJ9nE34opUbO6Zafm4x/93lsrkxnbgyyqnjIcXpnwZs41MUgujVltuJgVCM5zlQJS6MiOUlel3l8KLTyQGBDHmyf0mdbMRymqD3iJmJTPhWRnmyWLKQkScxHP3Bfb30KyTEHkNkaZRnQ/nyx+iovhVcVwlx/xUmLsy8VBKXgRkCAwEAAQ=="
    private val privateKey =
        "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"
    private val encryptedUserMnemonic =
        "IiZay0JlI6lbRrFSl+rEDFeDotv/hJ+C4+q90M8AaS45BKtpFaM+Z47rcVRtMt6XvtRdPwZSjzYiZ3QF3EOdhM1puefG+F7RIupWHaBf+4un73wjWLzHt+C0/1/F4toCXuBPdi/m7AdLE3K9abzXscpkwDlzKq+YlcY+CeaIoiODxcsL2hL1snVTcCUAmSEK5ixvG5DWP8eF4gdGl/oDrPBWe3NSwLTEQT9092NTjYgP0vJ2/JY6fED14Euzi327KW4fkiGOhLl7C0h9gU6uXK5Si7zHzEroKjiC+yRdNTvwK2CGQ9lrZ1EuEVcHYxIJmD56IaqY9rNgECQi14eMn2nkT5ikKjc5bKCJ/3975SyurEtEbjrM7+cfVsnKnV0+xN4QW4rz9/JxNGYvgldDDTbhWFrhpXZEjkx3OJJebDWrFviFJpFmsYc7Lu0duUshzr5LtVJNErzC8OseIsHy4EMZ9sLekOEfbGWwl+v+k1dxMThEIXIdrvKhTRI22cCuLIMVzJZyYu/mDdZt/qvEYhGWDeQ10Jril727Cqm9KLftC25hDea/FpTsh0rXeV1JF8Sd3djfCd8YO0gpaqRuPJ+oXS0XxoXLJ2iUlrtmRykh3NZOTUEiH5nXreG8u82tLVU8AH6vUhfeqYHzR/a86IqPgHMlbIWfSsJ9+6pAl1o="

    @Test
    fun encrypt_rsa_test() {
        val walletCryptoManager = WalletCryptoManager()
        try {
            val encryptedUserMnemonic: String =
                walletCryptoManager.encryptRsa(userMnemonic, publicKey)
            printLog("encryption result : ", encryptedUserMnemonic)
            // 동일한 public key 로 동일한 Plain Data를 암호화 해도, 암호 결과 값은 매번 다르게 나옴.
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

//    @Test
//    fun decrypt_rsa_test(addressIndex: Int, mnmonic: String?) {
//        val walletCryptoManager = WalletCryptoManager()
//        try {
//            val decryptedUsertMnemonic: String =
//                walletCryptoManager.decryptRsa(encryptedUserMnemonic, privateKey)
//            printLog("decryption result : ", decryptedUsertMnemonic)
//
//            assert(decryptedUsertMnemonic == userMnemonic)
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }

//    @Test
//    @Throws(NoSuchAlgorithmException::class)
//    fun decryptAnswerAndChangePassword() {
//        val walletCryptoManager = WalletCryptoManager()
//
//        val passwordType = 1
//        val answerType = 2
//
//        val userWalletKey = getUserWalletKey(answerType)
//        printLog("userWalletKey : ", userWalletKey.toString())
//
//        val encryptAnswerSeed = userWalletKey.encryptWalletSeed
//
//        try {
//            val walletMnemonic: String = walletCryptoManager.decryptAes(
//                encryptAnswerSeed,
//                Hex.decodeStrict(userWalletKey.walletKey),
//                walletCryptoManager.ivKey
//            )
//            printLog("walletMnemonic : ", walletMnemonic)
//
//            val newPassword = "qwer1234"
//            var userPasswordHash: ByteArray? =
//                walletCryptoManager.getDuplicatedSha256(newPassword, 10)
//
//            val userWalletKey1 = UserWalletKey(
//                type = passwordType,
//                walletKey = Hex.toHexString(userPasswordHash)
//            )
//
//            userPasswordHash = Hex.decodeStrict(userWalletKey1.walletKey)
//
//            val encryptWalletSeed: String = walletCryptoManager.encryptAes(
//                walletMnemonic,
//                userPasswordHash,
//                walletCryptoManager.ivKey
//            )
//            userWalletKey1.setEncryptoWalletSeed(encryptWalletSeed)
//
//            printLog("userWalletKey1 : ", userWalletKey1)
//
//            val decryptPasswordMnemonic: String = walletCryptoManager.decryptAes(
//                userWalletKey1.encryptWalletSeed,
//                Hex.decodeStrict(userWalletKey1.walletKey),
//                walletCryptoManager.ivKey
//            )
//            printLog("decryptPasswordMnemonic : ", decryptPasswordMnemonic)
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }

    @Throws(NoSuchAlgorithmException::class)
    private fun getUserWalletKey(type: Int): UserWalletKey {
        val encKey: String = (if (type == 1) userPassword else userAnswer)

        val walletCryptoManager = WalletCryptoManager()
        val userEncryptHash: ByteArray = walletCryptoManager.getDuplicatedSha256(encKey, 10)

        val hexUserWalletKey = Hex.toHexString(userEncryptHash)

        val userEncryptWalletKey = Hex.decodeStrict(hexUserWalletKey)

        try {
            return walletCryptoManager.encryptAes(
                userMnemonic,
                userEncryptWalletKey,
                walletCryptoManager.ivKey
            ).let {
                UserWalletKey(
                    type = type,
                    walletKey = hexUserWalletKey,
                    encryptWalletSeed = it
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return UserWalletKey(
            type = type,
            walletKey = hexUserWalletKey,
        )
    }

    @Test
    @Throws(Exception::class)
    fun eip712_sign_test2() {
        val eip712DataJSON = """{
  "types": {
    "FooBarDomain": [
      {
        "name": "name",
        "type": "string"
      },
      {
        "name": "version",
        "type": "string"
      },
      {
        "name": "chainId",
        "type": "uint256"
      },
      {
        "name": "verifyingContract",
        "type": "address"
      }
    ],
    "Person": [
      {
        "name": "name",
        "type": "string"
      },
      {
        "name": "wallet",
        "type": "address"
      }
    ],
    "Mail": [
      {
        "name": "from",
        "type": "Person[]"
      },
      {
        "name": "to",
        "type": "Person[]"
      },
      {
        "name": "contents",
        "type": "string[3]"
      }
    ]
  },
  "primaryType": "Mail",
  "domain": {
    "name": "Ether Mail",
    "version": "1",
    "chainId": 1,
    "verifyingContract": "0xCcCCccccCCCCcCCCCCCcCcCccCcCCCcCcccccccC"
  },
  "message": {
    "from": [
      {
        "name": "Cow",
        "wallet": "0xCD2a3d9F938E13CD947Ec05AbC7FE734Df8DD826"
      }
    ],
    "to": [
      {
        "name": "Bob",
        "wallet": "0xbBbBBBBbbBBBbbbBbbBbbbbBBbBbbbbBbBbbBBbB"
      },
      {
        "name": "Alice",
        "wallet": "0xdddddddddddddddddddddddddddddddddddddddd"
      },
      {
        "name": "Michael",
        "wallet": "0xeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee"
      }
    ],
    "contents": ["Hello, Bob!", "Hello, Alice!", "Hello, Michael!"]
  }
}
"""
        val seedPhrase = "swarm midnight street story that siege pledge bottom escape glue fly wash"
        val expectedAddress = "0xd687a25af8967a00449C4c387f6dDEF190B14933"

        val mnemonic = listOf(*seedPhrase.split(" ".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray())
        val ethereumWallet = EthereumWallet()

        val credentials: Credentials = checkNotNull(ethereumWallet.getCredentials(mnemonic, 0))
        val address = credentials.address

        val encoded712Message: String = ethereumWallet.hashEip712StructuredData(eip712DataJSON)
        printLog("encoded712Message : ", encoded712Message)

        val signEIP712Message: String =
            ethereumWallet.signEip712StructuredData(credentials.ecKeyPair, eip712DataJSON)
        printLog("signMessage Hex : ", signEIP712Message)

        // Server Part
        val result: Boolean =
            EthereumWallet.verifyAddressOwner(address, signEIP712Message, encoded712Message)
        printLog("verifyAddressOwner", result)

        assert(result)
    }
}
