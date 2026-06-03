package com.example.travellingapp.repository;

import com.example.travellingapp.entity.AccommodationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccommodationRepository extends JpaRepository<AccommodationEntity, Long> {
    List<AccommodationEntity> findAllByAccommodationNameContainingIgnoreCase (String keyword);

    List<AccommodationEntity> findTop10ByAccommodationNameStartingWithIgnoreCaseOrderByAccommodationNameAsc(String keyword);
}
