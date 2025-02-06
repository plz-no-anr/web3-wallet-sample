package core.ethereum

import com.plznoanr.web3wallet.core.ethereum.abi.Erc1155Abi
import core.base.BaseWeb3Test
import core.common.printLog
import org.junit.Test
import java.math.BigInteger

class Erc1155ContractTest : BaseWeb3Test() {

    @Test
    fun erc1155BalanceOfTest() {
        val holderAddress = "0x5029956fd298573d4484329a6400553291456bb2"
        val contractAddress = "0x4cbbe042ff9b63e9741a3eb8442295360f046bcd"
        val erc1155Abi = Erc1155Abi()

        var result = web3JRequester.requestEthCall(
            requireNotNull(ethereumWallet.getAddress(mnemonic, 0)),
            contractAddress,
            erc1155Abi.balanceOf(holderAddress, BigInteger.valueOf(107))
        )

        result?.forEach { type -> printLog("result:", type.value) }

        var index = BigInteger.ZERO
        val limit = BigInteger.valueOf(50)

        while (index <= limit) {
            result = web3JRequester.requestEthCall(
                requireNotNull(ethereumWallet.getAddress(mnemonic, 0)),
                contractAddress,
                erc1155Abi.balanceOf(holderAddress, index)
            )

            result?.forEach { type -> printLog("result[$index]:", type.value) }
                ?: println("result[$index]: result is null")

            index += BigInteger.ONE
        }
    }

    @Test
    fun erc1155UriTest() {
        val contractAddress = "0x4cbbe042ff9b63e9741a3eb8442295360f046bcd"
        val erc1155Abi = Erc1155Abi()

        var result = web3JRequester.requestEthCall(
            requireNotNull(ethereumWallet.getAddress(mnemonic, 0)),
            contractAddress,
            erc1155Abi.uri(BigInteger.valueOf(107))
        )

        result?.forEach { type -> printLog("result:", type.value) }
            ?: println("result: result is null")

        var index = BigInteger.ZERO
        val limit = BigInteger.valueOf(50)

        while (index <= limit) {
            result = web3JRequester.requestEthCall(
                requireNotNull(ethereumWallet.getAddress(mnemonic, 0)),
                contractAddress,
                erc1155Abi.uri(index)
            )

            result?.forEach { type -> printLog("result[$index]:", type.value) }
                ?: println("result[$index]: result is null")

            index += BigInteger.ONE
        }
    }

}