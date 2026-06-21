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
@Table(
        name = "trip_members",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_trip_members_trip_user",
                        columnNames = {"trip_id", "user_id"}
                )
        }
)
@Getter
@Setter
public class TripMemberEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 13L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trip_member_id")
    private Long tripMemberId;

    // One trip can have many members.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private TripEntity trip;

    // One user can be a member of many trips.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private TripEnum role;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "modified_date")
    private LocalDateTime modifiedDate;

    public TripMemberEntity() {
    }

    public TripMemberEntity(
            TripEntity trip,
            User user,
            TripEnum role,
            LocalDateTime createdDate
    ) {
        this.trip = trip;
        this.user = user;
        this.role = role;
        this.createdDate = createdDate;
    }
}

