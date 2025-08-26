package com.egeozmen.weather_java_app.controller;

import org.springframework.stereotype.Component;

@Component("weatherCodeHelper")
public class WeatherCodeHelper {
    public String text(int code) {
        return switch (code) {
            case 0 -> "Açık gökyüzü";
            case 1 -> "Çoğunlukla açık";
            case 2 -> "Parçalı bulutlu";
            case 3 -> "Bulutlu";
            case 45 -> "Sis";
            case 48 -> "Kırağılı sis";
            case 51 -> "Hafif çisentı";
            case 53 -> "Orta çisentı";
            case 55 -> "Yoğun çisentı";
            case 61 -> "Hafif yağmur";
            case 63 -> "Yağmur";
            case 65 -> "Kuvvetli yağmur";
            case 66 -> "Hafif donan yağmur";
            case 67 -> "Donan yağmur";
            case 71 -> "Hafif kar";
            case 73 -> "Kar";
            case 75 -> "Yoğun kar";
            case 77 -> "Kar taneleri";
            case 80 -> "Hafif sağanak";
            case 81 -> "Sağanak";
            case 82 -> "Kuvvetli sağanak";
            case 85 -> "Hafif kar sağanağı";
            case 86 -> "Kuvvetli kar sağanağı";
            case 95 -> "Gök gürültülü fırtına";
            case 96 -> "Dolu ile fırtına (hafif)";
            case 99 -> "Dolu ile fırtına";
            default -> "Hava durumu";
        };
    }
    public String emoji(int code) {
        if (code == 0 || code == 1) return "☀️";
        if (code == 2 || code == 3) return "⛅";
        if (code == 45 || code == 48) return "🌫️";
        if (code == 61 || code == 63 || code == 65 || code == 80 || code == 81 || code == 82) return "🌧️";
        if (code == 71 || code == 73 || code == 75 || code == 77 || code == 85 || code == 86) return "❄️";
        if (code == 95 || code == 96 || code == 99) return "⛈️";
        return "☁️";
    }
}

