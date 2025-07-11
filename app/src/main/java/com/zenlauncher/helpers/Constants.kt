package com.zenlauncher.helpers

import android.graphics.Color

object Constants {

    const val APP_TITLE = "Zen Launcher"
    const val SEARCH_APPS_HINT = "Search apps"
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


    object Animation {
        const val SCALE_UP = 1.1f
        const val SCALE_NORMAL = 1f
        const val DURATION_MS = 80L
    }

    object Colors {
        const val LETTER_COLOR_DEFAULT = Color.LTGRAY
        const val ICON_COLOR = Color.WHITE
    }

    object Sizes {
        const val ICON_WIDTH = 90
        const val ICON_HEIGHT = 90
        const val ICON_PADDING = 8

        const val CLOCK_TEXT_SIZE = 36f
        const val DATE_TEXT_SIZE = 16f
        const val FAV_TITLE_TEXT_SIZE = 20f
        const val FAV_TITLE_PADDING_H = 60
        const val FAV_TITLE_PADDING_TOP = 90
        const val FAV_TITLE_PADDING_BOTTOM = 16
        const val VERTICAL_LAYOUT_PADDING_TOP = 100
        const val SPACE_SMALL = 12
        const val SPACE_LARGE = 120
        const val APP_LABEL_TEXT_SIZE = 18f
        const val APP_LABEL_PADDING_V = 25
        const val APPS_LAYOUT_PADDING_H = 48

        const val ICON_MARGIN_SIDE = 40
        const val ICON_MARGIN_BOTTOM = 20
        const val DOUBLE_TAP_THRESHOLD_MS = 400

        const val INDEXBAR_PADDING_HORIZONTAL_DP = 4
        const val INDEXBAR_PADDING_TOP_DP = 40
        const val INDEXBAR_PADDING_BOTTOM_DP = 8

        const val SETTINGS_ICON_SIZE_DP = 32
        const val SETTINGS_ICON_MARGIN_BOTTOM_DP = 8
        const val SETTINGS_ICON_PADDING_DP = 6

        const val LETTER_TEXT_SIZE_SP = 10f
        const val LETTER_TEXT_SIZE_SELECTED_SP = 12f
        const val LETTER_TEXT_SIZE_DEFAULT_SP = 9f
        const val LETTER_HEIGHT_DP = 20

        const val ANIMATION_DURATION_MS = 80L
    }

    object Settings {
        const val TITLE = "Zen Launcher Settings"
        const val PADDING_HORIZONTAL = 40
        const val PADDING_VERTICAL = 80
        const val TEXT_PADDING_ALL = 5
        const val TEXT_SIZE_OPTION = 16f
        const val TEXT_SIZE_HEADER = 18f

        // Spacing values
        const val SPACING_SMALL = 20
        const val SPACING_MEDIUM = 40
        const val SPACING_LARGE = 60
        const val SPACING_XLARGE = 80
        const val SPACING_XXLARGE = 120
    }



    object Texts {
        const val SHARE_TEXT = "Try ZenLauncher for a minimal, fast Android experience. " +
                "https://github.com/Santhoshmani1/ZenLauch"
    }

    object Toasts {
        const val DEVICE_ADMIN_ENABLED = "Device Admin Enabled"
        const val DEVICE_ADMIN_DISABLED = "Device Admin Disabled"
        const val DEVICE_ADMIN_INACTIVE = "Device Admin not active"
        const val CLOCK_NOT_AVAILABLE = "Clock settings not available"
    }
}
