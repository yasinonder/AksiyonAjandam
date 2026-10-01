# Aksiyon Ajandam - Release Signing

Aksiyon Ajandam v0.3 ve sonraki sürümler kalıcı bir upload/release anahtarıyla imzalanacak.

## GitHub Secrets

Repository > Settings > Secrets and variables > Actions > New repository secret bölümünden şu dört secret eklenmelidir:

- `ANDROID_KEYSTORE_BASE64`
- `ANDROID_KEYSTORE_PASSWORD`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEY_PASSWORD`

Bu değerler kullanıcıya ayrı olarak verilen `AksiyonAjandam-SigningKit-v0.3.zip` içindeki `SIGNING_INFO.txt` dosyasında bulunur.

## Release derleme

GitHub > Actions > **Android Release APK + AAB** > **Run workflow**

Başarılı çalıştırma sonunda tek artifact içinde:

- `AksiyonAjandam-v0.3.0-release.apk`
- `AksiyonAjandam-v0.3.0-release.aab`
- `SHA256SUMS.txt`

üretilir.

## Önemli

`.jks` dosyası repoya commit edilmemelidir. Anahtar kaybolursa aynı imzayla doğrudan APK güncellemesi üretilemez. Google Play App Signing kullanılırsa bu anahtar upload key olarak saklanabilir.
