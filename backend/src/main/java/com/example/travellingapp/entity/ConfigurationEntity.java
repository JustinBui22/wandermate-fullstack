package com.example.travellingapp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;



@Entity
@Table(name = "configuration")
@Getter
@Setter
public class ConfigurationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "config_id")
    private int configId;

    @Column(name = "config_code", nullable = false, unique = true)
    private String configCode;

    @Column(name = "config_value", nullable = false, columnDefinition = "TEXT")
    private String configValue;

    @Column(name = "config_message", nullable = false)
    private String configMessage;

    @Column(name = "created_date", unique = true)
    private LocalDateTime createdDate;

    @Column(name = "modified_date")
    private LocalDateTime modifiedDate;

    @Column(name = "config_type")
    private String configType;


    public ConfigurationEntity(String configCode, String configValue, LocalDateTime createdDate) {
        this.configCode = configCode;
        this.configValue = configValue;
        this.createdDate = createdDate;
    }

    public ConfigurationEntity() {

    }
}
