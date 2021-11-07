package me.ibrahimsn.lib.internal.pattern

import me.ibrahimsn.lib.internal.model.CaretString
import me.ibrahimsn.lib.internal.model.Character

class CountryPattern private constructor(
    private val pattern: CharArray
) : Pattern {

    val length: Int
        get() = pattern.size

    override fun apply(number: CaretString, before: Int, count: Int): PatternResult {
        val numberArr = number.text.filter { i -> i.isDigit() }

        var cursor = 0
        val currentCaretPos = number.caretPosition

        val text = StringBuilder().apply {
            pattern.forEachIndexed { _, key ->
                if (cursor < numberArr.length) when (key) {
                    Character.PLUS.key -> {
                        append(Character.PLUS.char)
                    }
                    Character.SPACE.key -> {
                        append(Character.SPACE.char)
                    }

                    Character.DASH.key -> {
                        append(Character.DASH.char)
                    }
                    Character.DIGIT.key -> {
                        append(numberArr[cursor++])
                    }
                }
            }
        }

        val extraAppends = calculateExtraAppends(currentCaretPos, count)
        val extraRemovals = calculateExtraRemovals(currentCaretPos, before)

        return PatternResult(
            CaretString(
                text = text.toString(),
                caretPosition = number.caretPosition + count + extraAppends - extraRemovals,
                caretGravity = number.caretGravity
            )
        )
    }

    private fun calculateExtraAppends(currentPos: Int, appendedCount: Int): Int {
        var appends = 0
        var cursor = 0
        for (i in currentPos until pattern.size) {
            if (cursor < appendedCount) {
                if (pattern[i] != Character.DIGIT.key) appends++ else cursor++
            }
        }
        return appends
    }

    private fun calculateExtraRemovals(currentPos: Int, removedCount: Int): Int {
        var removals = 0
        var cursor = 0
        for (i in currentPos - 1 downTo 0) {
            if (cursor < removedCount) {
                if (pattern[i] != Character.DIGIT.key) removals++ else cursor++
            }
        }
        return removals
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
