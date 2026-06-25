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
  `SearchScreen.kt` (Route/Screen ayrımı), `SearchRepository` interface +
  `RetrofitSearchRepository`.

- Son Güncelleme Tarihi: 20.06.2026

- Uygulama: `ui/search/` paketi Login/Home referans implementasyonlarıyla aynı MVI desenini izler.
  Arama alanı (`OutlinedTextField`), yatay filtre çipleri (`FilterChip`) ve API'dan gelen şarkı
  sonuçları listesi bulunur. Veri katmanı `data/search/` altında, DI bağlaması
  `di/SearchModule.kt` içinde yer alır. `RetrofitSearchRepository`, `/api/v1/songs` endpoint'inin
  `q` parametresini kullanarak şarkı araması yapar. API'da genre endpoint'i olmadığı için sahte
  tür kartları üretilmez. Sonuca tıklanınca gerçek `Song.id` ile Now Playing ekranına gidilir.

- Sebep: API sözleşmesinde arama `/api/v1/songs?q=...` üzerinden sağlandığı için Search ekranını
  mock genre verisi yerine gerçek şarkı arama akışına taşımak.


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


### Ana Sayfa Playlist API Entegrasyonu

- Seçim: **Home feed içinde `/api/v1/playlists` kullanımı** - `RetrofitHomeRepository`,
  mevcut `PlaylistApiService` üzerinden çalma listelerini alır.

- Son Güncelleme Tarihi: 20.06.2026

- Uygulama: Home ekranındaki "Senin için çalma listeleri" satırı artık boş liste dönmez;
  OpenAPI sözleşmesindeki `GET /api/v1/playlists` yanıtı `PlaylistForYou` modeline map edilir.
  API'da playlist artwork/background alanı olmadığı için `ArtworkPalette.colorPairForId(playlist.id)`
  ile deterministik renk çifti üretilir. Playlist kartına tıklanınca MVI akışı
  `HomeIntent.PlaylistClicked -> HomeEffect.NavigateToPlaylistDetail` üzerinden
  `playlist_detail/{playlistId}` rotasına gider.

- Sebep: Home ekranındaki playlist alanını mock/boş durumdan çıkarıp yayınlanan backend
  sözleşmesine bağlamak; kullanıcıyı gerçek playlist detay akışına taşımak.


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
  `RetrofitFavoritesRepository`.

- Son Güncelleme Tarihi: 19.06.2026

- Uygulama: `ui/favorites/` paketi mevcut Login/Home/Search/Library referans
  implementasyonlarıyla aynı MVI desenini izler. Ekran başlık, favori sayısı, filtre çipleri,
  favori içerik listesi, favoriden kaldırma aksiyonu ve boş liste durumundan oluşur. Veri katmanı
  `data/favorites/` altında, DI bağlaması `di/FavoritesModule.kt` içinde yer alır.
  `RetrofitFavoritesRepository`, API'da favoriler için ayrı endpoint olmadığı için
  `SongRepository.getSongs` üzerinden `/api/v1/songs` verisini alır ve `FavoriteItem` modeline
  dönüştürür. Şarkı item'larının ID değeri gerçek `Song.id` olduğu için Now Playing/ExoPlayer
  akışına uyumludur. Favoriden kaldırma davranışı backend endpoint'i gelene kadar ekranda lokal
  kalır.

- Sebep: Mevcut alt gezinme çubuğundaki Favorites sekmesini gerçek MVI ekranına bağlamak;
  mevcut API tasarımında favori endpoint'i olmadığı için şarkı listesinden API uyumlu favori feed'i
  türetmek ve player'a gönderilen ID'lerin gerçek API song ID'si olmasını sağlamak.


### Çalma Listeleri (Playlist) API Entegrasyonu

- Seçim: **Retrofit tabanlı playlist repository** - `PlaylistApiService`, `PlaylistApiModels.kt`,
  `RetrofitPlaylistRepository`.

- Son Güncelleme Tarihi: 20.06.2026

- Uygulama: `PlaylistApiService`, OpenAPI sözleşmesindeki `GET /api/v1/playlists` ve
  `GET /api/v1/playlists/{id}` endpointlerini tanımlar. `RetrofitPlaylistRepository`,
  `getPlaylistDetail()` çağrısında API'dan gelen playlist ve sıralı şarkı listesini
  `PlaylistDetailModel` içine map eder. Detay ekranındaki şarkı ID'leri gerçek `Song.id`
  değeridir; bu sayede şarkıya tıklanınca Now Playing/ExoPlayer akışına doğru ID gider.
  `getAvailableSongs()` create playlist ekranı için `SongRepository.getSongs()` ile API şarkılarını
  getirir. API'da playlist oluşturma endpoint'i olmadığı için `createPlaylist()` yalnızca lokal
  validasyon ve başarılı dönüş davranışı sağlar.

- Sebep: API'da playlist okuma endpointleri hazırdır; detay ekranını mock veriden gerçek playlist
  verisine taşımak ve create playlist ekranındaki şarkı seçimlerini gerçek API song ID'leriyle
  uyumlu hale getirmek.


### Bildirimler (Notifications) Ekranı

