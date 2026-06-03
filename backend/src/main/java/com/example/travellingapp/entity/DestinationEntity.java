package com.example.travellingapp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "trip_destinations")
@Getter
@Setter
public class DestinationEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 13L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "destination_id")
    private Long destinationId;

    @Column(name = "destination_name", nullable = false, length = 150)
    private String destinationName;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "destination_order")
    private Integer destinationOrder;

    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "modified_date")
    private LocalDateTime modifiedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private TripEntity trip;

    @OneToMany(mappedBy = "destination", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ActivityEntity> activities = new ArrayList<>();

    public DestinationEntity() {
    }

    public DestinationEntity(
            String destinationName,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Integer destinationOrder,
            String notes,
            LocalDateTime createdDate,
            LocalDateTime modifiedDate,
            TripEntity trip
    ) {
        this.destinationName = destinationName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.destinationOrder = destinationOrder;
        this.notes = notes;
        this.createdDate = createdDate;
        this.modifiedDate = modifiedDate;
        this.trip = trip;
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdDate == null) {
            this.createdDate = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.modifiedDate = LocalDateTime.now();
    }
}