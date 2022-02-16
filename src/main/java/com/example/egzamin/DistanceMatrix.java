package com.example.egzamin;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class DistanceMatrix {
    private static final String API_KEY = "";
    public static long[][] distances;
    public static long[][] times;
    public static String[] cities;
    public static int n;

    //downloading the data
    public static String getData(String source, String destination) throws Exception {
        var url = "https://maps.googleapis.com/maps/api/distancematrix/json?origins=place_id:" + source + "&destinations=place_id:" + destination + "&key=" + API_KEY;
        var request = HttpRequest.newBuilder().GET().uri(URI.create(url)).build();
        var client = HttpClient.newBuilder().build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString()).body();
//        System.out.println(response);
        return response;

    }

    public static void parse(String response,int i,int j){
        long distance = -1L;
        long time = -1L;
        //parsing json data and updating data
        {
            try {
                JSONParser jp = new JSONParser();
                JSONObject jo = (JSONObject) jp.parse(response);
                JSONArray ja = (JSONArray) jo.get("rows");
                jo = (JSONObject) ja.get(0);
                ja = (JSONArray) jo.get("elements");
                jo = (JSONObject) ja.get(0);
                JSONObject je = (JSONObject) jo.get("distance");
                JSONObject jf = (JSONObject) jo.get("duration");
                distance = (long) je.get("value");
                time = (long) jf.get("value");

                distances[i][j] =  distance;
                times[i][j] = time;

            } catch (Exception e) {
                System.out.println(e + " for " + cities[j]);
            }
        }
    }

    public static long[][] usage(List<String> adresses) throws Exception {
        n = adresses.size();
        distances = new long[n][n];
        times = new long[n][n];
        cities = new String[n];
        for(int x = 0; x < adresses.size(); x++)
        {
            cities[x] = adresses.get(x);
        }
        long[][] list_of_elements = new long[n][n];
        int count=0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
//                System.out.print(++count+"/100 ");
                if (i != j) {
                    String response = getData(cities[i], cities[j]);
                    parse(response, i, j);
                } else {
                    times[i][j] = 0;
                    distances[i][j] = 0;
                }
                list_of_elements[i][j] = distances[i][j];
            }
        }
        //Test wypisywania iteracji
        /*for(int x = 0; x < n; x++)
        {
            System.out.println("Iteracja" + x);
            for(int y = 0; y < n; y++)
            {
                System.out.println("Element " + y + ": " + list_of_elements[x][y]);
            }
        }*/
        return list_of_elements;
    }

    public static long[][] usage_time(List<String> adresses) throws Exception {
        n = adresses.size();
        distances = new long[n][n];
        times = new long[n][n];
        cities = new String[n];
        for(int x = 0; x < adresses.size(); x++)
        {
            cities[x] = adresses.get(x);
        }
        long[][] list_of_elements = new long[n][n];
        int count=0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
//                System.out.print(++count+"/100 ");
                if (i != j) {
                    String response = getData(cities[i], cities[j]);
                    parse(response, i, j);
                } else {
                    times[i][j] = 0;
                    distances[i][j] = 0;
                }
                list_of_elements[i][j] = times[i][j];
            }
        }
        //Test wypisywania iteracji
        /*for(int x = 0; x < n; x++)
        {
            System.out.println("Iteracja" + x);
            for(int y = 0; y < n; y++)
            {
                System.out.println("Element " + y + ": " + list_of_elements[x][y]);
            }
        }*/
        return list_of_elements;
    }

}
