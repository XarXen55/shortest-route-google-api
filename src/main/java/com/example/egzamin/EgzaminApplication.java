package com.example.egzamin;

import com.google.maps.model.Distance;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.google.maps.DistanceMatrixApiRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import com.example.egzamin.DistanceMatrix;

@SpringBootApplication
public class EgzaminApplication {

	public static void main(String[] args) throws Exception {
		SpringApplication.run(EgzaminApplication.class, args);
	}

}
