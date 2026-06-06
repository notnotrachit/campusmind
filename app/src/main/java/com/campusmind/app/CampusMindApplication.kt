package com.campusmind.app

import android.app.Application
import com.campusmind.app.agent.AgentRouter
import com.campusmind.app.data.CampusMindDatabase
import com.campusmind.app.data.CampusMindRepository
import com.campusmind.app.settings.ModelSettingsStore

class CampusMindApplication : Application() {
  val database by lazy { CampusMindDatabase.get(this) }
  val repository by lazy { CampusMindRepository(database.dao(), AgentRouter()) }
  val modelSettingsStore by lazy { ModelSettingsStore(this) }
}
