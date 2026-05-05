package com.todoapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Hilt entry-point application class.
 * Must be declared in AndroidManifest as android:name=".TodoApplication".
 */
@HiltAndroidApp
class TodoApplication : Application()
