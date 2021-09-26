package me.ibrahimsn.lib.internal.pattern

import android.util.Log
import me.ibrahimsn.lib.internal.model.CaretString
import me.ibrahimsn.lib.internal.model.Character

class CountryPattern private constructor(
    private val pattern: CharArray
) : Pattern {

    val length: Int
        get() = pattern.size

    override fun apply(number: CaretString): PatternResult {
        val numberArr = number.text.filter { i -> i.isDigit() }
        var cursor = 0

        val currentCaretPos = number.caretPosition
        var appendedCount = 0
        var removedCount = 0

        val text = StringBuilder().apply {
            pattern.forEachIndexed { index, key ->
                if (cursor < numberArr.length) when (key) {
                    Character.PLUS.key -> {
                        append(Character.PLUS.char)
                        if (index >= currentCaretPos) appendedCount++
                    }
                    Character.SPACE.key -> {
                        append(Character.SPACE.char)
                        if (index >= currentCaretPos) appendedCount++
                    }
                    Character.DASH.key -> {
                        append(Character.DASH.char)
                        if (index >= currentCaretPos) appendedCount++
                    }
                    Character.DIGIT.key -> {
                        append(numberArr[cursor++])
                        Log.d("###", "i: $index, start: $currentCaretPos")
                        if (index >= currentCaretPos) appendedCount++
                    }
                }
            }
        }

        if (text.length < number.text.length) {
            removedCount = number.text.length - text.length
        }

        return PatternResult(
            CaretString(
                text = text.toString(),
                caretPosition = currentCaretPos + appendedCount - removedCount,
                caretGravity = number.caretGravity
            )
        )
    }

    companion object {

        fun create(number: String?) = CountryPattern(
            number.orEmpty()
                .replace("(\\d)".toRegex(), Character.DIGIT.key.toString())
                .replace("(\\s)".toRegex(), Character.SPACE.key.toString())
                .toCharArray()
            )
    }
}
