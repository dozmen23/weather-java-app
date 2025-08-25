package com.egeozmen.weather_java_app.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.egeozmen.weather_java_app.model.WeatherView;
import com.egeozmen.weather_java_app.service.WeatherService;

@RestController
public class ForecastController {

    private final WeatherService service;

    public ForecastController(WeatherService service) {
        this.service = service;
    }

    @GetMapping("/api/forecast")
    public WeatherView forecast(
            @RequestParam("lat") double lat,
            @RequestParam("lon") double lon,
            @RequestParam(value = "name", required = false, defaultValue = "Seçili konum") String name
    ) throws Exception {
        return service.getForecast(lat, lon, name);
    }
}

