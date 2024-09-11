package nz.ac.canterbury.seng302.gardenersgrove.service;


import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;


/**
 * This code was partially generated by chatGPT
 * RateLimiterService is based on Token Bucket Algorithm which is for rate-limiting.
 * The main reason is when users put the input on location search, each keystroke sends a request to api server.
 * This code will help to control the ratio of sending requests from users.
 */
@Service
public class RateLimiterService {
    private Instant lastRefillTime;
    private String query;
    private final LocationService locationService;

    /**
     * Constructor of RateLimiterService
     */
    public RateLimiterService(LocationService locationService) {
        this.lastRefillTime = Instant.now();
        this.locationService = locationService;
    }


    public String sendRequest() throws IOException, InterruptedException {
        Instant now = Instant.now();
        long elapsedSeconds = Duration.between(lastRefillTime, now).toSeconds();
        if (elapsedSeconds >= 1) {
            lastRefillTime = now;
            HttpResponse<String> response = locationService.sendRequest(query);
            return response.body();
        }
        return "{\"status\":\"Searching\"}"; // Return a valid JSON response
    }

    public void setQuery(String query) {this.query = query;}

    public String getQuery() {return this.query;}

}