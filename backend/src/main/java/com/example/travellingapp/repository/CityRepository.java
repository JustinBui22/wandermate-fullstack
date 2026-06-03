package com.example.travellingapp.repository;

import com.example.travellingapp.entity.CityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CityRepository extends JpaRepository<CityEntity, Long> {
    List<CityEntity> findAllByCityNameContainingIgnoreCase (String keyword);

    List<CityEntity> findTop10ByCityNameStartingWithIgnoreCaseOrderByCityNameAsc(String keyword);

}
