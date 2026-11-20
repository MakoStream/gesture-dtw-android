package ua.makostream.gesturedtw.ui

import androidx.annotation.StringRes
import ua.makostream.gesturedtw.R

object GestureCatalog {

    data class Entry(val id: String, @StringRes val labelRes: Int)

    val all = listOf(
        Entry("shake", R.string.gesture_shake),
        Entry("circle", R.string.gesture_circle),
        Entry("up_down", R.string.gesture_up_down),
        Entry("left_right", R.string.gesture_left_right)
    )

    @StringRes
    fun labelFor(id: String): Int =
        all.firstOrNull { it.id == id }?.labelRes ?: R.string.gesture_unknown
}