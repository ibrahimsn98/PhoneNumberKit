package me.ibrahimsn.lib.internal.io

import android.content.Context
import java.io.IOException

object FileReader {

    fun readAssetFile(context: Context, name: String): String? {
        return try {
            return context.assets.open(name)
                .bufferedReader()
                .use{ it.readText() }
        } catch (e: IOException) {
            null
        }
    }
}
