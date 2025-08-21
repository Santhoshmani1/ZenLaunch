package com.zenlauncher.data.models

/**
 * Represents basic information about an installed application.
 *
 * @property label The user-facing name of the app (e.g., "WhatsApp").
 * @property packageName The unique package identifier of the app (e.g., "com.whatsapp").
 * @property className The name of the main activity class to launch the app.
 */
data class AppInfo(
    val label: String,
    val packageName: String,
    val className: String
)