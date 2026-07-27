package com.example.travellingapp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

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
    private Instant createdDate;

    public AccommodationEntity(String accommodationName, Instant createdDate) {
        this.accommodationName = accommodationName;
        this.createdDate = createdDate;
    }

    public AccommodationEntity() {

    }
}