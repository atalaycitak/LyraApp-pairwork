# decisions.md

> Projede verilen bütün mimarisel-teknik kararları ve karar geçmişini içeren dökümantasyondur.

---

### Dependency Injection Kütüphanesi

- Seçim*: **Hilt**

- Son Güncelleme Tarihi*: 04.06.2026

- Alternatifler: **Koin**

- Sebep: **Opsiyonel**


### Navigasyon

- Seçim: **Compose Navigation**

- Son Güncelleme Tarihi: 09.06.2026

- Bağımlılık: `androidx.navigation:navigation-compose` **2.9.5** (version catalog: `navigationCompose`).

- Uygulama: Tek `NavHost` (`ui/navigation/LyraNavHost.kt`) Auth grafiğini barındırır (başlangıç
  hedefi Login). Navigasyon MVI ile uyumlu kurulur: ViewModel'de navigasyon API'si yoktur
  (bkz. [architecture/mvi-viewmodel-rules.md](architecture/mvi-viewmodel-rules.md) §6); navigasyon
  `Intent → Effect` üzerinden akar, `Route` Effect'i tüketip `NavHost`'tan gelen lambda'ları çağırır.


### Sunum Katmanı Mimarisi

- Seçim: **MVI (Model-View-Intent)**

- Son Güncelleme Tarihi: 09.06.2026

- Kapsam: Her ekran State + Intent + Effect sözleşmesiyle yazılır. Detaylı kurallar ve
  referans implementasyon (Login) için bkz. [architecture/mvi-overview.md](architecture/mvi-overview.md).

- Sebep: Tek yönlü veri akışı, durumsuz UI, test edilebilirlik.


### Hilt Annotation Processing

- Seçim: **KSP** (kapt değil)

- Son Güncelleme Tarihi: 09.06.2026

- Sürümler: Hilt **2.59.2**, KSP **2.2.10-2.0.2** (Kotlin 2.2.10 ile birebir uyumlu).

- Compose'da ViewModel: `androidx.hilt:hilt-lifecycle-viewmodel-compose` (`hiltViewModel()`).
  Compose Navigation henüz kurulmadığından navigation-compose bağımlılığı eklenmemiştir.

- Sebep: KSP, kapt'a göre belirgin biçimde hızlıdır ve Kotlin 2.2 ile uyumludur.


### AGP 9 Built-in Kotlin + KSP Uyumu

- Karar: `gradle.properties` içinde **`android.disallowKotlinSourceSets=false`** zorunludur.

- Son Güncelleme Tarihi: 09.06.2026

- Sebep: AGP 9 built-in Kotlin kullanır; KSP'nin ürettiği kaynak dizinlerini eklemesi bu bayrak
  olmadan derlemeyi kırar. Bayrak deneysel (experimental) olarak işaretlidir ancak gereklidir.


### Alt Gezinme Çubuğu (Bottom Navigation Bar)

- Seçim: **Material 3 `NavigationBar`** — tek `NavHost` + iskelet seviyesinde tek dış `Scaffold`.

- Son Güncelleme Tarihi: 11.06.2026

- Uygulama: `ui/navigation/LyraBottomBar.kt` (bileşen + `LyraBottomBarTab` sekme tanımları) ve
  `ui/navigation/LyraNavHost.kt` (Scaffold `bottomBar` entegrasyonu). Çubuk yalnızca üst düzey
  sekme rotalarında görünür (Auth ekranlarında gizli); böylece her ana sayfanın altında otomatik
  yer alır. Sekme geçişi standart desenle yapılır: `popUpTo(Home) { saveState = true }` +
  `launchSingleTop` + `restoreState`. Dış Scaffold'ın `contentWindowInsets`'i sıfırdır; sistem
  çubuğu boşluklarını ekranlar kendisi yönetir, içerik dolgusu yalnızca alt çubuk yüksekliğini taşır.

