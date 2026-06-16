package com.turkcell.lyraapp.ui.navigation

/**
 * Uygulamadaki navigasyon hedeflerinin tek doğruluk kaynağı.
 *
 * Her hedef benzersiz bir [route] string'iyle temsil edilir; [LyraNavHost] bu route'lar
 * üzerinden composable'ları bağlar. Yeni bir ekran eklendiğinde buraya bir hedef eklenir.
 *
 * [NowPlaying] rotası `{songId}` path parametresi taşır; hedef oluşturulurken
 * [NowPlaying.routeWithArg] kullanılır.
 */
enum class LyraDestination(val route: String) {
    Login("login"),
    Register("register"),
    Home("home"),
    Search("search"),
    Library("library"),
    Favorites("favorites"),
    Profile("profile"),
    NowPlaying("now_playing/{songId}"),
    CreatePlaylist("create_playlist");

    companion object {
        /** NowPlaying rotası için songId'yi yerleştirilmiş tam rota döndürür. */
        fun nowPlayingRoute(songId: String): String = "now_playing/$songId"
    }
}

