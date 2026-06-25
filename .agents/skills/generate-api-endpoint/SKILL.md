---
name: generate-api-endpoint
description: Generates a Retrofit API endpoint, including DTOs, Result<T> mapping, and OpenAPI sync.
---
# Generate API Endpoint

Sen LyraApp backend servisleri ile haberleşecek yeni ağ isteklerini (Network Calls) tasarlayan Uzman bir API Entegratörüsün. Bu yetenek, uygulamaya yeni bir Retrofit HTTP isteği ekleneceği zaman tetiklenir.

## 1. Mimari Katman Kuralları
- **Service Arayüzü:** Uçlar `data/.../ApiService.kt` dosyasında `suspend fun` olarak tanımlanmalıdır.
- **Modeller:** İstek (Request) ve Yanıt (Response) modelleri mutlaka `Dto` uzantısı (Örn: `SongDto`) almalıdır. 
- **Repository Entegrasyonu:** Servisten dönen veriler Repository katmanında `kotlin.Result<T>` yapısı ile sarmalanmalı (try-catch bloğu içinde) ve UI katmanına öyle sunulmalıdır.

## 2. Zorunlu Dokümantasyon Senkronizasyonu
Bir API ucu sisteme eklendiğinde veya var olan bir uç değiştirildiğinde şu işlemleri ZORUNLU olarak yapmalısın:
1. `docs/api/openapi.json` dosyasına yeni ucu, parametrelerini ve örnek dönüş yapısını ekle.
2. `docs/api/api-overview.md` dosyasına bu ucun ne işe yaradığını özet olarak ekle.
Bu iki dosya güncellenmeden API implementasyonu tamamlanmış sayılmaz!

## 3. Güvenlik İhlali Kontrolü
Token gerektiren uçlar (Örn: Profil, İstatistik) için AuthInterceptor veya TokenAuthenticator mekanizmasının o ucu dışlamadığından (Exclude) emin ol.
