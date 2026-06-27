package com.turkcell.lyraapp.ui.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.turkcell.lyraapp.ui.auth.phone.PhoneRoute
import com.turkcell.lyraapp.ui.auth.otp.OtpRoute
import com.turkcell.lyraapp.ui.profile.complete.CompleteProfileRoute
import com.turkcell.lyraapp.ui.favorites.FavoritesRoute
import com.turkcell.lyraapp.ui.home.HomeRoute
import com.turkcell.lyraapp.ui.library.LibraryRoute
import com.turkcell.lyraapp.ui.notifications.NotificationsRoute
import com.turkcell.lyraapp.ui.nowplaying.NowPlayingRoute
import com.turkcell.lyraapp.ui.search.SearchRoute
import com.turkcell.lyraapp.ui.playlist.create.CreatePlaylistRoute
import com.turkcell.lyraapp.ui.premium.plans.PremiumPlansRoute
import com.turkcell.lyraapp.ui.premium.payment.PaymentRoute

/**
 * Uygulamanın iskelet navigasyon yapısı.
 *
 * Tek [NavHost], Auth grafiği ile ana akış sekmelerini barındırır; başlangıç hedefi
 * [LyraDestination.Login]'dir. Dış [Scaffold]'ın `bottomBar` yuvasındaki [LyraBottomBar]
 * yalnızca üst düzey sekme rotalarında görünür; böylece çubuk her ana sayfanın altında
 * yer alır, Auth ekranlarında gizlenir.
 *
 * Her ekranın `Route` composable'ı, MVI Effect'lerini buradan sağlanan navigasyon
 * lambda'larına köprüler (ViewModel navigasyon API'si bilmez; bkz. mvi-viewmodel-rules §6).
 *
 * Dış Scaffold'ın `contentWindowInsets`'i sıfırlanır: sistem çubuğu boşluklarını her ekran
 * kendisi yönetir (Login/Register'da olduğu gibi); içerik dolgusu yalnızca alt çubuğun
 * yüksekliğini taşır.
 */
import com.turkcell.lyraapp.ui.components.premium.GlobalPremiumRenewalManager

