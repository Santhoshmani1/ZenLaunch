package com.zenlauncher.helpers

object Constants {

    const val APP_TITLE = "Zen Launcher"
    val APP_OPTIONS = arrayOf("Add to Favourites", "Rename", "App Info", "Uninstall")

    const val GITHUB_REPO = "https://github.com/santhoshmani1/Zenlaunch"

    const val DIGITAL_WELLBEING_PACKAGE_NAME = "com.google.android.apps.wellbeing"
    const val DIGITAL_WELLBEING_ACTIVITY =
        "com.google.android.apps.wellbeing.settings.TopLevelSettingsActivity"
    const val DIGITAL_WELLBEING_SAMSUNG_PACKAGE_NAME = "com.samsung.android.forest"
    const val DIGITAL_WELLBEING_SAMSUNG_ACTIVITY =
        "com.samsung.android.forest.launcher.LauncherActivity"

    object Intents {
        const val SHARE_VIA = "Share via"
        const val DEVICE_ADMIN_INFO = "Enable Zen Launcher double-tap to lock"
    }

    object Prefs {
        const val LAUNCHER_PREFS = "launcher_prefs"
        const val FAVORITES_KEY = "favourites"
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
        const val TITLE = "Settings"

        object Ui {
            const val OPTION_TEXT_SIZE = 16
            const val SUBTITLE_TEXT_SIZE = 13
            const val HEADER_TEXT_SIZE = 22
            const val SUB_HEADER_TEXT_SIZE = 18
            const val DIALOG_TITLE_SIZE = 20
            const val DIALOG_BODY_SIZE = 15
        }

        object Colors {
            val DIALOG_BG = androidx.compose.ui.graphics.Color(0xFF111111)
            val OVERLAY_BG = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.6f)
            val PRIMARY = androidx.compose.ui.graphics.Color(0xFF069AED)
        }

        object Texts {
            const val SECTION_GENERAL = "General"
            const val SECTION_SUPPORT = "Support"

            const val SET_DEFAULT_LAUNCHER_TITLE = "Set as default launcher"
            const val SET_DEFAULT_LAUNCHER_SUB = "Use ZenLauncher as home"

            const val DEACTIVATE_ADMIN_TITLE = "Deactivate device admin"
            const val DEACTIVATE_ADMIN_SUB = "Remove admin permission"

            const val SHARE_TITLE = "Share ZenLauncher"
            const val SHARE_SUB = "Invite your friends & family"

            const val GITHUB_TITLE = "Star on GitHub"
            const val GITHUB_SUB = "Support open-source"

            const val EXIT_TITLE_OPTION = "Exit ZenLauncher"
            const val EXIT_SUB = "Close launcher session"

            const val EXIT_TITLE = "Exit ZenLauncher"
            const val EXIT_CONFIRM = "Are you sure you want to exit ZenLauncher?"
            const val SELECT_LAUNCHER_TITLE = "Select default launcher"
            const val SELECT_LAUNCHER_DESC =
                "Choose another launcher app to exit ZenLauncher."
            const val CANCEL = "Cancel"
            const val EXIT = "Exit"
            const val OPEN_SETTINGS = "Open Settings"
            const val SHARE_TEXT =
                "Try ZenLauncher for a minimal, focussed Android experience. https://github.com/Santhoshmani1/ZenLaunch"
        }
    }

    object Toasts {
        const val DEVICE_ADMIN_ENABLED = "Device Admin Enabled"
        const val DEVICE_ADMIN_DISABLED = "Device Admin Disabled"
        const val DEVICE_ADMIN_INACTIVE = "Device Admin not active"
        const val CLOCK_NOT_AVAILABLE = "Clock settings not available"
    }
}
