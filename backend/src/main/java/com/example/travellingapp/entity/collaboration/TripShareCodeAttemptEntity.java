package com.example.travellingapp.entity.collaboration;

import com.example.travellingapp.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "trip_share_code_attempts")
@Getter
@Setter
public class TripShareCodeAttemptEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 19L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attempt_id")
    private Long attemptId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "restricted_until")
    private Instant restrictedUntil;

    @Column(name = "last_attempt_date")
    private Instant lastAttemptDate;

    @Column(name = "created_date", nullable = false)
    private Instant createdDate;

    @Column(name = "modified_date")
    private Instant modifiedDate;

    public TripShareCodeAttemptEntity() {
    }

    public TripShareCodeAttemptEntity(
            User user,
            int retryCount,
            Instant createdDate
    ) {
        this.user = user;
        this.retryCount = retryCount;
        this.createdDate = createdDate;
    }
}