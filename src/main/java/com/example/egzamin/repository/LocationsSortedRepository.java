package com.example.egzamin.repository;

import com.example.egzamin.models.LocationsSorted;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LocationsSortedRepository  extends JpaRepository<LocationsSorted,Long> {

    //List<LocationsSorted> findAllByOrderByIdAsc();

}