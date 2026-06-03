package com.example.travellingapp.repository;

import com.example.travellingapp.entity.EmailContentEntity;
import com.example.travellingapp.enums.EmailEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailRepository extends JpaRepository<EmailContentEntity, Long> {
    Optional<EmailContentEntity> findByEmailEnum(EmailEnum emailEnum);
}
