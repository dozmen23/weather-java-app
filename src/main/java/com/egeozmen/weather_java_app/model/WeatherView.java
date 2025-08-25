package com.egeozmen.weather_java_app.model;

import java.util.List;

public record WeatherView(
        String locationName,
        int currentTemp,
        int currentCode,
        List<DailyForecast> daily
) {}