@Composable
fun LyraNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    // Check and show premium renewal dialog if needed
    GlobalPremiumRenewalManager(
        onNavigateToPayment = { planId ->
            navController.navigate(LyraDestination.paymentRoute(planId)) {
                launchSingleTop = true
            }
        }
    )

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (currentRoute != LyraDestination.Phone.route && 
                currentRoute?.startsWith("otp") != true &&
                currentRoute != LyraDestination.CompleteProfile.route && 
                currentRoute?.startsWith("premium_plans") != true &&
                currentRoute?.startsWith("payment") != true &&
                currentRoute?.startsWith(LyraDestination.NowPlaying.route.substringBefore("{")) != true) {
                androidx.compose.foundation.layout.Column {
                    com.turkcell.lyraapp.ui.components.miniplayer.LyraMiniPlayerRoute(
                        onNavigateToNowPlaying = { songId ->
                            navController.navigate(LyraDestination.nowPlayingRoute(songId)) {
                                launchSingleTop = true
                            }
                        }
                    )
                    if (isTopLevelRoute(currentRoute)) {
                        LyraBottomBar(
                            currentRoute = currentRoute,
                            onTabSelected = navController::navigateToTab,
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = LyraDestination.Phone.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(LyraDestination.Phone.route) {
                PhoneRoute(
                    onNavigateToOtp = { phone ->
                        navController.navigate(LyraDestination.otpRoute(phone)) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(
                route = LyraDestination.Otp.route,
                arguments = listOf(
                    navArgument("phoneNumber") { type = NavType.StringType }
                )
            ) {
                OtpRoute(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToHome = { navController.navigateToHomeClearingAuth() },
                    onNavigateToCompleteProfile = {
                        navController.navigate(LyraDestination.CompleteProfile.route) {
                            launchSingleTop = true
                        }
                    }
                )
            }
            
            composable(LyraDestination.CompleteProfile.route) {
                CompleteProfileRoute(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToHome = { navController.navigateToHomeClearingAuth() }
                )
            }

            composable(LyraDestination.Home.route) {
                HomeRoute(
                    onNavigateToNowPlaying = { songId ->
                        navController.navigate(LyraDestination.nowPlayingRoute(songId)) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToPlaylistDetail = { playlistId ->
                        navController.navigate(LyraDestination.playlistDetailRoute(playlistId)) {
                            launchSingleTop = true
                        }
                    },
                )
            }
            composable(LyraDestination.Search.route) {
                SearchRoute(
                    onNavigateToPlayer = { songId ->
                        navController.navigate(LyraDestination.nowPlayingRoute(songId)) {
                            launchSingleTop = true
                        }
                    },
                )
            }
            composable(LyraDestination.Library.route) {
                LibraryRoute(
                    onNavigateToCreatePlaylist = {
                        navController.navigate(LyraDestination.CreatePlaylist.route) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToPlaylistDetail = { playlistId ->
                        navController.navigate(LyraDestination.playlistDetailRoute(playlistId)) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToPlayer = { songId ->
                        navController.navigate(LyraDestination.nowPlayingRoute(songId)) {
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(LyraDestination.Favorites.route) { 
                com.turkcell.lyraapp.ui.favorites.FavoritesRoute(
                    onNavigateToPlayer = { songId ->
                        navController.navigate(LyraDestination.nowPlayingRoute(songId)) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToPlaylistDetail = { playlistId ->
                        navController.navigate(LyraDestination.playlistDetailRoute(playlistId)) {
                            launchSingleTop = true
                        }
                    }
                ) 
            }
            composable(LyraDestination.Profile.route) {
                com.turkcell.lyraapp.ui.profile.ProfileRoute(
                    onNavigateToLogin = {
                        navController.navigate(LyraDestination.Phone.route) {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onNavigateToNotifications = {
                        navController.navigate(LyraDestination.Notifications.route) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToPremiumPlans = {
                        navController.navigate(LyraDestination.PremiumPlans.route) {
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(LyraDestination.Notifications.route) {
                NotificationsRoute(
                    onNavigateBack = { navController.popBackStack() },
                )
            }
            composable(
                route = LyraDestination.NowPlaying.route,
                arguments = listOf(
                    navArgument("songId") { type = NavType.StringType }
                ),
            ) {
                NowPlayingRoute(
                    onNavigateBack = { navController.popBackStack() },
                )
            }
            composable(LyraDestination.CreatePlaylist.route) {
                CreatePlaylistRoute(
                    onNavigateBack = { navController.popBackStack() },
                )
            }
            composable(
                route = LyraDestination.PlaylistDetail.route,
                arguments = listOf(
                    navArgument("playlistId") { type = NavType.StringType }
                ),
            ) { backStackEntry ->
                val playlistId = backStackEntry.arguments?.getString("playlistId") ?: return@composable
                com.turkcell.lyraapp.ui.playlist.detail.PlaylistDetailRoute(
                    playlistId = playlistId,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToPlayer = { songId ->
                        navController.navigate(LyraDestination.nowPlayingRoute(songId)) {
                            launchSingleTop = true
                        }
                    }
                )
            }
            
            composable(LyraDestination.PremiumPlans.route) {
                PremiumPlansRoute(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToPayment = { planId ->
                        navController.navigate(LyraDestination.paymentRoute(planId)) {
                            launchSingleTop = true
                        }
                    }
                )
            }
            
            composable(
                route = LyraDestination.Payment.route,
                arguments = listOf(
                    navArgument("planId") { type = NavType.StringType }
                ),
            ) { backStackEntry ->
                val planId = backStackEntry.arguments?.getString("planId") ?: return@composable
                PaymentRoute(
                    planId = planId,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToHome = { navController.navigateToHomeClearingAuth() } // Payment success!
                )
            }
        }
    }
}

/**
 * Alt çubuk sekmesine standart desenle geçiş yapar: back stack'te sekme kopyası birikmez
 * (`launchSingleTop`), sekmeler arası geçişte durum saklanır/geri yüklenir
 * (`saveState`/`restoreState`) ve geri tuşu daima Home'a döner (`popUpTo(Home)`).
 */
private fun NavHostController.navigateToTab(destination: LyraDestination) {
    navigate(destination.route) {
        popUpTo(LyraDestination.Home.route) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

/** Auth akışını back stack'ten temizleyerek Home'a geçer (geri tuşu Phone'a dönmez). */
private fun NavHostController.navigateToHomeClearingAuth() {
    navigate(LyraDestination.Home.route) {
        popUpTo(LyraDestination.Phone.route) { inclusive = true }
        launchSingleTop = true
    }
}
