package com.egeozmen.weather_java_app.service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.egeozmen.weather_java_app.model.Location;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class WeatherService {

    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper om = new ObjectMapper();

    /** Şehir arama (Open-Meteo geocoding) */
    public List<Location> searchCity(String q) throws IOException, InterruptedException {
        if (q == null || q.isBlank()) return List.of();
        String enc = URLEncoder.encode(q.trim(), StandardCharsets.UTF_8);
        String url = "https://geocoding-api.open-meteo.com/v1/search?name=" + enc +
                     "&count=5&language=tr&format=json";

        JsonNode root = getJson(url);
        JsonNode results = root.get("results");

        List<Location> list = new ArrayList<>();
        if (results != null && results.isArray()) {
            for (JsonNode r : results) {
                list.add(new Location(
                        text(r, "name"),
                        text(r, "country"),
                        r.path("latitude").asDouble(),
                        r.path("longitude").asDouble()
                ));
            }
        }
        return list;
    }

    /* ——— yardımcılar ——— */
    private JsonNode getJson(String url) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder(URI.create(url)).GET().build();
        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() >= 400) throw new IOException("HTTP " + res.statusCode() + " for " + url);
        return om.readTree(res.body());
    }

    private static String text(JsonNode n, String f) {
        JsonNode x = n.get(f);
        return x == null || x.isNull() ? "" : x.asText();
    }
}
