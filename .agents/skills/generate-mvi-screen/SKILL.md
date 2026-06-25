---
name: generate-mvi-screen
description: Generates a complete Compose MVI screen for LyraApp following strict architecture rules.
---
# Generate MVI Screen

Sen LyraApp için yeni bir ekran tasarlayacak olan Kıdemli Compose/MVI Mimarı'sın. Bu yetenek, projede MVI mimarisine sadık kalarak sıfırdan bir ekran oluşturmak istediğinde tetiklenir.

## 1. Dizin ve Dosya Yapısı
Yeni eklenecek her özellik (feature) kendi klasöründe olmalıdır:
`ui/<feature_name>/`
Bu klasör içinde şu dosyalar ZORUNLUDUR:
- `<Feature>Contract.kt` (UiState, Intent, Effect)
- `<Feature>ViewModel.kt` (Hilt ile enjekte edilmiş, MVI mantığını yöneten ViewModel)
- `<Feature>Screen.kt` (State'i okuyan, Intent fırlatan Compose arayüzü)

## 2. Kural Setleri (Referanslar)
Ekranı üretirken uyman ZORUNLU OLAN dokümanlar şunlardır:
1. `docs/architecture/mvi-contracts.md`
2. `docs/architecture/mvi-viewmodel-rules.md`
Kodu üretmeden önce lütfen bu referans belgelerini `view_file` ile oku veya bağlamda olduklarından emin ol.

## 3. Kod Üretim Standartları
- **UiState:** Sadece Data Class olmalı. Mutlak doğrular içermeli (Örn: `isLoading`, `errorMessage`, `data`).
- **Intent:** Sealed Class veya Interface olmalı. Kullanıcının yapabileceği aksiyonları (Örn: `OnButtonClicked`, `LoadData`) temsil etmeli.
- **Effect:** Ekran navigasyonu veya Toast mesajı gibi tek seferlik olayları temsil etmeli (Sealed Class).
- **ViewModel:** Mutlaka `kotlinx.coroutines.channels.Channel` ile Effect'leri yönetmeli ve `StateFlow` ile UiState sunmalıdır.

## 4. Onay (Review) Aşaması
Kodu fiziksel dosyalara yazmadan önce, üretilen `Contract`, `ViewModel` ve `Screen` taslaklarını kullanıcıya (User) sun. "Onaylıyor musun?" diye sor. Kullanıcı onaylarsa `write_to_file` aracıyla fiziksel dosyaları yarat.
