package com.simec.weather;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

@Repository
public class WeatherRepository {

    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String baseUrl;

    @Autowired
    public WeatherRepository(RestTemplate restTemplate,
                             @Value("${provider.key}") String apiKey,
                             @Value("${provider.url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
    }

    public ResponseEntity<String> findForLocation(String location) {
        String url = String.format("%s/%s?key=%s", baseUrl, location, apiKey);
        return restTemplate.getForEntity(url, String.class);
    }
}
