# LyraApp Yol Haritası

> Bu doküman pair çalışma reposu için mevcut durum, tamamlanan işler ve sonraki geliştirme
> adımlarını özetler. Kaynaklar: `agents.md`, `docs/decisions.md`, mevcut MVI feature paketleri
> ve `lyraapp.pdf` tasarım referansı.

---

## 1. Mevcut Durum Özeti

Proje Android Jetpack Compose ve Kotlin ile geliştirilen online/offline müzik çalar
uygulamasıdır. Sunum katmanı MVI mimarisiyle ilerler. Backend REST API sözleşmesi henüz
bağlanmadığı için veri katmanında repository interface + mock/fake implementasyon deseni
kullanılmaktadır.

Mevcut build durumu:

- Pair repo local klasörü: `C:\Users\zzeyn\Documents\LyraApp-pairwork`
- Remote repo: `atalaycitak/LyraApp-pairwork`
- Ana branch: `main`
- Son doğrulanan commit: `6564e24`
- Son doğrulanan build: `.\gradlew.bat build --console=plain --stacktrace`
- Cihaz doğrulaması: Pixel 5 API 35 üzerinde uygulama çalıştırıldı.

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
- Mimari kararlar `docs/decisions.md` altında tutulmaya başlandı.

### 2.2. Tasarım Sistemi

- Renk sistemi eklendi.
- Tipografi sistemi eklendi.
- Uygulama teması `LyraAppTheme` üzerinden yönetiliyor.
- Material Icons bağımlılığı eklenmeden proje içi `LyraIcons` seti oluşturuldu.

### 2.3. Auth Akışı

- Login ekranı MVI ile eklendi.
- Register ekranı MVI ile eklendi.
- Auth repository interface ve fake implementasyon eklendi.
- Auth DI binding eklendi.
- Login/Register arası navigation effect tabanlı kuruldu.

### 2.4. Ana Akış ve Sekmeler

- Home ekranı MVI ile eklendi.
- Search ekranı MVI ile eklendi.
- Library ekranı MVI ile eklendi.
- Now Playing ekranı MVI ile eklendi.
- Yeni çalma listesi oluşturma ekranı MVI ile eklendi.
- Bottom navigation bar eklendi.
- `Home`, `Search`, `Library`, `Favorites`, `Profile` sekmeleri tanımlandı.
- `Favorites` ve `Profile` dışındaki ana ekranlar gerçek route'lara bağlandı.

### 2.5. Veri Katmanı

Mock/fake repository deseni aşağıdaki feature'larda uygulanmaktadır:

- `auth`
- `home`
- `search`
- `library`
- `nowplaying`
- `playlist`

Bu yapı backend hazır olduğunda ViewModel ve UI katmanını bozmadan gerçek implementasyona
geçmeyi amaçlar.

---

## 3. Eksik Kapsam

### 3.1. Placeholder Kalan Ekranlar

Aşağıdaki sekmeler navigation içinde tanımlıdır ancak gerçek MVI feature olarak henüz
tamamlanmamıştır:

- `Favorites`
- `Profile`

Bu ekranlar şu anda `PlaceholderScreen` ile temsil edilmektedir.

### 3.2. Backend Entegrasyonu

Backend REST API sözleşmesi henüz projeye bağlanmamıştır. Bu nedenle:

- DTO modelleri yoktur.
- API service katmanı yoktur.
- Repository implementasyonları gerçek ağ çağrısı yapmamaktadır.
- Auth token/session yönetimi yoktur.
- Hata modeli ve response mapping standardı netleşmemiştir.

API sözleşmesi gelmeden endpoint, response modeli veya iş kuralı uydurulmamalıdır.

### 3.3. Gerçek Medya Oynatma

Now Playing ekranı görsel ve state akışı olarak hazırdır; ancak gerçek player entegrasyonu
yoktur.

Eksikler:

- Gerçek medya oynatıcı motoru seçimi.
- Play/pause, seek, next/previous aksiyonlarının player'a bağlanması.
- Player state'inin ViewModel state'ine aktarılması.
- Background playback ve notification/media session desteği.

### 3.4. Offline Mod

Uygulama hedefinde offline müzik çalma vardır; ancak bu kapsam henüz teknik olarak
tamamlanmamıştır.

