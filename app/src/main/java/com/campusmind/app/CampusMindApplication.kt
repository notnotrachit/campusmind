package com.campusmind.app

import android.app.Application
import com.campusmind.app.agent.AgentRouter
import com.campusmind.app.ai.LiteRtLocalLlmEngine
import com.campusmind.app.ai.ModelDownloadManager
import com.campusmind.app.ai.SingleModelRunner
import com.campusmind.app.data.CampusMindDatabase
import com.campusmind.app.data.CampusMindRepository
import com.campusmind.app.settings.ModelSettingsStore
import kotlinx.coroutines.flow.first

class CampusMindApplication : Application() {
  val database by lazy { CampusMindDatabase.get(this) }
  val modelSettingsStore by lazy { ModelSettingsStore(this) }
  val modelDownloadManager by lazy { ModelDownloadManager(this) }
  val modelRunner by lazy {
    SingleModelRunner(
      configProvider = { modelSettingsStore.config.first() },
      engine = LiteRtLocalLlmEngine(this),
    )
  }
  val repository by lazy { CampusMindRepository(database.dao(), AgentRouter(modelRunner)) }
}
