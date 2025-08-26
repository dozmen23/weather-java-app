package com.egeozmen.weather_java_app.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.egeozmen.weather_java_app.model.DailyForecast;
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
            @RequestParam(value = "u",   required = false, defaultValue = "c") String unit, // °C / °F
            Model model
    ) {
        try {
            // 1) Koordinat verilmişse: forecast akışı
            if (lat != null && lon != null) {
                // name sadece koordinat gibi ise ters geocoding dene
                boolean looksLikeCoords = (name == null) ||
                        name.isBlank() ||
                        name.matches("[-+]?\\d+(\\.\\d+)?\\s*,\\s*[-+]?\\d+(\\.\\d+)?");
                if (looksLikeCoords) {
                    try {
                        Location loc = service.reverseGeocode(lat, lon);
                        if (loc != null && loc.name() != null && !loc.name().isBlank()) {
                            name = loc.country().isBlank()
                                    ? loc.name()
                                    : (loc.name() + " — " + loc.country());
                        } else {
                            name = String.format("%.3f, %.3f", lat, lon);
                        }
                    } catch (Exception e) {
                        log.warn("Reverse geocode failed: {}", e.toString());
                        name = String.format("%.3f, %.3f", lat, lon);
                    }
                }

                WeatherView w = service.getForecast(lat, lon, name);
                WeatherView converted = maybeConvertUnit(w, unit);

                model.addAttribute("weather", converted);
                model.addAttribute("lat", lat);
                model.addAttribute("lon", lon);
                model.addAttribute("unit", unit.toLowerCase());
                return "index";
            }

            // 2) Arama sorgusu verilmişse
            if (q != null && !q.isBlank()) {
                List<Location> results = service.searchCity(q);
                model.addAttribute("results", results);
                model.addAttribute("query", q);
                model.addAttribute("unit", unit.toLowerCase());
                return "index";
            }

            // 3) İlk sayfa
            model.addAttribute("unit", unit.toLowerCase());
            return "index";

        } catch (Exception ex) {
            log.error("Index error", ex);
            model.addAttribute("error", "Bir şeyler ters gitti: " + ex.getMessage());
            model.addAttribute("unit", unit.toLowerCase());
            return "index";
        }
    }

    // ---------- helpers: °C <-> °F ----------
    private WeatherView maybeConvertUnit(WeatherView w, String unit) {
        if (!"f".equalsIgnoreCase(unit)) return w; // default C
        return new WeatherView(
                w.locationName(),
                c2f(w.currentTemp()),
                w.currentCode(),
                w.daily().stream()
                        .map(d -> new DailyForecast(d.date(), c2f(d.tmax()), c2f(d.tmin()), d.code()))
                        .toList()
        );
    }

    private int c2f(int c) {
        return (int) Math.round(c * 9.0 / 5.0 + 32);
    }
}
