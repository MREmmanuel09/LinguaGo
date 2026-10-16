package dev.linguago.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/** Root Hilt de la aplicación (RFC §2.3: DI global). */
@HiltAndroidApp
class LinguaGoApplication : Application()
