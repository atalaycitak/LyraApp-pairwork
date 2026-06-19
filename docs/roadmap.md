# LyraApp Yol Haritası

> Bu doküman pair çalışma reposu için mevcut durum, tamamlanan işler ve sonraki geliştirme
> adımlarını özetler. Kaynaklar: `AGENTS.md`, `docs/decisions.md`, `docs/api/openapi.json`,
> mevcut MVI feature paketleri ve tasarım referanslarıdır.

---

## 1. Mevcut Durum Özeti

Proje Android Jetpack Compose ve Kotlin ile geliştirilen online/offline müzik çalar
uygulamasıdır. Sunum katmanı MVI mimarisiyle ilerler. Backend streaming API sözleşmesi
`docs/api/openapi.json` altında projeye eklenmiştir.

Mevcut build durumu:

- Pair repo local klasörü: `C:\Users\zzeyn\Documents\LyraApp-pairwork`
- Remote repo: `atalaycitak/LyraApp-pairwork`
- Ana branch: `main`
- Son doğrulanan main commit: `67be5ab`
- Build komutu: `.\gradlew.bat build --console=plain --stacktrace`

---

## 2. Tamamlanan Kapsam

### 2.1. Mimari ve Proje Temeli

- Gradle/Android proje iskeleti kuruldu.
- Hilt dependency injection altyapısı eklendi.
- KSP ile Hilt annotation processing yapılandırıldı.
- Compose Navigation ile tek `NavHost` yapısı kuruldu.
- MVI mimari dokümanları eklendi:
  - `docs/architecture/mvi-overview.md`
  - `docs/architecture/mvi-contracts.md`
  - `docs/architecture/mvi-viewmodel-rules.md`
- Mimari kararlar `docs/decisions.md` altında tutuluyor.

### 2.2. Tasarım Sistemi ve Navigasyon

- Renk sistemi, tipografi sistemi ve `LyraAppTheme` eklendi.
- Proje içi `LyraIcons` seti oluşturuldu.
- Bottom navigation bar eklendi.
- Global mini player eklendi.
- `Home`, `Search`, `Library`, `Favorites`, `Profile`, `Notifications`, `NowPlaying`,
  `CreatePlaylist` ve `PlaylistDetail` rotaları bağlandı.

### 2.3. Tamamlanan Ekranlar

- Login ekranı MVI ile eklendi.
- Register ekranı MVI ile eklendi.
- Home ekranı MVI ile eklendi ve şarkı listesi API verisine bağlandı.
- Search ekranı MVI ile eklendi.
- Library ekranı MVI ile eklendi.
- Favorites ekranı MVI ile eklendi.
- Profile ekranı MVI ile eklendi.
- Bildirim ayarları ekranı MVI ile eklendi ve Profile menüsüne bağlandı.
- Now Playing ekranı MVI ile eklendi ve ExoPlayer akışına bağlandı.
- Yeni çalma listesi oluşturma ekranı MVI ile eklendi.
- Playlist detay ekranı MVI ile eklendi.

### 2.4. API ve Player Entegrasyonu

- `docs/api/openapi.json` projeye eklendi.
- `docs/api/api-overview.md` ile API özeti oluşturuldu.
- Retrofit + Gson + OkHttp logging altyapısı eklendi.
- `SongRepository`, `SongApiService` ve `RetrofitSongRepository` eklendi.
- Home ekranı `/api/v1/songs` üzerinden gerçek şarkı listesi alıyor.
- Library ekranı `/api/v1/songs` üzerinden gerçek şarkı listesi alıyor.
- Şarkı tıklanınca `now_playing/{songId}` rotasına gidiliyor.
- `AudioPlayerManager` ve `PlayerController` ile global player katmanı eklendi.
- ExoPlayer, `/api/v1/songs/{id}/stream-url` üzerinden alınan imzalı URL ile ses çalıyor.
- API'da artwork/background alanı olmadığı için `ArtworkPalette` ile ID tabanlı renk çifti üretiliyor.

### 2.5. Test Kapsamı

- Playlist detail ViewModel testi eklendi.
- Profile ViewModel testi eklendi.

---

## 3. Eksik Kapsam

### 3.1. API'ye Geçmemiş Ekranlar

- Favorites ekranı hâlâ `MockFavoritesRepository` kullanıyor.
- Search ekranı hâlâ mock repository deseniyle çalışıyor.
- Bildirimler ekranı API'da endpoint olmadığı için mock/local repository kullanıyor.
- Playlist listesi/detayı için API endpointleri mevcut olsa da uygulamadaki playlist repository katmanı
  henüz API tasarımına tam taşınmadı.

### 3.2. Favorites API Uyumluluğu

- Favorites içindeki bazı mock item ID'leri gerçek API song ID formatıyla uyumlu değildir.
- Şarkı item'ı player'a gönderilecekse ID gerçek API'daki `Song.id` olmalıdır.
- API'da favori ekleme/silme endpoint'i olmadığı için favori kalıcılığı hâlâ gerçek backend davranışı değildir.

### 3.3. Player Kapsamı

- Temel ExoPlayer çalma, duraklatma ve seek akışı mevcuttur.
- Kuyruk yönetimi ve sonraki şarkıya geçiş gerçek playlist/song queue mantığına bağlı değildir.
- Background playback, media session ve media playback notification desteği yoktur.
- Offline indirme/çalma yoktur.

