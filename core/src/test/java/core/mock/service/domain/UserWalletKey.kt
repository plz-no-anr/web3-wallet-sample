package core.mock.service.domain

data class UserWalletKey(
    val type: Int,  // 1 - user password type, 2 - user answer type
    val walletKey: String, // 1 - user password, 2 - user answer
    val encryptWalletSeed: String? = null
)
