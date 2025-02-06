package com.plznoanr.web3wallet.core.ethereum.abi

import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.Type
import org.web3j.abi.datatypes.Utf8String
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigInteger

// https://docs.openzeppelin.com/contracts/4.x/api/token/erc721
class Erc721Abi {

    // IERC721 Spec
    // balanceOf(address owner) → uint256 balance
    // How many different tokens an account has
    fun balanceOf(owner: String): Function {
        return Function(
            "balanceOf",
            listOf(Address(owner)),
            listOf<TypeReference<*>>(object : TypeReference<Uint256>() {})
        )
    }

    // ownerOf(uint256 tokenId) → address owner
    fun ownerOf(tokenId: BigInteger): Function {
        return Function(
            "ownerOf",
            listOf<Type<*>>(Uint256(tokenId)),
            listOf<TypeReference<*>>(object : TypeReference<Address>() {})
        )
    }

    // safeTransferFrom(address from, address to, uint256 tokenId)
    fun safeTransferFrom(from: String, to: String, tokenId: BigInteger): Function {
        return Function(
            "safeTransferFrom",
            listOf(Address(from), Address(to), Uint256(tokenId)),
            emptyList()
        )
    }

    // transferFrom(address from, address to, uint256 tokenId)
    fun transferFrom(from: String, to: String, tokenId: BigInteger): Function {
        return Function(
            "transferFrom",
            listOf(Address(from), Address(to), Uint256(tokenId)),
            emptyList()
        )
    }

    // approve(address to, uint256 tokenId)
    fun approve(to: String, tokenId: BigInteger): Function {
        return Function(
            "approve",
            listOf(Address(to), Uint256(tokenId)),
            emptyList()
        )
    }

    // IERC721Metadata Spec
    fun name(): Function {
        return Function(
            "name",
            emptyList(),
            listOf<TypeReference<*>>(object : TypeReference<Utf8String>() {})
        )
    }

    fun symbol(): Function {
        return Function(
            "symbol",
            emptyList(),
            listOf<TypeReference<*>>(object : TypeReference<Utf8String>() {})
        )
    }

    // tokenURI(tokenId)
    fun tokenURI(tokenId: BigInteger): Function {
        return Function(
            "tokenURI",
            listOf<Type<*>>(Uint256(tokenId)),
            listOf<TypeReference<*>>(object : TypeReference<Utf8String>() {})
        )
    }

    // IERC721Enumerable Spec
    // totalSupply() → uint256
    fun totalSupply(): Function {
        return Function(
            "totalSupply",
            emptyList(),
            listOf<TypeReference<*>>(object : TypeReference<Uint256>() {})
        )
    }

    // tokenOfOwnerByIndex(address owner, uint256 index) → uint256 tokenId
    fun tokenOfOwnerByIndex(owner: String, index: BigInteger): Function {
        return Function(
            "tokenOfOwnerByIndex",
            listOf<Type<*>>(Address(owner), Uint256(index)),
            listOf<TypeReference<*>>(object : TypeReference<Uint256>() {})
        )
    }

    // tokenByIndex(index) → uint256
    fun tokenByIndex(index: BigInteger): Function {
        return Function(
            "tokenByIndex",
            listOf<Type<*>>(Uint256(index)),
            listOf<TypeReference<*>>(object : TypeReference<Uint256>() {})
        )
    }
}