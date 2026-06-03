package com.example.travellingapp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "Restaurants")
@Getter
@Setter
public class RestaurantEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 12L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "restaurant_id")
    private int restaurantId;

    @Column(name = "restaurant_name", nullable = false, unique = true)
    private String restaurantName;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    public RestaurantEntity(String restaurantName, LocalDateTime createdDate) {
        this.restaurantName = restaurantName;
        this.createdDate = createdDate;
    }

    public RestaurantEntity() {

    }
}
