package com.example.travellingapp.repository.collaboration;

import com.example.travellingapp.entity.collaboration.TripShareCodeEntity;
import com.example.travellingapp.enums.TripEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TripShareCodeRepository extends JpaRepository<TripShareCodeEntity, Long> {

    Optional<TripShareCodeEntity> findByCode(String code);

    boolean existsByCode(String code);

    List<TripShareCodeEntity> findByTrip_TripIdAndCodeStatus(
            Long tripId,
            TripEnum codeStatus
    );

    Optional<TripShareCodeEntity> findFirstByTrip_TripIdAndCodeStatusOrderByCreatedDateDesc(
            Long tripId,
            TripEnum codeStatus
    );

    Optional<TripShareCodeEntity> findFirstByTrip_TripIdOrderByCreatedDateDesc(
            Long tripId
    );
}