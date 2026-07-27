package com.example.travellingapp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
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
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "destination_order")
    private Integer destinationOrder;

    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(name = "created_date", nullable = false)
    private Instant createdDate;

    @Column(name = "modified_date")
    private Instant modifiedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private TripEntity trip;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modified_by_user_id")
    private User modifiedBy;

    @OneToMany(mappedBy = "destination", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ActivityEntity> activities = new ArrayList<>();

    public DestinationEntity() {
    }

    public DestinationEntity(
            String destinationName,
            LocalDate startDate,
            LocalDate endDate,
            Integer destinationOrder,
            String notes,
            Instant createdDate,
            Instant modifiedDate,
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
            this.createdDate = Instant.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.modifiedDate = Instant.now();
    }
}