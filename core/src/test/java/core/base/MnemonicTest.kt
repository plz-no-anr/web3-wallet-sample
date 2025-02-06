package core.base

import com.plznoanr.web3wallet.core.ethereum.EthereumWallet
import core.common.printLog
import org.junit.Test

class MnemonicTest {

    @Test
    fun mnemonic_creation_test() {
        val ethereumWallet = EthereumWallet()
        val seedList = ethereumWallet.createMnemonic()

        seedList.also {
            printLog("size ->", seedList?.size)
        }?.forEach {
            printLog("word ->", it)
        }
    }

}