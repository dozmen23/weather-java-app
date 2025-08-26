package com.egeozmen.weather_java_app.service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.egeozmen.weather_java_app.model.DailyForecast;
import com.egeozmen.weather_java_app.model.Location;
import com.egeozmen.weather_java_app.model.WeatherView;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class WeatherService {

    // Tek kez oluştur (timeout’lu client)
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8))
            .build();

    private final ObjectMapper om = new ObjectMapper();
public com.egeozmen.weather_java_app.model.Location reverseGeocode(double lat, double lon)
        throws IOException, InterruptedException {
    String url = "https://geocoding-api.open-meteo.com/v1/reverse?latitude=" + lat +
            "&longitude=" + lon + "&language=tr&format=json";
    JsonNode root = getJson(url);
    JsonNode results = root.path("results");
    if (results.isArray() && results.size() > 0) {
        JsonNode r = results.get(0);
        String name = text(r, "name");
        String country = text(r, "country");
        return new com.egeozmen.weather_java_app.model.Location(
                name, country,
                r.path("latitude").asDouble(),
                r.path("longitude").asDouble()
        );
    }
    // Bulamazsa koordinatı isim olarak döndür
    String fallback = String.format("%.3f, %.3f", lat, lon);
    return new com.egeozmen.weather_java_app.model.Location(fallback, "", lat, lon);
}

    /** Şehir arama (Open-Meteo geocoding) */
    public List<Location> searchCity(String q) throws IOException, InterruptedException {
        if (q == null || q.isBlank()) return List.of();
        String enc = URLEncoder.encode(q.trim(), StandardCharsets.UTF_8);
        String url = "https://geocoding-api.open-meteo.com/v1/search?name=" + enc +
                "&count=5&language=tr&format=json";

        JsonNode root = getJson(url);
        JsonNode results = root.path("results");

        List<Location> list = new ArrayList<>();
        if (results.isArray()) {
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

    /** Verilen koordinasyon için basit forecast (anlık + günlük) */
    public WeatherView getForecast(double lat, double lon, String locName)
            throws IOException, InterruptedException {

        // Koordinasyon güvenlik kontrolü
        if (lat < -90 || lat > 90 || lon < -180 || lon > 180) {
            throw new IllegalArgumentException("Geçersiz koordinat: lat=" + lat + ", lon=" + lon);
        }
        String name = (locName == null || locName.isBlank()) ? "Seçili konum" : locName.trim();

        String url = "https://api.open-meteo.com/v1/forecast?latitude=" + lat + "&longitude=" + lon +
                "&current=temperature_2m,weather_code" +
                "&daily=temperature_2m_max,temperature_2m_min,weather_code" +
                "&timezone=auto";

        JsonNode root = getJson(url);

        // Current güvenli okuma
        JsonNode cur = root.path("current");
        int currentTemp = (int) Math.round(cur.path("temperature_2m").asDouble());
        int currentCode = cur.path("weather_code").asInt();

        // ---- daily arrays (null-safe) ----
     // ---- daily arrays (null-safe) ----
JsonNode daily = root.path("daily");
JsonNode timeArr = daily.path("time");
JsonNode tmaxArr = daily.path("temperature_2m_max");
JsonNode tminArr = daily.path("temperature_2m_min");
JsonNode codeArr = daily.path("weather_code");

int nTime = timeArr.isArray() ? timeArr.size() : 0;
int nTmax = tmaxArr.isArray() ? tmaxArr.size() : 0;
int nTmin = tminArr.isArray() ? tminArr.size() : 0;
int nCode = codeArr.isArray() ? codeArr.size() : 0;
int n = Math.min(Math.min(nTime, nTmax), Math.min(nTmin, nCode));

List<DailyForecast> days = new ArrayList<>(n);
for (int i = 0; i < n; i++) {
    // path() => null yerine MissingNode döner, NPE riski yok
    String date = timeArr.path(i).asText("");
    int tmaxVal = (int) Math.round(tmaxArr.path(i).asDouble(0));
    int tminVal = (int) Math.round(tminArr.path(i).asDouble(0));
    int codeVal = codeArr.path(i).asInt(0);
    days.add(new DailyForecast(date, tmaxVal, tminVal, codeVal));
}


        return new WeatherView(name, currentTemp, currentCode, days);
    }

    /* ---------------- helpers ---------------- */

    private JsonNode getJson(String url) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(12))
                .GET()
                .build();
        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() >= 400) {
            throw new IOException("HTTP " + res.statusCode() + " for " + url);
        }
        return om.readTree(res.body());
    }

    private static String text(JsonNode n, String f) {
        JsonNode x = n.get(f);
        return (x == null || x.isNull()) ? "" : x.asText();
    }
}
