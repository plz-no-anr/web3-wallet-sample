package core.base

import com.plznoanr.web3wallet.core.ethereum.EthereumWallet
import com.plznoanr.web3wallet.core.ethereum.Web3jRequester
import okhttp3.OkHttpClient
import org.junit.Before
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import java.math.BigInteger
import java.util.concurrent.TimeUnit

abstract class BaseWeb3Test {

    protected val testMnemonic: String = "anchor slender execute rocket educate effort quarter glance extra frame card you"
    protected val bscTestnetId: Long = 97 // BSC Testnet (https://chainlist.org/)

    protected lateinit var web3j: Web3j
    protected lateinit var web3JRequester: Web3jRequester
    protected lateinit var ethereumWallet: EthereumWallet
    protected lateinit var mnemonic: List<String>

    protected val addLimit: BigInteger = BigInteger.valueOf(21000)

    @Before
    fun createWeb3jModule() {
        val bscTestnetRpcUrl = "https://data-seed-prebsc-1-s1.binance.org:8545"
        val timeout = 20L

        val client = OkHttpClient.Builder()
            .connectTimeout(timeout, TimeUnit.SECONDS)
            .readTimeout(timeout, TimeUnit.SECONDS)
            .writeTimeout(timeout, TimeUnit.SECONDS)
            .build()

        web3j = Web3j.build(HttpService(bscTestnetRpcUrl, client, false))
        web3JRequester = Web3jRequester(web3j)
        ethereumWallet = EthereumWallet()
        mnemonic = testMnemonic.split(" ")
    }

}