package com.egeozmen.weather_java_app.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.egeozmen.weather_java_app.model.Location;
import com.egeozmen.weather_java_app.model.WeatherView;
import com.egeozmen.weather_java_app.service.WeatherService;

@Controller
public class HomeController {

    private static final Logger log = LoggerFactory.getLogger(HomeController.class);
    private final WeatherService service;

    public HomeController(WeatherService service) {
        this.service = service;
    }

    @GetMapping("/")
    public String index(
            @RequestParam(value = "q",   required = false) String q,
            @RequestParam(value = "lat", required = false) Double lat,
            @RequestParam(value = "lon", required = false) Double lon,
            @RequestParam(value = "name",required = false) String name,
            Model model
    ) {
        try {
            // 1) Koordinat verilmişse: forecast
            if (lat != null && lon != null) {
                // name koordinat gibi mi? (örn. "41.025, 28.923")
                boolean looksLikeCoords = (name == null) ||
                        name.isBlank() ||
                        name.matches("[-+]?\\d+(\\.\\d+)?\\s*,\\s*[-+]?\\d+(\\.\\d+)?");

                if (looksLikeCoords) {
                    try {
                        Location loc = service.reverseGeocode(lat, lon);
                        if (loc != null && (loc.name() != null && !loc.name().isBlank())) {
                            name = loc.country().isBlank() ? loc.name() : (loc.name() + " — " + loc.country());
                        } else {
                            name = String.format("%.3f, %.3f", lat, lon);
                        }
                    } catch (Exception e) {
                        log.warn("Reverse geocode failed: {}", e.toString());
                        name = String.format("%.3f, %.3f", lat, lon);
                    }
                }

                WeatherView w = service.getForecast(lat, lon, name);
                model.addAttribute("weather", w);
                model.addAttribute("lat", lat);
                model.addAttribute("lon", lon);
                return "index";
            }

            // 2) Arama sorgusu
            if (q != null && !q.isBlank()) {
                List<Location> results = service.searchCity(q);
                model.addAttribute("results", results);
                model.addAttribute("query", q);
                return "index";
            }

            // 3) İlk sayfa
            return "index";
        } catch (Exception ex) {
            log.error("Index error", ex);
            model.addAttribute("error", "Bir şeyler ters gitti: " + ex.getMessage());
            return "index";
        }
    }
}
