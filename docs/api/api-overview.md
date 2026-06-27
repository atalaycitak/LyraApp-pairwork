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

### Sarki Stream URL Uretimi
* **Endpoint:** `GET /api/v1/songs/{id}/stream-url`
* **Donus:** `{ "data": { "url", "expiresAt", "mimeType" } }` — URL 300 saniye gecerlidir.
* **Not:** Bu endpoint yalnizca premium kullanicilar tarafindan kullanilabilir. Premium olmayan kullanicilar 403 hatasi alir. Tum kullanicilar icin `POST /api/v1/me/playback/next` tercih edilmelidir.

## 5. Akis Operasyonu (Stream)

* **Endpoint:** `GET /api/v1/stream/{songId}`
* **Parametreler:** `expires`, `signature` (query); `Range` (header, opsiyonel)
* **Donus:** `200` (tam), `206` (kismi range), `302` (CDN yonlendirme), `403`/`404`/`410`/`416` hata

## 6. Calma Listeleri (Playlists)

### Genel Calma Listeleri
* **Endpoint:** `GET /api/v1/playlists`
* **Donus:** `{ "data": [ Playlist, ... ] }` (ownerId null olabilir)

### Calma Listesi Detayi
* **Endpoint:** `GET /api/v1/playlists/{id}`
* **Donus:** `{ "data": { PlaylistWithSongs } }`

## 7. Uyelik (Membership)

### Premium Plan Katalogu
* **Endpoint:** `GET /api/v1/memberships/plans`
* **Donus:** `{ "data": [ MembershipPlan, ... ] }`
* **Aciklama:** Satin alinabilir premium planlari listeler. `recurring` aylik yenilenen (139 TL), `one-time` tek seferlik (159 TL).

### Premium Satin Alma (Mock Odeme)
* **Endpoint:** `POST /api/v1/memberships/checkout` (Kimlik dogrulama gerekli)
* **Body:** `{ "plan": "recurring", "card": { "number": "4242 4242 4242 4242", "expMonth": 12, "expYear": 2030, "cvc": "123", "holderName": "..." } }`
* **Donus (201):** `{ "data": { "payment": { "transactionId", "amountKurus", "currency" }, "membership": Membership } }`
* **Test Kartlari:**
  * `4242 4242 4242 4242` — Onaylandi
  * `4000 0000 0000 0002` — Reddedildi (402)
  * Diger tum kartlar — Reddedildi
* **Hatalar:** `400` gecersiz plan/kart, `401` token hatasi, `402` odeme reddedildi

## 8. Oynatma Akisi (Playback)

### Siradaki Oynatma Ogesini Cozumle
* **Endpoint:** `POST /api/v1/me/playback/next` (Kimlik dogrulama gerekli)
* **Body:** `{ "songId": "s_neon-tide" }`
* **Donus (200):** `{ "data": PlaybackSong | PlaybackAd }`
* **Aciklama:** Sarki calmadan hemen once cagrilir. Sunucu dinlemeyi kaydeder ve ne calinacagina karar verir:
  * **Premium:** Daima `type: "song"` doner — dogrudan stream URL ile cal.
  * **Ucretsiz:** Her 3 sarkida bir `type: "ad"` doner — once reklam, sonra sarki.
* **Onemli:** Bu endpoint kullanildiginda ayrica `POST /api/v1/me/plays` cagirilmamalidir; dinleme kaydi otomatik olusturulur.

### Reklam Tamamlandi Bildirimi
* **Endpoint:** `POST /api/v1/me/playback/ad-complete` (Kimlik dogrulama gerekli)
* **Body:** `{ "impressionId": "uuid" }`
* **Donus (200):** `{ "data": { "completed": true } }`
* **Aciklama:** Reklam bittiginde sunucuya bildirilir (analytics). `playback/next` yanitindaki `impressionId` ile cagirilir.

## 9. Sistem Sagligi (Health)

* **Endpoint:** `GET /health`
* **Donus:** `{ "status": "ok" }`

## 10. Veri Modelleri (Schemas)

* **Song:** `id`, `title`, `artist`, `album`, `durationMs`, `mimeType`, `sizeBytes`, `createdAt`
* **Playlist:** `id`, `name`, `description`, `ownerId` (nullable), `createdAt`
* **PlaylistWithSongs:** Playlist + `songs` dizisi
* **User:** `id`, `phone`, `displayName`, `firstName`, `lastName`, `birthDate`, `createdAt`, `profileCompleted`, `membership` (nullable Membership)
* **Membership:** `planId`, `type` (one-time|recurring), `status` (active|expired), `autoRenew`, `startedAt`, `expiresAt`
* **MembershipPlan:** `id`, `type`, `name`, `description`, `priceKurus`, `price`, `currency`, `durationDays`, `autoRenew`
* **Ad:** `id`, `title`, `advertiser`, `durationMs`, `mimeType`
* **StreamLink:** `url`, `expiresAt`, `mimeType`
* **PlaybackSong:** `type: "song"`, `song: Song`, `stream: StreamLink`
* **PlaybackAd:** `type: "ad"`, `ad: Ad`, `adStream: StreamLink`, `impressionId`, `song: Song`, `stream: StreamLink`
* **AuthTokensData:** `accessToken`, `refreshToken`, `tokenType`, `expiresIn`, `user`, `firstTime`
* **Error:** `{ "error": { "code", "message" } }`

