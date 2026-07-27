package com.example.travellingapp.entity;

import com.example.travellingapp.enums.TripEnum;
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
@Table(
        name = "trips",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_trips_user_trip_name",
                        columnNames = {"user_id", "trip_name"}
                )
        }
)
@Getter
@Setter
public class TripEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 12L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trip_id")
    private Long tripId;

    // A user cannot have trips with same name
    @Column(name = "trip_name", nullable = false)
    private String tripName;

    @Column(name = "destination", nullable = false)
    private String destination;

    @Column(name = "created_date", nullable = false)
    private Instant createdDate;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "cover_image_url", length = 500)
    private String coverImageUrl;

    @Column(name = "cover_image_public_id", length = 500)
    private String coverImagePublicId;

    // Many trips can belong to one user, but each trip belongs to one user
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "modified_date")
    private Instant modifiedDate;

    // One trip can have multiple destinations, but each destination belongs to one trip
    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DestinationEntity> destinations = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "trip_status", nullable = false, length = 20)
    private TripEnum tripStatus = TripEnum.PLANNING;

    public TripEntity(String tripName, String destination, Instant createdDate, LocalDate startDate, LocalDate endDate, User user) {
        this.tripName = tripName;
        this.destination = destination;
        this.createdDate = createdDate;
        this.startDate = startDate;
        this.endDate = endDate;
        this.user = user;
    }

    public TripEntity(String tripName, String destination, Instant createdDate, LocalDate startDate, LocalDate endDate, Instant modifiedDate, User user) {
        this.tripName = tripName;
        this.destination = destination;
        this.createdDate = createdDate;
        this.startDate = startDate;
        this.endDate = endDate;
        this.modifiedDate = modifiedDate;
        this.user = user;
    }

    public TripEntity() {

    }
}