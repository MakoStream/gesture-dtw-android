package ua.makostream.gesturedtw

import android.util.Log
import timber.log.Timber

class ReleaseTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority < Log.WARN) return
        val text = if (t != null) "$message\n${Log.getStackTraceString(t)}" else message
        Log.println(priority, tag ?: "GestureDTW", text)
    }
}