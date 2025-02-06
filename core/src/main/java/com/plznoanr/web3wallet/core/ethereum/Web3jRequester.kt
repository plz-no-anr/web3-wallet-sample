package com.plznoanr.web3wallet.core.ethereum

import org.web3j.abi.FunctionEncoder
import org.web3j.abi.FunctionReturnDecoder
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.Type
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.request.Transaction
import org.web3j.tx.RawTransactionManager
import org.web3j.utils.Convert
import java.io.IOException
import java.math.BigDecimal
import java.math.BigInteger

class Web3jRequester(private val web3j: Web3j) {

    fun getNonce(address: String, parameterName: DefaultBlockParameterName): BigInteger? {
        val request = web3j.ethGetTransactionCount(address, parameterName)
        return try {
            val transactionCount = request.send()
            if (transactionCount?.error == null) {
                transactionCount.transactionCount
            } else null
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    fun getBalance(address: String, parameterName: DefaultBlockParameterName): BigInteger? {
        val request = web3j.ethGetBalance(address, parameterName)
        return try {
            val balance = request.send()
            if (balance?.error == null) {
                balance.balance
            } else null
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    fun getGasPrice(): BigInteger? {
        return try {
            val request = web3j.ethGasPrice().send()
            if (request?.error == null) {
                Convert.fromWei(BigDecimal(request.gasPrice), Convert.Unit.GWEI).toBigInteger()
            } else null
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    fun getEstimateGas(
        fromAddress: String,
        toAddress: String,
        value: BigInteger,
        data: String
    ): BigInteger? {
        return try {
            val transaction = Transaction(
                fromAddress,
                null,
                null,
                null,
                toAddress,
                value,
                data
            )
            val estimateGas = web3j.ethEstimateGas(transaction).send()
            if (estimateGas?.error == null) {
                estimateGas.amountUsed
            } else null
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    fun requestEthCall(fromAddress: String, toAddress: String, function: Function): List<Type<*>>? {
        val encodedFunction = FunctionEncoder.encode(function)
        return try {
            val ethCall = web3j.ethCall(
                Transaction.createEthCallTransaction(fromAddress, toAddress, encodedFunction),
                DefaultBlockParameterName.LATEST
            ).send()
            if (ethCall?.error == null) {
                FunctionReturnDecoder.decode(ethCall.value, function.outputParameters)
            } else null
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    fun sendLegacyTransaction(
        credentials: Credentials,
        chainId: Long,
        nonce: BigInteger,
        gasPrice: BigInteger,
        gasLimit: BigInteger,
        to: String,
        value: BigInteger,
        data: String
    ): String? {
        val signedTransaction = getLegacySignedTransaction(
            credentials,
            chainId,
            nonce,
            gasPrice,
            gasLimit,
            to,
            value,
            data
        )
        return try {
            val request = web3j.ethSendRawTransaction(signedTransaction).send()
            if (request?.error == null) {
                request.transactionHash
            } else null
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    fun sendEip1559Transaction(
        credentials: Credentials,
        chainId: Long,
        contractAddress: String,
        value: BigInteger,
        nonce: BigInteger,
        gasLimit: BigInteger,
        gasPremium: BigInteger,
        feeCap: BigInteger,
        data: String
    ): String? {
        val signedTransaction = getEip1559SignedTransaction(
            credentials, chainId, contractAddress, value, nonce, gasLimit, gasPremium, feeCap, data
        )
        return try {
            val request = web3j.ethSendRawTransaction(signedTransaction).send()
            if (request?.error == null) {
                request.transactionHash
            } else null
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun getLegacySignedTransaction(
        credentials: Credentials,
        chainId: Long,
        nonce: BigInteger,
        gasPrice: BigInteger,
        gasLimit: BigInteger,
        to: String,
        value: BigInteger,
        data: String
    ): String {
        val rawTransaction = org.web3j.crypto.RawTransaction.createTransaction(
            nonce, gasPrice, gasLimit, to, value, data
        )
        val transactionManager = RawTransactionManager(web3j, credentials, chainId)
        return transactionManager.sign(rawTransaction)
    }

    private fun getEip1559SignedTransaction(
        credentials: Credentials,
        chainId: Long,
        contractAddress: String,
        value: BigInteger,
        nonce: BigInteger,
        gasLimit: BigInteger,
        gasPremium: BigInteger,
        feeCap: BigInteger,
        function: String
    ): String {
//        val rawTransaction = org.web3j.crypto.RawTransaction.createTransaction(
//            nonce, null, gasLimit, contractAddress, value, function, gasPremium, feeCap
//        )
        val rawTransaction = org.web3j.crypto.RawTransaction.createTransaction(
            nonce, null, gasLimit, contractAddress, value, function
        )
        val transactionManager = RawTransactionManager(web3j, credentials, chainId)
        return transactionManager.sign(rawTransaction)
    }
}