### 3.4. Görsel İçerik ve Kapaklar

- API'da artwork/image URL alanı bulunmamaktadır.
- Kapaklar şu an ID tabanlı deterministik gradyan renkleriyle temsil edilir.
- Gerçek artwork yükleme için API alanı ve image loading kararı gereklidir.

### 3.5. Test ve Kalite

- Login ViewModel testi eksiktir.
- Register ViewModel testi eksiktir.
- Library filtreleme ve API mapping testleri eksiktir.
- Favorites state/intent testleri eksiktir.
- Notifications state/intent testleri eksiktir.
- Now Playing / Player state test kapsamı genişletilmelidir.
- Repository API davranış testleri eksiktir.

---

## 4. Önerilen Önceliklendirme

### Faz 1: Güncel Durumu Temizleme

Amaç: Uygulama metinleri ve dokümantasyon güncel `main` durumuyla uyumlu olsun.

Önerilen işler:

- Now Playing kullanıcı metinlerinde Türkçe karakter standardını tamamlamak.
- `docs/roadmap.md` ve `docs/decisions.md` dosyalarını güncel duruma çekmek.
- Build ve lint çıktısını PR öncesi doğrulamak.

### Faz 2: Library API Entegrasyonu

Amaç: Library ekranını mock veriden API tabanlı şarkı/playlist verisine taşımak.

Önerilen işler:

- `LibraryRepository` için Retrofit tabanlı implementasyon eklemek.
- `/api/v1/songs` verisini Library item modeline map etmek.
- API'da background alanı olmadığı için `ArtworkPalette` ile renk üretmek.
- Şarkı item tıklamasını `NowPlaying` rotasına gerçek `songId` ile bağlamak.
- Playlist endpointleri gerekiyorsa `/api/v1/playlists` ve `/api/v1/playlists/{id}` tasarımını esas almak.

### Faz 3: Favorites API Uyumluluğu

Amaç: Favorites ekranının player'a gerçek API song ID'leriyle gitmesini sağlamak.

Önerilen işler:

- Mock favorite song item'ları API song modeliyle uyumlu hale getirmek veya API'dan türetmek.
- Gerçek favori endpoint'i yoksa favori ekleme/silme kalıcılığını kapsam dışı tutmak.
- Kullanıcıya yanıltıcı kalıcılık hissi veren davranışları netleştirmek.

### Faz 3.5: Bildirim Ayarları

Amaç: Profile menüsündeki Bildirimler girişini gerçek bir ayar ekranına taşımak.

Önerilen işler:

- `NotificationsRepository` için mock/local implementasyon eklemek.
- Bildirim tercihlerini MVI ekranında switch kontrolleriyle yönetmek.
- Profile `Bildirimler` tıklamasını `notifications` rotasına bağlamak.
- API'da bildirim endpoint'i olmadığını dokümantasyonda belirtmek.

### Faz 4: Player Deneyimini Genişletme

Amaç: Şarkı çalma akışını gerçek kullanım senaryolarına yaklaştırmak.

Önerilen işler:

- Şarkı kuyruğu modeli eklemek.
- Sonraki/önceki şarkı davranışını gerçek queue üzerinden yönetmek.
- Media session ve notification araştırması yapmak.
- Background playback kapsamını planlamak.

### Faz 5: Offline Dinleme

Amaç: Online/offline müzik çalar hedefini gerçek ürün davranışına yaklaştırmak.

Önerilen işler:

- Download modeli.
- Lokal kayıt/cache stratejisi.
- Library ekranında indirilenler filtresi.
- Offline kullanılabilirlik state'i.
- İndirme/silme happy-path ve hata akışları.

### Faz 6: Test ve Kalite

Amaç: Feature geliştirmelerinde regresyon riskini azaltmak.

Önerilen işler:

- ViewModel unit testleri.
- Repository mapping testleri.
- Navigation happy-path testleri.
- Build ve lint kontrolünü PR öncesi zorunlu hale getirmek.

---

## 5. Önerilen Sıradaki Branchler

1. `feature/notifications-screen`
   - Profile menüsündeki Bildirimler girişini MVI bildirim ayarları ekranına taşır.

2. `feature/favorites-api-alignment`
   - Favorites ekranını gerçek API song ID'leriyle uyumlu hale getirir.

3. `chore/viewmodel-tests`
   - Mevcut MVI ViewModel'ler için temel unit test kapsamı ekler.

---

## 6. Dikkat Edilecek Kurallar

- Pair çalışmaları yalnızca `atalaycitak/LyraApp-pairwork` reposunda yapılmalıdır.
- Bireysel repo ile pair repo karıştırılmamalıdır.
- Branch adlarında yapay zeka aracı adı kullanılmamalıdır.
- Commit author/committer `csezeze <csezeze@gmail.com>` olmalıdır.
- Co-author veya bot imzası eklenmemelidir.
- Her feature için önce dosya dökümü ve bağımlılık matrisi sunulmalıdır.
- Backend sözleşmesi gelmeden API detayı uydurulmamalıdır.
- Mock veriler API tasarımıyla çelişirse API tasarımı esas alınmalıdır.
- Yeni ekranlar MVI kurallarına ve Login referansına uygun yazılmalıdır.
- Kullanıcıya görünen uygulama içi metinlerde Türkçe karakterler korunmalıdır.
