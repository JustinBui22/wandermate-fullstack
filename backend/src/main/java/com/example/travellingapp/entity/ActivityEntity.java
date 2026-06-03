package com.example.travellingapp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "destination_activities")
@Getter
@Setter
public class ActivityEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 12L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "activity_id")
    private Long activityId;

    @Column(name = "activity_name", nullable = false)
    private String activityName;

    @Column(name = "location")
    private String location;

    @Column(name = "description")
    private String description;

    @Column(name = "start_date_time", nullable = false)
    private LocalDateTime startDateTime;

    @Column(name = "end_date_time", nullable = false)
    private LocalDateTime endDateTime;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "modified_date")
    private LocalDateTime modifiedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_id", nullable = false)
    private DestinationEntity destination;

    public ActivityEntity() {
    }

    public ActivityEntity(
            String activityName,
            String location,
            String description,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            LocalDateTime createdDate,
            DestinationEntity destination
    ) {
        this.activityName = activityName;
        this.location = location;
        this.description = description;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.createdDate = createdDate;
        this.destination = destination ;
    }
}

