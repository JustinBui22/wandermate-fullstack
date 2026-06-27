package com.example.travellingapp.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class CollaborationSummaryResponseDTO {
    private long pendingInvitationCount;
    private long pendingOwnedTripJoinRequestCount;
    private long totalPendingActionCount;
    private Map<Long, Long> tripPendingJoinRequestCounts;

    public CollaborationSummaryResponseDTO() {
    }

    public CollaborationSummaryResponseDTO(
            long pendingInvitationCount,
            long pendingOwnedTripJoinRequestCount,
            long totalPendingActionCount,
            Map<Long, Long> tripPendingJoinRequestCounts
    ) {
        this.pendingInvitationCount = pendingInvitationCount;
        this.pendingOwnedTripJoinRequestCount = pendingOwnedTripJoinRequestCount;
        this.totalPendingActionCount = totalPendingActionCount;
        this.tripPendingJoinRequestCounts = tripPendingJoinRequestCounts;
    }
}