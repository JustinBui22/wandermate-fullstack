package com.example.travellingapp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "Cities")
@Getter
@Setter
public class CityEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 12L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "city_id")
    private int cityId;

    @Column(name = "city_name", nullable = false, unique = true)
    private String cityName;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    public CityEntity(String cityName, LocalDateTime createdDate) {
        this.cityName = cityName;
        this.createdDate = createdDate;
    }

    public CityEntity() {

    }
}
