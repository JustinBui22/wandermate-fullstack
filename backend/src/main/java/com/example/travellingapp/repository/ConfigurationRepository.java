package com.example.travellingapp.repository;

import com.example.travellingapp.entity.ConfigurationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConfigurationRepository extends JpaRepository<ConfigurationEntity, Integer> {

    Optional<ConfigurationEntity> findByConfigCode(String configCode);
}
