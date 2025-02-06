package core.ethereum

import com.plznoanr.web3wallet.core.ethereum.EthereumWallet
import core.common.printLog
import org.junit.Test
import org.web3j.crypto.Credentials
import java.nio.charset.StandardCharsets

class WalletTest {

    @Test
    fun createWalletTest() {
        val ethereumWallet = EthereumWallet()
        val mnemonic = ethereumWallet.createMnemonic()
        printLog("mnemonic:", mnemonic)
    }

    @Test
    fun generateEthAddressTest() {
        val ethereumWallet = EthereumWallet()
        val mnemonic = ethereumWallet.createMnemonic().also {
            printLog("mnemonic:", it)
        }
        check(mnemonic != null)

        val address = ethereumWallet.getAddress(mnemonic, 0) ?: "address is null"
        printLog("addressTest:", address)
    }

    @Test
    fun ethAddressCorrectTest() {
        // From Mnemonic Generator (https://iancoleman.io/bip39/)
        val seedPhrase = "shrimp divorce afford decide north label copper limb wheel awake inmate parrot"
        val expectedAddress = "0x02F02C9AF392f9744d78107279BE615Aa5293e74"

        val ethereumWallet = EthereumWallet()
        val mnemonic = seedPhrase.split(" ")
        println("mnemonic: $mnemonic")

        val address = ethereumWallet.getAddress(mnemonic, 0)
        println("address: $address")

        assert(address == expectedAddress)
    }

    @Test
    fun ethAddressValidationTest() {
        val address = "0x710Bec65d520bE3607c2220534C269446c1DA730"
        assert(EthereumWallet.checkAddressValidation(address))
        println("validationTest: success")
    }

    @Test
    fun verifyEthAddressOwner() {
        val message = "Hello World"

        // Client Part
        val ethereumWallet = EthereumWallet()
        val mnemonic = requireNotNull(ethereumWallet.createMnemonic())
        val credentials: Credentials? = ethereumWallet.getCredentials(mnemonic, 0)
        requireNotNull(credentials) { "Credentials should not be null" }

        val address = credentials.address
        println("address: $address")

        val signMessage = ethereumWallet.ethSign(credentials.ecKeyPair, message.toByteArray(
            StandardCharsets.UTF_8).joinToString("") { "%02x".format(it) })
        println("signMessage Hex: $signMessage")

        // Server Part
        val result = EthereumWallet.verifyAddressOwner(address, signMessage, message)
        println("verifyAddressOwner: $result")

        assert(result)
    }

    @Test
    fun xpubKeyTest() {
        // From Mnemonic Generator (https://iancoleman.io/bip39/)
        val seedPhrase = "shrimp divorce afford decide north label copper limb wheel awake inmate parrot"

        val ethereumWallet = EthereumWallet()
        val mnemonic = seedPhrase.split(" ")
        println("pub: ${ethereumWallet.getMasterPubkey(mnemonic, 60, 0, true)}")
    }

}