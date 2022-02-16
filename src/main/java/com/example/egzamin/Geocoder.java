package com.example.egzamin;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Geocoder {

    private static final String GEOCODING_RESOURCE = "https://maps.googleapis.com/maps/api/geocode/json?";
    private static final String API_KEY = "";

    public String GeocodeSync(String query) throws IOException, InterruptedException, JSONException {

        HttpClient httpClient = HttpClient.newHttpClient();

        String encodedQuery = URLEncoder.encode(query,"UTF-8");
        String requestUri = GEOCODING_RESOURCE + "address=" + encodedQuery + "&key=" + API_KEY;

        HttpRequest geocodingRequest = HttpRequest.newBuilder().GET().uri(URI.create(requestUri))
                .timeout(Duration.ofMillis(2000)).build();

        HttpResponse geocodingResponse = httpClient.send(geocodingRequest,
                HttpResponse.BodyHandlers.ofString());
        //return (String) geocodingResponse.body();
        JSONObject obj = new JSONObject((String) geocodingResponse.body());
        JSONArray arr = obj.getJSONArray("results");
        String pageName = arr.getJSONObject(0).getString("place_id");
       return pageName;

    }

}