- Seçim: **MVI** - `NotificationsContract.kt` (State + Intent + Effect),
  `NotificationsViewModel.kt`, `NotificationsScreen.kt` (Route/Screen ayrımı),
  `NotificationsRepository` interface + `MockNotificationsRepository`.

- Son Güncelleme Tarihi: 19.06.2026

- Uygulama: `ui/notifications/` paketi mevcut MVI feature desenini izler. Ekran Profile
  menüsündeki `Bildirimler` satırından açılır; `ProfileViewModel`, snackbar yerine
  `ProfileEffect.NavigateToNotifications` effect'i üretir ve `LyraNavHost` bu effect'i
  `notifications` rotasına bağlar. Bildirim tercihleri switch kontrolleriyle yönetilir.
  API sözleşmesinde bildirim endpoint'i bulunmadığı için veri katmanı `data/notifications/`
  altında local mock repository ile sağlanır.

- Sebep: Profile ekranındaki bildirim girişini placeholder/snackbar davranışından gerçek ayar
  ekranına taşımak; backend desteği gelene kadar MVI ve repository soyutlamasını korumak.


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


### Arka Plan Müzik Oynatma (Background Playback)

- Seçim: **androidx.media3 MediaSessionService** — `PlaybackService` sınıfı
  `MediaSessionService`'ten türer; Singleton `ExoPlayer`'ı Hilt ile alır ve `MediaSession`
  oluşturarak arka plan oynatımı + sistem bildirim kontrollerini sağlar.

- Son Güncelleme Tarihi: 25.06.2026

- Bağımlılık: `androidx.media3:media3-session` **1.6.1** (version catalog: `media3`).

- Uygulama: `NowPlayingModule`'deki `provideExoPlayer()` fonksiyonu `AudioAttributes`
  (CONTENT_TYPE_MUSIC, USAGE_MEDIA), `handleAudioBecomingNoisy(true)` ve
  `setWakeMode(C.WAKE_MODE_NETWORK)` ile yapılandırıldı. `PlaybackService`
  (`data/player/PlaybackService.kt`) `@AndroidEntryPoint` ile işaretlenip `ExoPlayer`
  singleton'ını enjekte eder; `onCreate`'de `MediaSession` oluşturur, `onDestroy`'da serbest
  bırakır. `AndroidManifest.xml`'e `FOREGROUND_SERVICE` ve
  `FOREGROUND_SERVICE_MEDIA_PLAYBACK` izinleri ile `<service>` tanımı eklendi.

- Sebep: Android, Activity arka plana geçtiğinde sesi kesmektedir. `MediaSessionService`
  foreground service olarak çalışarak müziğin kesintisiz devam etmesini ve sistem bildirim
  panelinden kontrol edilebilmesini sağlar. Media3'ün kendi `MediaSessionService`
  implementasyonu bildirimi otomatik yönettiği için ek `NotificationCompat` kodu gerekmez.


### Offline Müzik İndirme

- Seçim: **Room Veritabanı + OkHttp Download + Yerel ExoPlayer Oynatımı**

- Son Güncelleme Tarihi: 25.06.2026

- Bağımlılık: `androidx.room:room-runtime`, `room-ktx`, `room-compiler` **2.6.1**

- Uygulama: `SongDownloadManager` üzerinden OkHttp kullanılarak `streamUrl` byte bazında çekilip
  cihazın yerel diskine kaydedilir. İndirilen şarkının metadata ve dosya yolu **Room** `DownloadedSongEntity`
  ile kalıcı bellekte saklanır. `AudioPlayerManager.playSong()` metodu, ağ üzerinden URL çekmeden önce
  Room veritabanını kontrol eder; eğer dosya varsa `MediaItem.fromUri(localFileUri)` kullanarak
  offline akış başlatır.
  
- Sebep: NowPlaying ekranından indirilen şarkıların internet bağlantısı olmadan da dinlenebilmesi.

### Kişisel Çalma Listesi Yönetimi (API Entegrasyonu)

- Seçim: **Retrofit `POST/DELETE` API Uçlarının Kullanımı** — Çalma listesi oluşturma ve düzenleme işlemleri gerçek API üzerinden yapılır.

- Son Güncelleme Tarihi: 25.06.2026

- Uygulama: `PlaylistApiService` içine `/api/v1/me/playlists` endpoint'leri (`GET`, `POST` playlist ve `POST/DELETE` tracks) eklendi. `CreatePlaylistViewModel`, sahte geri dönüş yerine `PlaylistRepository.createPlaylist` çağrısıyla API'de çalma listesini oluşturup ardından `addTrackToPlaylist` iterasyonlarıyla seçili şarkıları bağlar. `LibraryScreen`'deki `RetrofitLibraryRepository`, bu uçları çağırarak "Kendi Kütüphanem" feed'ine API'dan gelen kullanıcıya ait `PlaylistSummaryModel`'leri ekler. `PlaylistDetailScreen`'e eklenen `DropdownMenu` ile listelerde şarkı çıkarma (`removeSongFromPlaylist`) olanağı sağlanır.

