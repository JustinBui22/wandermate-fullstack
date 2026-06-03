package com.example.travellingapp.repository;

import com.example.travellingapp.entity.RestaurantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RestaurantRepository extends JpaRepository<RestaurantEntity, Long> {
    List<RestaurantEntity> findAllByRestaurantNameContainingIgnoreCase (String keyword);

    List<RestaurantEntity> findTop10ByRestaurantNameStartingWithIgnoreCaseOrderByRestaurantNameAsc (String keyword);
}
