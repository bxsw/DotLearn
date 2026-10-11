package com.dotlearn.app

import android.app.Application
import com.dotlearn.app.work.NotificationHelper

class DotLearnApp : Application() {

    override fun onCreate() {
        super.onCreate()
        ServiceLocator.init(this)
        NotificationHelper.ensureChannel(this)
    }
}
