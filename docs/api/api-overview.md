# Streaming API Genel Bakış

Bu doküman `openapi.json` spesifikasyonundan çıkarılmış API bilgilerini içermektedir. API, Media3/ExoPlayer istemcisine yönelik tasarlanmış bir müzik akışı (streaming) arka plan servisidir.

## 1. Temel Bilgiler (Sunucular)

* **Canlı Ortam (Prod):** `https://streaming-api.halitkalayci.com`
* **Geliştirme Ortamı (Dev):** `http://localhost:3000`
* **Kimlik Doğrulama:** Bearer JWT (header: `Authorization: Bearer <accessToken>`)

## 2. Kimlik Doğrulama (Auth)

### OTP İsteği
* **Endpoint:** `POST /api/v1/auth/otp/request`
* **Body:** `{ "phone": "+905551234567" }`
* **Dönüş:** `{ "data": { "sent": true, "firstTime": false } }`

### OTP Doğrulama
* **Endpoint:** `POST /api/v1/auth/otp/verify`
* **Body:** `{ "phone": "+905551234567", "code": "123456" }`
* **Dönüş:** `accessToken`, `refreshToken`, `tokenType`, `expiresIn`, `user`, `firstTime`

### Token Yenileme
* **Endpoint:** `POST /api/v1/auth/refresh`
* **Body:** `{ "refreshToken": "..." }`
* **Dönüş:** Yeni token çifti. 401 → refresh token süresi dolmuş.

### Çıkış
* **Endpoint:** `POST /api/v1/auth/logout`
* **Body:** `{ "refreshToken": "..." }`
* **Dönüş:** `{ "data": { "revoked": true } }`

## 3. Kullanıcı Profili (Me)

### Profil Bilgisi
* **Endpoint:** `GET /api/v1/me` (Kimlik doğrulama gerekli)
* **Dönüş:** `{ "data": { "id", "phone", "displayName", "firstName", "lastName", "birthDate", "createdAt", "profileCompleted" } }`

### Profil Güncelleme
* **Endpoint:** `POST /api/v1/me/update-informations`
* **Body:** `{ "firstName": "...", "lastName": "...", "birthDate": "2000-01-01" }`
* **Dönüş:** Güncellenmiş kullanıcı profili

### Son Çalınanlar
* **Endpoint:** `GET /api/v1/me/recently-played?limit=N`
* **Dönüş:** `{ "data": [ Song, ... ] }`

### Senin İçin
* **Endpoint:** `GET /api/v1/me/for-you?limit=N`
* **Dönüş:** `{ "data": [ Song, ... ] }`

### Öneriler
* **Endpoint:** `GET /api/v1/me/recommendations?limit=N`
* **Dönüş:** `{ "data": [ Song, ... ] }`

### Oynatma Bildirimi
* **Endpoint:** `POST /api/v1/me/plays`
* **Body:** `{ "songId": "s_neon-tide" }`
* **Dönüş:** `201` `{ "data": { "recorded": true } }`

### Çalma Listelerim
* **Endpoint:** `GET /api/v1/me/playlists`
* **Dönüş:** `{ "data": [ Playlist, ... ] }` (ownerId dolu)

### Çalma Listesi Oluşturma
* **Endpoint:** `POST /api/v1/me/playlists`
* **Body:** `{ "name": "...", "description": "..." }`
* **Dönüş:** `201` `{ "data": { "id", "name", "description", "createdAt", "ownerId" } }`

### Listeye Şarkı Ekleme
* **Endpoint:** `POST /api/v1/me/playlists/{id}/tracks`
* **Body:** `{ "songId": "s_..." }`
* **Dönüş:** `201` `{ "data": { "added": true } }`

### Listeden Şarkı Çıkarma
* **Endpoint:** `DELETE /api/v1/me/playlists/{id}/tracks/{songId}`
* **Dönüş:** `{ "data": { "removed": true } }`

## 4. Şarkı Operasyonları (Songs)

### Şarkı Listeleme
* **Endpoint:** `GET /api/v1/songs`
* **Parametreler:** `q` (arama), `limit` (varsayılan 20), `cursor` (sayfalama)
* **Dönüş:** `{ "data": [ Song, ... ], "nextCursor": "..." }`

### Şarkı Detayı
* **Endpoint:** `GET /api/v1/songs/{id}`
* **Dönüş:** `{ "data": { Song } }`

### Şarkı Stream URL Üretimi
* **Endpoint:** `GET /api/v1/songs/{id}/stream-url`
* **Dönüş:** `{ "data": { "url", "expiresAt", "mimeType" } }` — URL 300 saniye geçerlidir.

## 5. Akış Operasyonu (Stream)

* **Endpoint:** `GET /api/v1/stream/{songId}`
* **Parametreler:** `expires`, `signature` (query); `Range` (header, opsiyonel)
* **Dönüş:** `200` (tam), `206` (kısmi range), `302` (CDN yönlendirme), `403`/`404`/`410`/`416` hata

## 6. Çalma Listeleri (Playlists)

### Genel Çalma Listeleri
* **Endpoint:** `GET /api/v1/playlists`
* **Dönüş:** `{ "data": [ Playlist, ... ] }` (ownerId null olabilir)

### Çalma Listesi Detayı
* **Endpoint:** `GET /api/v1/playlists/{id}`
* **Dönüş:** `{ "data": { PlaylistWithSongs } }`

## 7. Sistem Sağlığı (Health)

* **Endpoint:** `GET /health`
* **Dönüş:** `{ "status": "ok" }`

## 8. Veri Modelleri (Schemas)

* **Song:** `id`, `title`, `artist`, `album`, `durationMs`, `mimeType`, `sizeBytes`, `createdAt`
* **Playlist:** `id`, `name`, `description`, `ownerId` (nullable), `createdAt`
* **PlaylistWithSongs:** Playlist + `songs` dizisi
* **User:** `id`, `phone`, `displayName`, `firstName`, `lastName`, `birthDate`, `createdAt`, `profileCompleted`
* **AuthTokensData:** `accessToken`, `refreshToken`, `tokenType`, `expiresIn`, `user`, `firstTime`
* **Error:** `{ "error": { "code", "message" } }`
