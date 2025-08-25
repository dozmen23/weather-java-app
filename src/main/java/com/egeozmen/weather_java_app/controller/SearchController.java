package com.egeozmen.weather_java_app.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.egeozmen.weather_java_app.model.Location;
import com.egeozmen.weather_java_app.service.WeatherService;

@RestController
public class SearchController {

    private final WeatherService service;

    public SearchController(WeatherService service) {
        this.service = service;
    }

    @GetMapping("/api/search")
    public List<Location> search(@RequestParam("q") String q) throws Exception {
        return service.searchCity(q);
    }
}
