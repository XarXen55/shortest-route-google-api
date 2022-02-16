package com.example.egzamin.repository;

import com.example.egzamin.models.Locations;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LocationsRepository  extends JpaRepository<Locations,Long> {

    Locations findTopByOrderByIdDesc();
    List<Locations> findAllByOrderByIdAsc();

}
