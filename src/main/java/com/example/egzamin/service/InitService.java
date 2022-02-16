package com.example.egzamin.service;

import com.example.egzamin.repository.LocationsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
@Slf4j
public class InitService {

    @Autowired
    LocationsRepository locationsRepository;

    @PostConstruct
    private void init() {
        log.info("init");


    }
}
