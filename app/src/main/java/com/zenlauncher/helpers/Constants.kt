package com.zenlauncher.helpers

object Constants {

    const val APP_TITLE = "Zen Launcher"
    val APP_OPTIONS = arrayOf("Add to Favorites", "Rename", "App Info", "Uninstall")

    const val GITHUB_REPO = "https://github.com/santhoshmani1/Zenlaunch"

    object Intents {
        const val SHARE_VIA = "Share via"
        const val DEVICE_ADMIN_INFO = "Enable Zen Launcher double-tap to lock"
    }

    object Prefs {
        const val LAUNCHER_PREFS = "launcher_prefs"
        const val FAVORITES_KEY = "favorites"
    }


    object Sizes {
        const val CLOCK_TEXT_SIZE = 40
        const val DATE_TEXT_SIZE = 16
        const val FAV_TITLE_TEXT_SIZE = 16
        const val APP_LABEL_TEXT_SIZE = 20
        const val APP_LABEL_PADDING_V = 12
        const val DOUBLE_TAP_THRESHOLD_MS = 400

    }

    object Settings {
        const val TITLE = "Zen Launcher Settings"
        const val PADDING_HORIZONTAL = 40
        const val PADDING_VERTICAL = 80
        const val TEXT_PADDING_ALL = 5
        const val TEXT_SIZE_OPTION = 16f

        // Spacing values
        const val SPACING_SMALL = 20
        const val SPACING_MEDIUM = 40
        const val SPACING_LARGE = 60
    }


    object Texts {
        const val SHARE_TEXT = "Try ZenLauncher for a minimal, fast Android experience. " +
                "https://github.com/Santhoshmani1/ZenLaunch"
    }

    object Toasts {
        const val DEVICE_ADMIN_ENABLED = "Device Admin Enabled"
        const val DEVICE_ADMIN_DISABLED = "Device Admin Disabled"
        const val DEVICE_ADMIN_INACTIVE = "Device Admin not active"
        const val CLOCK_NOT_AVAILABLE = "Clock settings not available"
    }
}