- Sebep: Kütüphanede rastgele simüle edilen sahte listeler ve oluşturulamayan playlist özelliklerinin backend sözleşmesiyle çalışan, tam yetkili (gerçek veri + manipülasyon) hale getirilmesi.

### Oynatma Geçmişi (Plays)

- Seçim: **`POST /api/v1/me/plays` ile şarkı başladığı an backend'e bildirim.**

- Son Güncelleme Tarihi: 25.06.2026

- Uygulama: `HomeApiService` içerisine `recordPlay` endpoint'i ve modeli eklendi. `HomeRepository` bu ucu sarmalayacak şekilde güncellendi. Uygulamanın merkezi oynatma denetleyicisi olan `AudioPlayerManager`, Hilt üzerinden `HomeRepository` bağımlılığını alacak şekilde güncellendi. Yeni mimaride `playSong(songId)` metodu, şarkı başarıyla yüklenip `player.play()` çağrıldıktan hemen sonra arka planda fire-and-forget olarak `homeRepository.recordPlay(songId)` fonksiyonunu tetikler.

- Sebep: Kullanıcının dinlediği şarkıların backend tarafındaki "Son Çalınanlar" (`recently-played`) listesini besleyebilmesi ve kişisel öneri algoritmalarının dinamik olarak güncellenmesi.

### Kullanıcı Profili (Me) ve Güncelleme

- Seçim: **`ProfileApiService` oluşturulup, `GET /api/v1/me` ve `POST /api/v1/me/update-informations` uçlarının buraya taşınması.**

- Son Güncelleme Tarihi: 25.06.2026

- Uygulama: `AuthApiService` içerisindeki `updateProfile` ucu mantıksal bir ayrıştırma ile yeni oluşturulan `ProfileApiService` dosyasına taşındı. `ProfileScreen` ve `ProfileViewModel`, MVI mimarisine sadık kalınarak profil bilgilerini `GET /me` üzerinden alacak şekilde bağlandı. `UserProfile` domain modeline gerçek `phone` ve `firstName`/`lastName` gibi alanlar eklendi. Ayrıca UI katmanına kullanıcının bilgilerini düzenleyebileceği bir **Düzenle** butonu (Edit) ve güncellemelerin yapılabildiği bir **EditProfileDialog** entegre edildi.

- Sebep: Profil sayfasındaki tüm bilgilerin sahte (mock) olması engelini aşarak backend ile tam entegre (read & write) profil yönetimi deneyimini sağlamak.

### Token Yenileme (Authenticator) Mekanizması

- Seçim: **`TokenAuthenticator` sınıfının oluşturularak OkHttp instance'ına bağlanması.**

- Son Güncelleme Tarihi: 25.06.2026

- Uygulama: `okhttp3.Authenticator` arayüzünü uygulayan bir `TokenAuthenticator` sınıfı oluşturuldu. Bu sınıf içerisinde Dagger'ın `Provider<AuthApiService>` yapısı kullanılarak OkHttp ve Retrofit arasındaki döngüsel bağımlılık (circular dependency) kırıldı. Ağ isteklerinde alınan `401 Unauthorized` hatalarında bu sınıf devreye girer; mevcut isteği bekletir, `runBlocking` ile senkron şekilde `/api/v1/auth/refresh` ucuna `RefreshTokenRequest` gönderir. Yenileme başarılı olursa `TokenManager` üzerinden yeni token'ları kaydeder ve orijinal isteği yeni token ile yenileyerek tekrar dener. Başarısız olursa yerel token'ları temizler.

- Sebep: Kısa ömürlü (short-lived) Access Token'ların süresi dolduğunda uygulamanın çökmeyip veya API bazlı hatalar göstermeyip, arkada sessizce token yenilemesi ve kullanıcının oturumunun kesintiye uğramaması (Seamless Session Management).

### Arka Planda Çalma (Background Playback) Optimizasyonu

- Seçim: **`AudioPlayerManager` içerisinde doğrudan `ExoPlayer` yerine `MediaController` kullanımı.**

- Son Güncelleme Tarihi: 25.06.2026

- Uygulama: Önceden `AudioPlayerManager` doğrudan singleton olarak enjekte edilmiş `ExoPlayer` nesnesini yönetiyordu. Ancak Media3 mimarisinde, Android sisteminin uygulamanın arkaplanda (Foreground Service) müzik çaldığını algılayabilmesi için `PlaybackService`'e (MediaSessionService) bir denetleyici ile bağlanılması zorunludur. `AudioPlayerManager` yeniden yazılarak, başlatıldığı anda `MediaController.Builder().buildAsync()` kullanılıp `PlaybackService`'e bağlanan bir denetleyiciye dönüştürüldü. Ayrıca asenkron başlatmayı güvenli hale getirmek için `scope.launch` içerisinde controller yüklenene kadar bekleyen korumalar (safeguard) eklendi.

- Sebep: Uygulama arka plana atıldığında (veya kilit ekranındayken) Android OS'in `PlaybackService`'i öldürmesini engellemek, bildirim çubuğunda şarkı kontrollerinin (Medya Kontrol Paneli) doğru şekilde kalmasını ve sistemle entegre çalışmasını garantilemek.
