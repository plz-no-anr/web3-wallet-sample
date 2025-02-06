package core.common

fun <T> printLog(
    msg: String = "",
    data: T?
) {
    val msgPrefix = if (msg.isEmpty()) "" else "$msg "
    println(msgPrefix + data)
}