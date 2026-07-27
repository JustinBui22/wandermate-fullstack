package com.example.travellingapp.entity.collaboration;

import com.example.travellingapp.entity.TripEntity;
import com.example.travellingapp.entity.User;
import com.example.travellingapp.enums.TripEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "trip_share_codes")
@Getter
@Setter
public class TripShareCodeEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 18L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "share_code_id")
    private Long shareCodeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private TripEntity trip;

    @Column(name = "code", nullable = false, unique = true, length = 32)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdByUser;

    @Enumerated(EnumType.STRING)
    @Column(name = "default_role", nullable = false, length = 20)
    private TripEnum defaultRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "code_status", nullable = false, length = 20)
    private TripEnum codeStatus;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "used_by_user_id")
    private User usedByUser;

    @Column(name = "used_date")
    private Instant usedDate;

    @Column(name = "created_date", nullable = false)
    private Instant createdDate;

    @Column(name = "modified_date")
    private Instant modifiedDate;

    public TripShareCodeEntity() {
    }

    public TripShareCodeEntity(
            TripEntity trip,
            String code,
            User createdByUser,
            TripEnum defaultRole,
            TripEnum codeStatus,
            Instant expiresAt,
            Instant createdDate
    ) {
        this.trip = trip;
        this.code = code;
        this.createdByUser = createdByUser;
        this.defaultRole = defaultRole;
        this.codeStatus = codeStatus;
        this.expiresAt = expiresAt;
        this.createdDate = createdDate;
    }
}