# LyraApp - Tipografi Sistemi 

> Bu dosya LyraApp isimli uygulamanın tipografi paleti için 
> **tek doğruluk kaynağıdır** (single source of truth) ve
> doğrudan bir **Android Jetpack Compose** projesinde kullanılmak
> üzere düzenlenmiştir.
> Tüm metin alanları **Roboto** font ailesi üzerine inşa edilmiştir.

---

## 1. Temel Kural

> Hiçbir `@Composable` içinde doğrudan `fontSize = 16.sp` veya `fontWeight = FontWeight.Bold` gibi spesifik değerler (hardcoded) yazılmaz.
> Tipografi daima `MaterialTheme.typography.<slot>` üzerinden 
> okunmak zorundadır.

Spesifik `sp` değerleri yalnızca `Type.kt` içinde, sabit değişken tanımlanırken kullanılır. Kural dışı tasarım öğeleri gerekirse `MaterialTheme.typography.titleMedium.copy(...)` gibi yöntemlerle esnetilebilir.

---

## 2. Tipografi Tablosu

| Kategori | Font Family | Weight | Size (sp) | Line Height (sp) | Letter Spacing (sp) |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Display Large** | Roboto | Regular (400) | 57 | 64 | 0 |
| **Display Medium** | Roboto | Regular (400) | 45 | 52 | 0 |
| **Display Small** | Roboto | Regular (400) | 36 | 44 | 0 |
| **Headline Large** | Roboto | Regular (400) | 32 | 40 | 0 |
| **Headline Medium** | Roboto | Regular (400) | 28 | 36 | 0 |
| **Headline Small** | Roboto | Regular (400) | 24 | 32 | 0 |
| **Title Large** | Roboto | Regular (400) | 22 | 28 | 0 |
| **Title Medium** | Roboto | Medium (500) | 16 | 24 | 0.15 |
| **Title Small** | Roboto | Medium (500) | 14 | 20 | 0.1 |
| **Body Large** | Roboto | Regular (400) | 16 | 24 | 0.5 |
| **Body Medium** | Roboto | Regular (400) | 14 | 20 | 0.25 |
| **Body Small** | Roboto | Regular (400) | 12 | 16 | 0.4 |
| **Label Large** | Roboto | Medium (500) | 14 | 20 | 0.1 |
| **Label Medium** | Roboto | Medium (500) | 12 | 16 | 0.5 |
| **Label Small** | Roboto | Medium (500) | 11 | 16 | 0.5 |

---

## 3. `Type.kt` — Tipografi Tanımları

```kotlin
package com.turkcell.lyraapp.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Set of Material typography styles to start with
val LyraTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = 0.sp
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)
```

---

## 4. Kullanım Kuralları

- **Display**: Uygulama içi çok büyük vurgular (ör. boş sayfa durumları veya devasa sayaçlar).
- **Headline**: Ekran başlıkları veya önemli kart başlıkları.
- **Title**: Standart bölüm ve liste öğesi başlıkları.
- **Body**: Uzun paragraflar, açıklamalar ve standart okunabilir metinler.
- **Label**: Buton metinleri, tag'ler, çip (chip) içerikleri veya navigasyon öğeleri.

> **Not:** Roboto fontu Android Jetpack Compose üzerinde varsayılan (default) font olarak geldiği için ekstra bir yükleme gerektirmez.

---
