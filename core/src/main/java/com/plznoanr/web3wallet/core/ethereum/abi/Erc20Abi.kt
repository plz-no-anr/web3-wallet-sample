package com.plznoanr.web3wallet.core.ethereum.abi

import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigInteger

class Erc20Abi {
    fun transfer(toAddress: String, amount: BigInteger): Function =
        Function(
            "transfer",
            listOf(
                Address(toAddress),
                Uint256(amount)
            ),
            emptyList()
        )
}