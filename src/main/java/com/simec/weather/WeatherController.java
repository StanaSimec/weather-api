package com.simec.weather;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
public class WeatherController {

    private static final Duration REDIS_EXPIRATION = Duration.ofMinutes(1);
    private final RedisTemplate<String, String> redisTemplate;
    private final WeatherRepository weatherRepository;

    @Autowired
    public WeatherController(RedisTemplate<String, String> redisTemplate, WeatherRepository weatherRepository) {
        this.redisTemplate = redisTemplate;
        this.weatherRepository = weatherRepository;
    }

    @GetMapping(value = "/weather/api/{location}", produces = "application/json")
    public ResponseEntity<String> getByLocation(@PathVariable String location) throws JsonProcessingException {
        String value = redisTemplate.opsForValue().get(location);
        if (value != null) {
            return ResponseEntity.status(HttpStatus.OK).body(value);
        }

        ResponseEntity<String> response = weatherRepository.findForLocation(location);

        if (response.getStatusCode().isSameCodeAs(HttpStatus.OK)) {
            String body = response.getBody() != null ? response.getBody() : "No data was found for this location.";
            redisTemplate.opsForValue().set(location, body, REDIS_EXPIRATION);
            return ResponseEntity.status(HttpStatus.OK).body(body);
        }

        if (response.getStatusCode().isSameCodeAs(HttpStatus.BAD_REQUEST)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid location was provided.");
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error. Contact administrator, please.");
    }
}
