package com.example.travellingapp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "accommodations")
@Getter
@Setter
public class AccommodationEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 12L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "accommodation_id")
    private int accommodationId;

    @Column(name = "accommodation_name", nullable = false, unique = true)
    private String accommodationName;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    public AccommodationEntity(String accommodationName, LocalDateTime createdDate) {
        this.accommodationName = accommodationName;
        this.createdDate = createdDate;
    }

    public AccommodationEntity() {

    }
}
