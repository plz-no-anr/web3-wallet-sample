package com.plznoanr.web3wallet.core.base

import org.bitcoinj.core.NetworkParameters
import org.bitcoinj.crypto.DeterministicHierarchy
import org.bitcoinj.crypto.DeterministicKey
import org.bitcoinj.crypto.HDKeyDerivation
import org.bitcoinj.crypto.HDPath

abstract class BaseWallet {

    private val mnemonic: Mnemonic = Mnemonic()

    abstract fun getAddress(mnemonicList: List<String>, addressIndex: Int): String?

    fun createMnemonic(): List<String>? {
        return mnemonic.generateMnemonic(Mnemonic.BIP39_ENTROPY_LEN_128)
    }

    fun checkMnemonicValidation(mnemonicList: List<String>): Boolean {
        return mnemonic.checkMnemonicValidation(mnemonicList)
    }

    protected fun getBip44DeterministicKey(
        mnemonicList: List<String>,
        coinIndex: Int,
        accountNo: Int,
        externalYn: Boolean,
        addressIndex: Int
    ): DeterministicKey? {
        var parentKey: DeterministicKey? = null
        val bip44ParentPath = "44H/%dH/%dH/%d/%d"
        val externalIndex = if (externalYn) 0 else 1

        try {
            val seed = mnemonic.getSeedFromMnemonic(mnemonicList)

            if (!seed.isNullOrEmpty()) {
                val masterKey = HDKeyDerivation.createMasterPrivateKey(seed)
                val hierarchy = DeterministicHierarchy(masterKey)

                val parentPath = String.format(bip44ParentPath, coinIndex, accountNo, externalIndex, addressIndex)
                val childNumbers = HDPath.parsePath(parentPath)
                parentKey = hierarchy.get(childNumbers, true, true)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return parentKey
    }

    fun getMasterPubkey(
        mnemonicList: List<String>,
        coinIndex: Int,
        accountNo: Int,
        externalYn: Boolean
    ): String? {
        val seed = mnemonic.getSeedFromMnemonic(mnemonicList)
        val externalIndex = if (externalYn) 0 else 1
        val bip44ParentPath = "44H/%dH/%dH/%d"

        if (!seed.isNullOrEmpty()) {
            val masterKey = HDKeyDerivation.createMasterPrivateKey(seed)
            val hierarchy = DeterministicHierarchy(masterKey)
            val parentPath = String.format(bip44ParentPath, coinIndex, accountNo, externalIndex)

            val childNumbers = HDPath.parsePath(parentPath)
            val coinMasterKey = hierarchy.get(childNumbers, true, true)
            return coinMasterKey.serializePubB58(NetworkParameters.fromID(NetworkParameters.ID_MAINNET))
        }
        return null
    }

    private fun ByteArray?.isNullOrEmpty(): Boolean {
        return this == null || this.isEmpty()
    }
}