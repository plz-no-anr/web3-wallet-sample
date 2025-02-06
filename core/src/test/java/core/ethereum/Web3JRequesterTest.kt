package core.ethereum

import com.plznoanr.web3wallet.core.ethereum.EthereumWallet
import com.plznoanr.web3wallet.core.ethereum.Web3jRequester
import core.base.BaseWeb3Test
import core.common.printLog
import org.junit.Test
import org.web3j.crypto.Credentials
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.utils.Convert
import java.math.BigDecimal
import java.math.BigInteger

class Web3JRequesterTest: BaseWeb3Test() {

    @Test
    fun walletBalanceTest() {
        val ethereumWallet = EthereumWallet()
        val mnemonic = testMnemonic.split(" ")
        val addressIndex0 = requireNotNull(ethereumWallet.getAddress(mnemonic, 0)).also {
            printLog("addressIndex 0:", it)
        }

        val web3JRequester = Web3jRequester(web3j)
        val balance = web3JRequester.getBalance(addressIndex0, DefaultBlockParameterName.LATEST)

        printLog("balance:", balance)
    }

    // Success https://bscscan.com/tx/0x8daa31699390ca2c08bcec8381096f1009f3a94c88445f540fa3dd9afa2fe769
    @Test
    fun legacyCoinSendTransactionTest() {
        val credentials: Credentials? = ethereumWallet.getCredentials(mnemonic, 0)
        val addressIndex1 = requireNotNull(ethereumWallet.getAddress(mnemonic, 1)).also {
            printLog("addressIndex1:", it)
        }

        credentials?.let {
            val nonce = requireNotNull(web3JRequester.getNonce(it.address, DefaultBlockParameterName.LATEST)).also { nonce ->
                printLog("nonce:", nonce)
            }

            var gasPrice = web3JRequester.getGasPrice()
            require(gasPrice != null) { "Gas price is null" }
            gasPrice = Convert.toWei(BigDecimal(gasPrice), Convert.Unit.GWEI).toBigInteger()
            println("gasPrice: $gasPrice")

            val estimateGas = requireNotNull(web3JRequester.getEstimateGas(it.address, addressIndex1, BigInteger.ONE, "")).also { estimateGas ->
                printLog("estimateGas:", estimateGas)
            }

            val txHash = web3JRequester.sendLegacyTransaction(
                it,
                bscTestnetId,
                nonce,
                gasPrice,
                estimateGas,
                addressIndex1,
                BigInteger.ONE,
                ""
            )

            printLog("txHash:", txHash)
            require(txHash != null) { "Transaction hash is null" }
        }
    }

}