- MVI kapsamı: BNB navigasyon iskeletidir (chrome), feature ekranı değildir; State/Intent/Effect
  sözleşmesi yoktur. Seçili sekme `currentBackStackEntryAsState()` ile nav back stack'ten türetilir
  (tek doğruluk kaynağı back stack'tir). Sekme ekranları MVI ile yazıldığında yalnızca
  `LyraNavHost` içindeki geçici placeholder rotaları gerçek `Route`'lara bağlanacaktır.

- Sebep: Tek doğruluk kaynağı (back stack) ile durum tekrarına yer bırakmaz; sekme başına ayrı
  `NavHost`/ViewModel karmaşıklığından kaçınılır; mevcut Auth grafiği değişmeden korunur.


### Backend Hazır Değilken Veri Katmanı

- Karar: **Stub repository** deseni — Repository interface + `Fake<X>Repository` implementasyonu.

- Son Güncelleme Tarihi: 09.06.2026

- Sebep: Backend REST API sözleşmesi tanımlı değil (`agents.md` §2.2 uydurmak yasak). Gerçek API
  geldiğinde yalnızca implementasyon ve DI bağlaması değişir; ViewModel/Contract etkilenmez.


### Arama (Search) Ekranı

- Seçim: **MVI** — `SearchContract.kt` (State + Intent + Effect), `SearchViewModel.kt`,
  `SearchScreen.kt` (Route/Screen ayrımı), `SearchRepository` interface + `MockSearchRepository`.

- Son Güncelleme Tarihi: 13.06.2026

- Uygulama: `ui/search/` paketi Login/Home referans implementasyonlarıyla aynı MVI desenini izler.
  Arama alanı (`OutlinedTextField`), yatay scrollable filtre çipleri (`FilterChip`) ve 2 sütunlu
  tür kartları grid'i (`Genre` modeli, gradyan arka plan) ekran görüntüsüne birebir uygun
  olarak tasarlanmıştır. Veri katmanı `data/search/` altında, DI bağlaması `di/SearchModule.kt`
  içinde yer alır. Navigasyonda `LyraNavHost` içindeki geçici `PlaceholderScreen` kaldırılarak
  `SearchRoute()` bağlanmıştır.

- Sebep: Tasarım ekran görüntüsüne uyum; mevcut MVI mimarisi ve stub repository deseniyle tutarlılık.


### Now Playing (Şimdi Çalıyor) Ekranı

- Seçim: **MVI** — `NowPlayingContract.kt` (State + Intent + Effect), `NowPlayingViewModel.kt`,
  `NowPlayingScreen.kt` (Route/Screen ayrımı).

- Son Güncelleme Tarihi: 18.06.2026

- Uygulama: `ui/nowplaying/` paketi Login/Home referans implementasyonlarıyla aynı MVI desenini
  izler. `NowPlayingViewModel`, `SongRepository` (Retrofit) üzerinden doğrudan şarkı metadatasını
  ve stream URL'ini çeker; ExoPlayer ile ses akışı sağlar. Progress takibi ExoPlayer
  `Player.Listener` üzerinden 500ms polling ile yapılır. ExoPlayer `NowPlayingModule`'de
  `@ApplicationContext` ile `@Provides @Singleton` olarak sağlanır. `LyraDestination.NowPlaying`
  üst düzey sekme değildir; alt gezinme çubuğu bu ekranda gizlenir.

- 18.06.2026 güncellemesi: ExoPlayer entegrasyonu sonrası `NowPlayingRepository` interface,
  `MockNowPlayingRepository` ve `NowPlayingModels.kt` (Track, NowPlayingInfo) artık ViewModel
  tarafından kullanılmadığından kaldırıldı. `NowPlayingModule` yalnızca ExoPlayer provider'ını
  içerecek şekilde sadeleştirildi.

- Sebep: Tasarım ekran görüntüsüne uyum; ExoPlayer ile gerçek ses akışı.


### Kütüphane (Library) Ekranı

- Seçim: **MVI** — `LibraryContract.kt` (State + Intent + Effect), `LibraryViewModel.kt`,
  `LibraryScreen.kt` (Route/Screen ayrımı), `LibraryRepository` interface +
  `RetrofitLibraryRepository`.

- Son Güncelleme Tarihi: 19.06.2026

- Uygulama: `ui/library/` paketi mevcut Login/Home/Search referans implementasyonlarıyla aynı
  MVI desenini izler. Ekran başlık, hızlı kütüphane aksiyonları, filtre çipleri ve API'dan gelen
  şarkı listesinden oluşur. Veri katmanı `data/library/` altında, DI bağlaması
  `di/LibraryModule.kt` içinde yer alır. `RetrofitLibraryRepository`, `SongRepository.getSongs`
  üzerinden `/api/v1/songs` verisini çeker ve `LibraryItem` modeline dönüştürür. API'da artwork
  veya background alanı olmadığı için `ArtworkPalette.colorPairForId(song.id)` ile renk çifti
  üretilir. Şarkı item'ına tıklanınca gerçek `songId` ile `NowPlaying` ekranına gidilir.

- Sebep: API sözleşmesinde Library için ayrı endpoint yoktur; mevcut `/api/v1/songs` tasarımı
  esas alınarak Library ekranındaki müzik listesi mock veriden gerçek API verisine taşınmıştır.


### Songs REST API Entegrasyonu (Retrofit)

- Seçim: **Retrofit 2.11.0** + **OkHttp logging-interceptor 4.12.0** + **Gson Converter**

- Son Güncelleme Tarihi: 16.06.2026

- Bağımlılık: `com.squareup.retrofit2:retrofit` + `converter-gson` **2.11.0**;
  `com.squareup.okhttp3:logging-interceptor` **4.12.0** (version catalog: `retrofit`, `okhttpLogging`).

- Uygulama: `di/NetworkModule.kt` Retrofit singleton + `SongApiService` sağlar. Base URL canlı
  ortam sunucusudur (`https://streaming-api.halitkalayci.com/`). `data/song/` paketine
  `SongModels.kt`, `SongApiService.kt`, `SongRepository.kt`, `RetrofitSongRepository.kt` eklendi;
  `di/SongModule.kt` bağlamayı yapar. `data/home/RetrofitHomeRepository.kt`, `SongRepository`
  üzerinden şarkı listesini çekip `HomeFeed`'e dönüştürür; `di/HomeModule.kt`'de bağlama hedefi
  `MockHomeRepository` → `RetrofitHomeRepository` olarak değiştirildi.

- Kapak Rengi Stratejisi: API'da `background`/kapak görseli alanı bulunmadığından her şarkı
  için `abs(id.hashCode()) % paletteSize` formülüyle deterministik renk çifti üretilir.
  Aynı ID her zaman aynı rengi verir; Home ve NowPlaying ekranları aynı paleti kullanır.

- Sebep: Backend API sözleşmesi yayınlandı; stub repository deseni gerçek implementasyonla
  değiştirildi. ViewModel ve Contract değişmedi (mvi-overview.md §6 prensibiyle uyumlu).


### ExoPlayer Ses Akışı

- Seçim: **androidx.media3:media3-exoplayer 1.6.1**

- Son Güncelleme Tarihi: 16.06.2026

- Bağımlılık: `androidx.media3:media3-exoplayer` **1.6.1** (version catalog: `media3`).

- Uygulama: `NowPlayingViewModel` artık `SavedStateHandle` üzerinden `songId` alır.
  Yükleme adımları: (1) `SongRepository.getSongById(songId)` → metadata, (2)
  `SongRepository.getStreamUrl(songId)` → 300 saniyelik imzalı URL → `ExoPlayer.setMediaItem()`.
  ExoPlayer, `NowPlayingModule`'de `@ApplicationContext` ile `@Provides @Singleton` olarak
  sağlanır; ViewModel doğrudan `Context` tutmaz (mvi-viewmodel-rules.md §3.6 uyumlu).
  `NowPlayingDestination` rotası `"now_playing/{songId}"` path parametresine güncellendi;
  `LyraDestination.nowPlayingRoute(songId)` yardımcı fonksiyonu eklendi.

- Kapsam: Bu iterasyonda progress bar simülasyonu kaldırıldı; ExoPlayer'ın gerçek pozisyon
  takibi (Player.Listener) bir sonraki iterasyona bırakıldı.

- Sebep: API stream URL'i artık mevcut; ExoPlayer HTTP Range desteğiyle imzalı URL'den
  doğrudan ses akışı sağlar (openapi.json /api/v1/stream/{songId} tasarımıyla uyumlu).

### Favoriler (Favorites) Ekranı

- Seçim: **MVI** - `FavoritesContract.kt` (State + Intent + Effect), `FavoritesViewModel.kt`,
  `FavoritesScreen.kt` (Route/Screen ayrımı), `FavoritesRepository` interface +
  `MockFavoritesRepository`.

- Son Güncelleme Tarihi: 13.06.2026

- Uygulama: `ui/favorites/` paketi mevcut Login/Home/Search/Library referans
  implementasyonlarıyla aynı MVI desenini izler. Ekran başlık, favori sayısı, filtre çipleri,
  favori içerik listesi, favoriden kaldırma aksiyonu ve boş liste durumundan oluşur. Veri katmanı
  `data/favorites/` altında, DI bağlaması `di/FavoritesModule.kt` içinde yer alır. Navigasyonda
  `LyraNavHost` içindeki Favorites placeholder'ı kaldırılarak `FavoritesRoute()` bağlanmıştır.

- Sebep: Mevcut alt gezinme çubuğundaki Favorites sekmesini gerçek MVI ekranına bağlamak;
  backend hazır olana kadar stub repository deseniyle geliştirmeyi sürdürmek.


### Ölü Kod Temizliği ve MVI Sapması Düzenlemesi

- Seçim: NowPlayingRepository/MockNowPlayingRepository/NowPlayingModels kaldırıldı;
  PlaylistDetailViewModel'de `MutableSharedFlow` → `Channel` geçişi yapıldı.

- Son Güncelleme Tarihi: 18.06.2026

- Uygulama:
  1. `data/nowplaying/` altındaki `NowPlayingRepository.kt`, `MockNowPlayingRepository.kt`,
     `NowPlayingModels.kt` silindi. `di/NowPlayingModule.kt`'deki ilgili `@Binds` binding'i
     kaldırıldı; modül `abstract class` → `object` dönüştürüldü.
  2. `PlaylistDetailViewModel.kt`'de `MutableSharedFlow` + `asSharedFlow()` kaldırılarak
     MVI standardı olan `Channel(Channel.BUFFERED)` + `receiveAsFlow()` kullanıma alındı.

- Sebep: ExoPlayer entegrasyonu sonrası NowPlayingRepository kullanılmıyordu (ölü kod).
  MutableSharedFlow tek seferlik olaylar için konfigürasyon değişiminde tekrar tetiklenme
  riski taşır; Channel ise mvi-contracts.md §4 kuralına uygundur.


### Kullanıcı Arayüzü Metin Dili

- Seçim: Uygulama içi kullanıcıya görünen metinler Türkçe yazılır ve Türkçe karakterler
  (`ç`, `ğ`, `ı`, `İ`, `ö`, `ş`, `ü`) korunur.

- Son Güncelleme Tarihi: 13.06.2026

- Kapsam: Compose ekran metinleri, snackbar/hata mesajları, erişilebilirlik açıklamaları,
  preview/mock veri başlıkları ve takım dokümantasyonu.

- Sebep: Uygulamanın hedef kullanıcı kitlesi Türk kullanıcılar olduğu için arayüz dilinin doğal,
  tutarlı ve okunabilir olması gerekir.
