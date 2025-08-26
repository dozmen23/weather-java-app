# Weather Java App ☀️

Harita üzerinde tıklanan noktanın veya aranan şehrin **hava durumunu** gösteren basit bir uygulama.  
**Java 17 + Spring Boot 3**, **Thymeleaf**, **Leaflet.js** ve **Open-Meteo** kullanır.

## Özellikler
- 🌍 Leaflet dünya haritası — tıklayınca o koordinatın hava durumu
- 🔎 Şehir arama (Open-Meteo Geocoding)
- ☁️ Anlık durum + 7 günlük tahmin (Open-Meteo Forecast)
- 🧠 Hava kodu → **Türkçe açıklama** + **emoji**
- 🌡️ °C / °F birim geçişi
- ♻️ Server-side render (Thymeleaf)

## Kurulum
```bash
# Java 17 kurulu olmalı (Temurin/OpenJDK)
# Projeyi çalıştır
./gradlew bootRun     # Windows: .\gradlew.bat bootRun
