package com.turkcell.lyraapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.turkcell.lyraapp.data.theme.ThemePreferences
import com.turkcell.lyraapp.ui.navigation.LyraNavHost
import com.turkcell.lyraapp.ui.theme.LyraAppTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var themePreferences: ThemePreferences

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* Kullanici izni versin ya da vermesin uygulama calismasina devam eder */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestNotificationPermissionIfNeeded()
        enableEdgeToEdge()
        setContent {
            val isDarkModeOverride by themePreferences.isDarkModeFlow.collectAsState()
            val isDarkTheme = isDarkModeOverride ?: isSystemInDarkTheme()
            
            LyraAppTheme(darkTheme = isDarkTheme) {
                LyraNavHost()
            }
        }
    }

    /**
     * Android 13 (API 33) ve uzeri cihazlarda POST_NOTIFICATIONS izni verilmemisse
     * runtime izin istegi baslatir. Bu izin olmadan PlaybackService'in medya bildirimi
     * bildirim panelinde goruntulenemez.
     */
    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(permission)
            }
        }
    }
}
