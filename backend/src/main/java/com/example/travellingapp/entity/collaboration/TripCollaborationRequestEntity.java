package com.example.travellingapp.entity.collaboration;

import com.example.travellingapp.entity.TripEntity;
import com.example.travellingapp.entity.User;
import com.example.travellingapp.enums.TripEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "trip_collaboration_requests")
@Getter
@Setter
public class TripCollaborationRequestEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 14L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    private Long requestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private TripEntity trip;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_user_id", nullable = false)
    private User requester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_user_id", nullable = false)
    private User targetUser;

    @Enumerated(EnumType.STRING)
    @Column(name = "requested_role", nullable = false, length = 20)
    private TripEnum requestedRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_type", nullable = false, length = 30)
    private TripEnum requestType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TripEnum status;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "modified_date")
    private LocalDateTime modifiedDate;

    @Column(name = "responded_date")
    private LocalDateTime respondedDate;

    public TripCollaborationRequestEntity() {
    }

    public TripCollaborationRequestEntity(
            TripEntity trip,
            User requester,
            User targetUser,
            TripEnum requestedRole,
            TripEnum requestType,
            TripEnum status,
            LocalDateTime createdDate
    ) {
        this.trip = trip;
        this.requester = requester;
        this.targetUser = targetUser;
        this.requestedRole = requestedRole;
        this.requestType = requestType;
        this.status = status;
        this.createdDate = createdDate;
    }
}