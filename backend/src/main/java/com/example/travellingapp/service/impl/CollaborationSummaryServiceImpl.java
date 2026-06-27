package com.example.travellingapp.service.impl;

import com.example.travellingapp.dto.response.CollaborationSummaryResponseDTO;
import com.example.travellingapp.enums.TripEnum;
import com.example.travellingapp.exception_handler.exception.BusinessException;
import com.example.travellingapp.repository.ErrorCodeRepository;
import com.example.travellingapp.repository.UserRepository;
import com.example.travellingapp.repository.collaboration.TripCollaborationRequestRepository;
import com.example.travellingapp.repository.collaboration.projection.TripPendingJoinRequestCountProjection;
import com.example.travellingapp.response_template.CompleteResponse;
import com.example.travellingapp.security.data_security.AuthenticatedUserProvider;
import com.example.travellingapp.service.CollaborationSummaryService;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;

import static com.example.travellingapp.enums.CommonEnum.COMMON;
import static com.example.travellingapp.enums.CommonEnum.TRIP_MEMBER;
import static com.example.travellingapp.enums.ErrorCodeEnum.*;
import static com.example.travellingapp.response_template.CompleteResponse.getCompleteResponse;

@Service
@Log4j2
public class CollaborationSummaryServiceImpl implements CollaborationSummaryService {

    private final TripCollaborationRequestRepository tripCollaborationRequestRepository;
    private final UserRepository userRepository;
    private final ErrorCodeRepository errorCodeRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    public CollaborationSummaryServiceImpl(
            TripCollaborationRequestRepository tripCollaborationRequestRepository,
            UserRepository userRepository,
            ErrorCodeRepository errorCodeRepository,
            AuthenticatedUserProvider authenticatedUserProvider
    ) {
        this.tripCollaborationRequestRepository = tripCollaborationRequestRepository;
        this.userRepository = userRepository;
        this.errorCodeRepository = errorCodeRepository;
        this.authenticatedUserProvider = authenticatedUserProvider;
    }

    @Override
    public CompleteResponse<Object> getCollaborationSummary() {
        try {
            // Get current logged-in username
            String username = authenticatedUserProvider.getUsername();
            // Check if user exists and is active
            userRepository.findByUsernameAndActive(username)
                    .orElseThrow(() -> new BusinessException(USER_NOT_FOUND, COMMON.name()));
            // Count pending invitations received by current user
            long pendingInvitationCount =
                    tripCollaborationRequestRepository.countByTargetUser_UsernameAndRequestTypeAndStatus(
                            username,
                            TripEnum.INVITATION,
                            TripEnum.PENDING
                    );
            // Count pending join requests for trips owned by current user
            long pendingOwnedTripJoinRequestCount =
                    tripCollaborationRequestRepository.countByTrip_User_UsernameAndRequestTypeAndStatus(
                            username,
                            TripEnum.JOIN_REQUEST,
                            TripEnum.PENDING
                    );
            // Count pending join requests per owned trip
            Map<Long, Long> tripPendingJoinRequestCounts =
                    tripCollaborationRequestRepository
                            .countPendingJoinRequestsByOwnedTrip(
                                    username,
                                    TripEnum.JOIN_REQUEST,
                                    TripEnum.PENDING
                            )
                            .stream()
                            .collect(Collectors.toMap(
                                    TripPendingJoinRequestCountProjection::getTripId,
                                    TripPendingJoinRequestCountProjection::getPendingCount
                            ));
            // Total actions that need current user's attention
            long totalPendingActionCount =
                    pendingInvitationCount + pendingOwnedTripJoinRequestCount;

            CollaborationSummaryResponseDTO responseDTO =
                    new CollaborationSummaryResponseDTO(
                            pendingInvitationCount,
                            pendingOwnedTripJoinRequestCount,
                            totalPendingActionCount,
                            tripPendingJoinRequestCounts
                    );

            return getCompleteResponse(
                    errorCodeRepository,
                    COLLABORATION_SUMMARY_RETRIEVED_SUCCESS,
                    TRIP_MEMBER.name(),
                    responseDTO
            );

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error occurred while getting collaboration summary", e);
            throw new BusinessException(INTERNAL_SERVER_ERROR, COMMON.name());
        }
    }
}