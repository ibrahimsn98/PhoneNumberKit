package me.ibrahimsn.lib.internal.model

data class CaretString(
    val text: String,
    val caretPosition: Int,
    val caretGravity: CaretGravity
) {

    sealed class CaretGravity {

        object FORWARD : CaretGravity()

        object BACKWARD : CaretGravity()

        val isForward: Boolean
            get() = this == FORWARD

        val isBackward: Boolean
            get() = this == BACKWARD
    }
}