Eksikler:

- İndirme durumu modeli.
- Lokal cache veya persistence katmanı.
- İndirilen içeriklerin Library ekranıyla gerçek entegrasyonu.
- Offline/online ayrımı.
- Depolama ve silme akışları.

### 3.5. Görsel İçerik ve Kapaklar

Şu anda album/playlist görselleri gerçek image URL ile değil, gradient placeholder ile
temsil edilmektedir.

Eksikler:

- Görsel yükleme kütüphanesi kararı.
- Image URL model alanları.
- Loading/error placeholder davranışı.
- PDF tasarım referansıyla birebir görsel kontrol.

### 3.6. Test Kapsamı

Mevcut test kapsamı sınırlıdır. Feature ViewModel testleri ve navigation happy-path testleri
eklenmelidir.

Eksikler:

- Login ViewModel testleri.
- Register ViewModel testleri.
- Library filtreleme testleri.
- Create Playlist validasyon testleri.
- Now Playing state testleri.
- Repository mock davranış testleri.

---

## 4. Önerilen Önceliklendirme

### Faz 1: Placeholder Ekranları Tamamlama

Amaç: Bottom navigation içindeki tüm sekmeler gerçek MVI feature haline gelsin.

Önerilen işler:

- `Favorites` feature paketi:
  - `FavoritesContract.kt`
  - `FavoritesViewModel.kt`
  - `FavoritesScreen.kt`
  - `data/favorites/`
  - `di/FavoritesModule.kt`
- `Profile` feature paketi:
  - `ProfileContract.kt`
  - `ProfileViewModel.kt`
  - `ProfileScreen.kt`
  - `data/profile/`
  - `di/ProfileModule.kt`
- `LyraNavHost` placeholder bağlantılarının gerçek route'larla değiştirilmesi.
- `docs/decisions.md` karar kayıtlarının eklenmesi.

### Faz 2: Backend Sözleşmesine Hazırlık

Amaç: Backend geldiğinde mock repository'lerden gerçek repository'lere kontrollü geçiş
yapılabilsin.

Önerilen işler:

- API response/error standardını beklemek.
- DTO ve mapper paket yapısını kararlaştırmak.
- Network katmanı için kütüphane kararı vermek.
- Auth token/session yönetimi için karar almak.
- Repository interface'lerini backend sözleşmesiyle karşılaştırmak.

### Faz 3: Gerçek Medya Oynatma

Amaç: Now Playing ekranını simülasyondan gerçek player state'ine taşımak.

Önerilen işler:

- Media3/ExoPlayer kararı.
- Player controller abstraction.
- Player state observer.
- Now Playing ViewModel entegrasyonu.
- Background playback ve media notification araştırması.

### Faz 4: Offline Dinleme

Amaç: Online/offline müzik çalar hedefini gerçek ürün davranışına yaklaştırmak.

Önerilen işler:

- Download modeli.
- Lokal kayıt/cache stratejisi.
- Library ekranında indirilenler filtresi.
- Offline kullanılabilirlik state'i.
- İndirme/silme happy-path ve hata akışları.

### Faz 5: Görsel ve UX İnce Ayar

Amaç: PDF tasarım referansı ile mevcut ekranları uyumlu hale getirmek.

Önerilen işler:

- PDF referansındaki ekranların tek tek mevcut uygulama ekranlarıyla karşılaştırılması.
- Spacing, typography ve color uyum kontrolü.
- Empty/loading/error state standardizasyonu.
- Gerçek artwork yükleme kararı sonrası görsel alanların güncellenmesi.

### Faz 6: Test ve Kalite

Amaç: Feature geliştirmelerinde regresyon riskini azaltmak.

Önerilen işler:

- ViewModel unit testleri.
- Repository fake/mock testleri.
- Navigation happy-path testleri.
- Build ve lint kontrolünün PR öncesi zorunlu hale getirilmesi.

---

## 5. Önerilen İlk Üç Branch

1. `feature/favorites-screen`
   - Favoriler sekmesini placeholder'dan gerçek MVI ekrana taşır.

2. `feature/profile-screen`
   - Profil sekmesini placeholder'dan gerçek MVI ekrana taşır.

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
- Yeni ekranlar MVI kurallarına ve Login referansına uygun yazılmalıdır.
