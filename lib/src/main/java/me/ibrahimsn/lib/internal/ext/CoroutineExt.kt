package me.ibrahimsn.lib.internal.ext

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal suspend fun <T> default(
    block: suspend () -> T
) = withContext(Dispatchers.Default) {
    block.invoke()
}

internal suspend fun <T> io(
    block: suspend () -> T
) = withContext(Dispatchers.IO) {
    block.invoke()
}
