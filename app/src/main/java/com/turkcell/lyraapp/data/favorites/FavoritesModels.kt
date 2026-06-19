package com.turkcell.lyraapp.data.favorites

/**
 * Favorites ekraninin repository'den aldigi aggregate model.
 *
 * API'da favoriler ve artwork icin ayri alanlar henuz bulunmadigindan feed, `/api/v1/songs`
 * yanitindan turetilir; kapaklar mevcut feature'larda oldugu gibi ARGB renk ciftleriyle
 * temsil edilir. Gercek favorites/artwork API'lari geldiginde model genisletilebilir.
 */
data class FavoritesFeed(
    val filters: List<FavoriteFilter>,
    val items: List<FavoriteItem>,
)

/** Favori listesini daraltmak icin kullanilan filtre cipi. */
data class FavoriteFilter(
    val id: String,
    val label: String,
    val type: FavoriteItemType?,
)

/** Favorites ekraninda gosterilen kayitli favori icerik. */
data class FavoriteItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val type: FavoriteItemType,
    val artworkStartColor: Long,
    val artworkEndColor: Long,
    val durationLabel: String,
    val isDownloaded: Boolean,
)

enum class FavoriteItemType {
    Song,
    Album,
    Playlist,
}
