# Aksiyon Ajandam

**Unutmadan yakala, zamanında aksiyon al.**

Sesli/yazılı aksiyon kaydı, fotoğraf ekleme, tarih-saat planlama, alarm ve erteleme özellikli Android uygulaması.

## v0.3.0

- Ana Başlık, Konu ve Açıklama
- Genel / Görev / Randevu / Alışveriş / Araç / Evrak türleri
- Tür seçimine göre değişken alanlar
- Tarih ve saat
- Alarm
- Tamamlandı / 10 dk ertele / 30 dk ertele
- Galeriden birden fazla görsel
- Sesli veri girişi
- Temel Türkçe tarih, saat, tür ve öncelik yorumlama
- Yerel SQLite kayıt
- Bugün / Yaklaşan / Tümü / Tamamlanan filtreleri
- Yeniden başlatma sonrası alarm planlama
- Özel açık/koyu Aksiyon Ajandam teması
- Ajanda + onay işaretli uygulama ikonu
- Kalıcı release/upload key altyapısı
- Signed release APK + Google Play için AAB
- GitHub Actions ile otomatik release üretimi

## Release Signing

Release anahtarı repoya commit edilmez. GitHub Actions, repository secrets üzerinden kalıcı anahtarı kullanır.

Ayrıntılar: [SIGNING.md](SIGNING.md)

## Teknoloji

- Kotlin
- Jetpack Compose
- SQLite
- AlarmManager
- Android Speech Recognizer

> Google Play dışından APK yüklenirken Android veya üretici güvenlik sistemi uygulamayı yine tarayabilir. Normal mağaza kurulum akışı için signed AAB, Google Play Internal Testing kanalında kullanılmalıdır.
