package com.example.travellingapp.repository.collaboration;

import com.example.travellingapp.entity.collaboration.TripShareCodeEntity;
import com.example.travellingapp.enums.TripEnum;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TripShareCodeRepository extends JpaRepository<TripShareCodeEntity, Long> {

    Optional<TripShareCodeEntity> findByCode(String code);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT shareCode FROM TripShareCodeEntity shareCode WHERE shareCode.code = :code")
    Optional<TripShareCodeEntity> findByCodeForUpdate(@Param("code") String code);

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