package com.example.travellingapp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "error_codes")
@Getter
@Setter
public class ErrorCodeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "errorId")
    private int errorId;

    @Column(name = "error_code", nullable = false)
    private String errorCode;

    @Column(name = "error_message", nullable = false)
    private String errorMessage;

    @Column(name = "error_description")
    private String errorDescription;

    @Column(name = "created_date")
    private Instant createdDate;

    @Column(name = "modified_date")
    private Instant modifiedDate;

    @Column(name = "error_type")
    private String errorType;

    @Column(name = "error_enum", nullable = false)
    private String errorEnum;

    @Column(name = "flow")
    private String flow;
}