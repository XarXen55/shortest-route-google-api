package com.example.egzamin.models;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Data
@Entity
public class LocationsSorted {

    @Id
    private Long id;

    @NotNull
    private String Adres;

    public LocationsSorted (String adres){
        this.Adres = adres;
    }

}
