package ua.makostream.gesturedtw

import android.app.Application
import timber.log.Timber

class GestureApp : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(ReleaseTree())
        }
        Timber.i("Application started, version %s", BuildConfig.VERSION_NAME)
    }
}