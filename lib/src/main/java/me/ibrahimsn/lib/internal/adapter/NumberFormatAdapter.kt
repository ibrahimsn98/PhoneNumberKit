package me.ibrahimsn.lib.internal.adapter

import me.ibrahimsn.lib.internal.Constants

class NumberFormatAdapter : FormatAdapter {

    override fun format(rawNumber: String): String {
        var format = rawNumber.replace("(\\d)".toRegex(), Constants.KEY_DIGIT.toString())
        format = format.replace("(\\s)".toRegex(), Constants.KEY_SPACE.toString())
        return format
    }
}
