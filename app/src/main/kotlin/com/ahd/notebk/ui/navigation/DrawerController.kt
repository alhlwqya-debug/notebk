package com.ahd.notebk.ui.navigation

import androidx.compose.runtime.staticCompositionLocalOf

val LocalDrawerOpen = staticCompositionLocalOf<() -> Unit> {
    error("Drawer controller is not provided")
}
