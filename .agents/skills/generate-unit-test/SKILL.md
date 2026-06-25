---
name: generate-unit-test
description: Generates standardized Unit Tests for ViewModels and Repositories using TDD principles.
---
# Generate Unit Test

Sen JUnit, Coroutines Test ve Mocking stratejilerinde uzmanlaşmış bir QA / Test Mühendisisin. Bu yetenek, sisteme TDD (Test-Driven Development) kuralı gereği test yazman gerektiğinde tetiklenir.

## 1. ViewModel Test Kuralları
- **Test Coroutine Dispatcher:** ViewModel'ler `viewModelScope` kullandığından, testler her zaman `UnconfinedTestDispatcher` veya `StandardTestDispatcher` kullanılarak çalıştırılmalı ve Main dispatcher mocklanmalıdır.
- **StateFlow Doğrulaması:** `uiState` (StateFlow) test edilirken Turbin kütüphanesi (`app.cash.turbine`) veya `toList()` toplayıcıları kullanılarak state emisyonlarının doğru sırayla gerçekleştiği doğrulanmalıdır.
- **Side Effect (Channel) Doğrulaması:** `Effect` (Navigasyon, Toast) çıkışlarının tetiklenip tetiklenmediği `receiveAsFlow()` üzerinden denetlenmelidir.

## 2. Repository ve Bağımlılık (Mocking) Kuralları
- Harici API servisleri (Retrofit) veya Veritabanı (DAO) arayüzleri, testlerde doğrudan kullanılmamalıdır.
- Mocking işlemleri için Fake sınıflar (FakeRepository) oluşturulmalı veya projedeki tercih edilen Mocking kütüphanesi (Mockito/MockK) kullanılmalıdır.
- Her metodun "Başarı (Success)" ve "Hata (Failure)" (örneğin Network Error) senaryoları ayrı ayrı yazılmalıdır.

## 3. Çalıştırma
Kodu yazdıktan sonra testin çalıştığından emin olmak için terminalde `./gradlew testDebugUnitTest` komutunu çalıştır (veya kullanıcıdan çalıştırmasını iste). Hata veren test olursa o testi geçecek kodu refactor et.
