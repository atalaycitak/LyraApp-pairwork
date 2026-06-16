package com.turkcell.lyraapp.data.favorites

/**
 * Favorites ekraninin repository'den aldigi aggregate model.
 *
 * Backend ve gercek artwork henuz hazir olmadigindan kapaklar mevcut feature'larda oldugu gibi
 * ARGB renk ciftleriyle temsil edilir. Gercek API geldiginde model image URL ile genisletilebilir.
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
