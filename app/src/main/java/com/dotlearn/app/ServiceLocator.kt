package com.dotlearn.app

import android.content.Context
import com.dotlearn.app.backup.BackupManager
import com.dotlearn.app.data.AppDatabase
import com.dotlearn.app.data.DotLearnRepository
import com.dotlearn.app.data.SettingsStore

/**
 * 轻量级服务定位器：避免引入 DI 框架，保持工程简单可读。
 * 在 Application.onCreate 中初始化。
 */
object ServiceLocator {

    lateinit var repository: DotLearnRepository
        private set

    lateinit var settings: SettingsStore
        private set

    lateinit var backup: BackupManager
        private set

    fun init(context: Context) {
        if (::repository.isInitialized) return
        val appContext = context.applicationContext
        repository = DotLearnRepository(AppDatabase.get(appContext))
        settings = SettingsStore(appContext)
        backup = BackupManager(appContext, repository)
    }
}
