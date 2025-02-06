package com.plznoanr.web3wallet.core.ethereum.abi

import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.DynamicArray
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.Type
import org.web3j.abi.datatypes.Utf8String
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigInteger

class Erc1155Abi {

    //// IERC1155 Spec
    // balanceOf(address account, uint256 id) → uint256
    fun balanceOf(account: String?, id: BigInteger?): Function =
        Function(
            "balanceOf",
            listOf<Type<*>>(Address(account), Uint256(id)),
            listOf<TypeReference<*>>(object : TypeReference<Uint256?>() {})
        )


    // balanceOfBatch(address[] account, uint256[] id) → uint256[]
    fun balanceOf(accounts: List<String?>, ids: List<BigInteger?>): Function {
        val addresses: MutableList<Address> = ArrayList<Address>().apply {
            for (account in accounts) {
                add(Address(account))
            }
        }

        val tokenIds: MutableList<Uint256> = ArrayList<Uint256>().apply {
            for (id in ids) {
                add(Uint256(id))
            }
        }

        return Function(
            "balanceOf",
            listOf<Type<*>>(
                DynamicArray(Address::class.java, addresses),
                DynamicArray(Uint256::class.java, tokenIds)
            ),
            listOf<TypeReference<*>>(object : TypeReference<DynamicArray<Uint256?>?>() {})
        )
    }

    // token id
    // uri(uint256 id) → string
    fun uri(id: BigInteger?): Function =
        Function(
            "uri",
            listOf<Type<*>>(Uint256(id)),
            listOf<TypeReference<*>>(object : TypeReference<Utf8String?>() {})
        )

}