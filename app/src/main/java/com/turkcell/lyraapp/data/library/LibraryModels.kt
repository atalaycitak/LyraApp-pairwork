package com.turkcell.lyraapp.data.library

/**
 * Kutuphane ekraninin repository'den tek seferde aldigi aggregate model.
 *
 * Kapak gorselleri henuz CDN/API ile saglanmadigi icin Home ve Search feature'larinda oldugu
 * gibi ARGB renk ciftleriyle temsil edilir. Gercek API geldiginde model gorsel URL'siyle
 * genisletilebilir; UI katmani yalnizca bu veriyi cizer.
 */
data class LibraryFeed(
    val filters: List<LibraryFilter>,
    val quickActions: List<LibraryQuickAction>,
    val items: List<LibraryItem>,
)

/** Kutuphane listesini daraltmak icin kullanilan filtre cipi. */
data class LibraryFilter(
    val id: String,
    val label: String,
    val type: LibraryItemType?,
)

/** Ekranin ust bolumundeki hizli kutuphane aksiyonlari. */
data class LibraryQuickAction(
    val id: String,
    val title: String,
    val subtitle: String,
    val type: LibraryQuickActionType,
)

/** Kutuphanede gosterilen kayitli icerik. */
data class LibraryItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val type: LibraryItemType,
    val artworkStartColor: Long,
    val artworkEndColor: Long,
    val isDownloaded: Boolean,
)

enum class LibraryItemType {
    Playlist,
    Album,
    Artist,
}

enum class LibraryQuickActionType {
    CreatePlaylist,
    Downloads,
    LikedSongs,
}
