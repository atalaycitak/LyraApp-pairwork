package com.turkcell.lyraapp

import android.app.Application
import com.turkcell.lyraapp.data.player.AudioPlayerManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Hilt'in bağımlılık grafını başlattığı uygulama giriş noktası.
 *
 * `@HiltAndroidApp` annotasyonu, derleme zamanında uygulama düzeyindeki bileşeni üretir;
 * bu sınıf [AndroidManifest] içinde `android:name` ile tanımlanmadan Hilt çalışmaz.
 *
 * Uygulama sonlandığında [AudioPlayerManager] kaynaklarını serbest bırakır
 * (ExoPlayer, CoroutineScope, position polling).
 */
@HiltAndroidApp
class LyraApplication : Application() {

    @Inject
    lateinit var audioPlayerManager: AudioPlayerManager

    override fun onTerminate() {
        super.onTerminate()
        audioPlayerManager.release()
    }
}
