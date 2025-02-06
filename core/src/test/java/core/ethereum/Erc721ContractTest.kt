package core.ethereum

import com.plznoanr.web3wallet.core.ethereum.abi.Erc721Abi
import core.base.BaseWeb3Test
import core.common.printLog
import org.junit.Test
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.utils.Convert
import java.math.BigDecimal
import java.math.BigInteger

class Erc721ContractTest : BaseWeb3Test() {

    @Test
    fun erc721MetadataTest() {
        val fromAddress = requireNotNull(ethereumWallet.getAddress(mnemonic, 0)) {
            "fromAddress is null"
        }

        val contractAddress = "0x55f3b561f9e0a0caf6fe416d555c26a741d8e922"
        val erc721Abi = Erc721Abi()

        // Token Name
        var result = web3JRequester.requestEthCall(fromAddress, contractAddress, erc721Abi.name())

        result?.forEach { type -> printLog("name:", type.value) }

        // Token Symbol
        result = web3JRequester.requestEthCall(fromAddress, contractAddress, erc721Abi.symbol())

        result?.forEach { type -> printLog("symbol:", type.value) }

        // Token URI
        val tokenId = BigInteger.TWO
        result = web3JRequester.requestEthCall(fromAddress, contractAddress, erc721Abi.tokenURI(tokenId))

        result?.forEach { type -> printLog("token uri:", type.value) }

        // Total Supply
        result = web3JRequester.requestEthCall(fromAddress, contractAddress, erc721Abi.totalSupply())

        var totalSupply = BigInteger.ZERO
        result?.forEach { type ->
            printLog("Total Supply:", type.value)
            printLog("Total Supply Type:", type.typeAsString)
        }

        totalSupply = (result?.get(0) as? Uint256)?.value ?: BigInteger.ZERO

        result = web3JRequester.requestEthCall(fromAddress, contractAddress, erc721Abi.tokenByIndex(totalSupply.subtract(
            BigInteger.ONE)))

        result?.forEach { type -> printLog("tokenByIndex:", type.value) }
    }

    @Test
    fun erc721BalanceOfTest() {
        val holderAddress = "0x14c919A9ad20948BbE50766B257aeCF849FF2Efc"
        val contractAddress = "0x55f3b561f9e0a0caf6fe416d555c26a741d8e922"

        val erc721Abi = Erc721Abi()
        val result = web3JRequester.requestEthCall(
            requireNotNull(ethereumWallet.getAddress(mnemonic, 0)),
            contractAddress,
            erc721Abi.balanceOf(holderAddress)
        )

        result?.forEach { type -> printLog("result:", type.value) }
    }

    @Test
    fun erc721OwnerOfTest() {
        val contractAddress = "0x55f3b561f9e0a0caf6fe416d555c26a741d8e922"

        val erc721Abi = Erc721Abi()
        val result = web3JRequester.requestEthCall(
            requireNotNull(ethereumWallet.getAddress(mnemonic, 0)),
            contractAddress,
            erc721Abi.ownerOf(BigInteger.TWO)
        )

        result?.forEach { type -> printLog("result:", type.value) }
    }

    @Test
    fun erc721SafeTransferTest() {
        val fromAddress = ethereumWallet.getAddress(mnemonic, 0)
        val contractAddress = "0x55f3b561f9e0a0caf6fe416d555c26a741d8e922"
        val toAddress = ethereumWallet.getAddress(mnemonic, 1)
        val credentials = ethereumWallet.getCredentials(mnemonic, 0)
        val tokenId = BigInteger.TWO

        check(fromAddress != null) {
            "fromAddress is null"
        }

        check(toAddress != null) {
            "toAddress is null"
        }

        credentials?.let {
            val erc721Abi = Erc721Abi()
            val function = erc721Abi.safeTransferFrom(fromAddress, toAddress, tokenId)
            val data = FunctionEncoder.encode(function)

            val nonce = requireNotNull(web3JRequester.getNonce(credentials.address, DefaultBlockParameterName.LATEST))
            printLog("nonce:", nonce)

            var gasPrice = web3JRequester.getGasPrice()
            require(gasPrice != null) { "Gas price is null" }

            gasPrice = Convert.toWei(BigDecimal(gasPrice), Convert.Unit.GWEI).toBigInteger()
            printLog("gasPrice:", gasPrice)

            var estimateGas = web3JRequester.getEstimateGas(credentials.address, contractAddress, BigInteger.ZERO, data)
            require(estimateGas != null) { "Estimate gas is null" }
            estimateGas = estimateGas.add(addLimit)
            printLog("estimateGas:", estimateGas)

            val txHash = web3JRequester.sendLegacyTransaction(
                credentials,
                bscTestnetId,
                nonce,
                gasPrice,
                estimateGas,
                contractAddress,
                BigInteger.ZERO,
                data
            )

            printLog("txHash:", txHash)
            require(txHash != null) { "Transaction hash is null" }
        }
    }

}