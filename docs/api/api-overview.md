# Streaming API Genel Bakış

Bu doküman `openapi.json` spesifikasyonundan çıkarılmış API bilgilerini içermektedir. API, Media3/ExoPlayer istemcisine yönelik tasarlanmış bir müzik akışı (streaming) arka plan servisidir.

## 1. Temel Bilgiler (Sunucular)

* **Canlı Ortam (Prod):** `https://streaming-api.halitkalayci.com`
* **Geliştirme Ortamı (Dev):** `http://localhost:3000`

## 2. Şarkı Operasyonları (Songs)

### Şarkı Listeleme
* **Endpoint:** `GET /api/v1/songs`
* **Açıklama:** Şarkıların listesini döner. Arama ve imleç (cursor) tabanlı sayfalama destekler.
* **Parametreler:**
  * `q` (Opsiyonel): Şarkı, sanatçı veya albüm adına göre arama.
  * `limit` (Opsiyonel, Varsayılan: 20): Sayfa boyutu.
  * `cursor` (Opsiyonel): Sonraki sayfayı çekmek için önceki istekten alınan imleç değeri.
* **Dönüş:** Şarkı listesi (`data` dizisi) ve bir sonraki sayfa için `nextCursor` bilgisi.

### Şarkı Detayı
* **Endpoint:** `GET /api/v1/songs/{id}`
* **Açıklama:** Belirli bir şarkının detaylarını ID bazlı getirir.

### Şarkı Stream URL Üretimi
* **Endpoint:** `GET /api/v1/songs/{id}/stream-url`
* **Açıklama:** Şarkıyı çalmak için 300 saniye (5 dakika) geçerli, HMAC-SHA256 imzalı bir akış URL'i üretir. 
* **Mimari Not:** ExoPlayer'a sağlanacak URL bu endpoint'ten anlık olarak alınmalıdır. Liste yanıtlarında saklanmamalıdır.

## 3. Akış Operasyonu (Stream)

* **Endpoint:** `GET /api/v1/stream/{songId}`
* **Açıklama:** İmzalı URL aracılığıyla ses dosyası baytlarını (audio bytes) sunar. ExoPlayer'ın ileri/geri sarma (seek) işlemleri için gereken **HTTP Range** (`bytes=N-` vb.) isteklerini tam destekler (200, 206, 416).
* **Gereksinimler:** İstekte `expires` (zaman aşımı) ve `signature` (imza) parametreleri bulunmalıdır. Bulut depolama sağlayıcısı kullanıldığında CDN imzalı URL'e 302 yönlendirmesi yapabilir.

## 4. Çalma Listesi Operasyonları (Playlists)

### Çalma Listeleri Listesi
* **Endpoint:** `GET /api/v1/playlists`
* **Açıklama:** Sistemdeki çalma listelerinin özet bilgilerini (şarkılar olmadan) getirir.

### Çalma Listesi Detayı
* **Endpoint:** `GET /api/v1/playlists/{id}`
* **Açıklama:** Belirli bir çalma listesinin detayını ve içindeki şarkıları sıralı (track order) şekilde getirir.

## 5. Sistem Sağlığı (Health)

* **Endpoint:** `GET /health`
* **Açıklama:** Servisin ayakta olup olmadığını kontrol eder. Yanıt olarak `{"status": "ok"}` döner.

## 6. Veri Modelleri (Schemas)

* **Song:** Şarkı veri yapısı. (`id`, `title`, `artist`, `album`, `durationMs`, `mimeType`, `sizeBytes`, `createdAt`)
* **Playlist:** Çalma listesi özet yapısı. (`id`, `name`, `description`, `createdAt`)
* **PlaylistWithSongs:** `Playlist` modeline ek olarak içindeki `Song` listesini (`songs` dizisi) barındırır.
* **Error:** Hata durumlarında dönen yapı. (`code`, `message`)
