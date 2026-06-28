package com.example.travellingapp.service.impl;

import com.example.travellingapp.dto.request.AddTripMemberDTO;
import com.example.travellingapp.dto.request.update.UpdateTripMemberRoleDTO;
import com.example.travellingapp.dto.response.TripMemberResponseDTO;
import com.example.travellingapp.entity.TripEntity;
import com.example.travellingapp.entity.User;
import com.example.travellingapp.entity.collaboration.TripMemberEntity;
import com.example.travellingapp.enums.TripEnum;
import com.example.travellingapp.exception_handler.exception.BusinessException;
import com.example.travellingapp.mapper.TripMemberMapper;
import com.example.travellingapp.repository.ErrorCodeRepository;
import com.example.travellingapp.repository.UserRepository;
import com.example.travellingapp.repository.collaboration.TripMemberRepository;
import com.example.travellingapp.response_template.CompleteResponse;
import com.example.travellingapp.security.data_security.AuthenticatedUserProvider;
import com.example.travellingapp.service.TripAccessService;
import com.example.travellingapp.service.TripMemberService;
import com.example.travellingapp.validator.TripMemberValidator;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static com.example.travellingapp.enums.CommonEnum.COMMON;
import static com.example.travellingapp.enums.CommonEnum.TRIP_MEMBER;
import static com.example.travellingapp.enums.ErrorCodeEnum.*;
import static com.example.travellingapp.response_template.CompleteResponse.getCompleteResponse;

@Service
@Log4j2
public class TripMemberServiceImpl implements TripMemberService {
    private final TripMemberRepository tripMemberRepository;
    private final UserRepository userRepository;
    private final ErrorCodeRepository errorCodeRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final TripAccessService tripAccessService;
    private final TripMemberMapper tripMemberMapper;
    private final TripMemberValidator tripMemberValidator;

    public TripMemberServiceImpl(
            TripMemberRepository tripMemberRepository,
            UserRepository userRepository,
            ErrorCodeRepository errorCodeRepository,
            AuthenticatedUserProvider authenticatedUserProvider,
            TripAccessService tripAccessService,
            TripMemberMapper tripMemberMapper,
            TripMemberValidator tripMemberValidator) {
        this.tripMemberRepository = tripMemberRepository;
        this.userRepository = userRepository;
        this.errorCodeRepository = errorCodeRepository;
        this.authenticatedUserProvider = authenticatedUserProvider;
        this.tripAccessService = tripAccessService;
        this.tripMemberMapper = tripMemberMapper;
        this.tripMemberValidator = tripMemberValidator;
    }

    @Override
    public CompleteResponse<Object> getTripMembers(Long tripId) {
        try {
            log.info("Getting trip members for tripId: {}", tripId);

            // Validate trip ID
            tripMemberValidator.validateTripId(tripId);

            String username = authenticatedUserProvider.getUsername();

            // Check if current user can view this trip
            tripAccessService.assertCanView(tripId, username);

            // Get all members in this trip
            List<TripMemberResponseDTO> members = tripMemberRepository
                    .findAllByTrip_TripId(tripId)
                    .stream()
                    .map(tripMemberMapper::toResponseDTO)
                    .toList();

            return getCompleteResponse(
                    errorCodeRepository,
                    TRIP_MEMBERS_RETRIEVED_SUCCESS,
                    TRIP_MEMBER.name(),
                    members
            );

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error occurred while getting trip members", e);
            throw new BusinessException(INTERNAL_SERVER_ERROR, COMMON.name());
        }
    }

    @Override
    public CompleteResponse<Object> addTripMember(Long tripId, AddTripMemberDTO addTripMemberDTO) {
        try {
            log.info("Adding trip member for tripId: {}", tripId);

            // Validate input and get normalized username
            String targetUsername = tripMemberValidator.validateAddTripMemberInput(tripId, addTripMemberDTO);
            String currentUsername = authenticatedUserProvider.getUsername();
            // Only trip owner can add members
            TripEntity trip = tripAccessService.getTripIfOwner(tripId, currentUsername);

            // Find target user
            User targetUser = userRepository.findByUsernameAndActive(targetUsername).orElseThrow(() -> new BusinessException(USER_NOT_FOUND, COMMON.name()));

            // Validate role
            TripEnum role = TripEnum.valueOf(addTripMemberDTO.getRole().trim().toUpperCase());

            // Check if user is already a trip member
            boolean alreadyMember = tripMemberRepository.existsByTrip_TripIdAndUser_UserId(tripId, targetUser.getUserId());

            if (alreadyMember) {
                log.error("User {} is already a member of tripId: {}", targetUsername, tripId);
                throw new BusinessException(TRIP_MEMBER_ALREADY_EXISTS, TRIP_MEMBER.name());
            }

            // Create trip member
            TripMemberEntity tripMember = new TripMemberEntity(
                    trip,
                    targetUser,
                    role,
                    LocalDateTime.now()
            );
            tripMemberRepository.save(tripMember);
            return getCompleteResponse(
                    errorCodeRepository,
                    TRIP_MEMBER_ADDED_SUCCESS,
                    TRIP_MEMBER.name(),
                    tripMemberMapper.toResponseDTO(tripMember)
            );
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error occurred while adding trip member", e);
            throw new BusinessException(INTERNAL_SERVER_ERROR, COMMON.name());
        }
    }

    @Override
    public CompleteResponse<Object> updateTripMemberRole(Long tripId, Long tripMemberId, UpdateTripMemberRoleDTO updateTripMemberRoleDTO) {
        try {
            log.info("Updating trip member role for tripId: {}, tripMemberId: {}", tripId, tripMemberId);
            // Validate and get normalized role
            TripEnum newRole = tripMemberValidator.validateUpdateTripMemberRoleInput(
                    tripId,
                    tripMemberId,
                    updateTripMemberRoleDTO
            );
            String currentUsername = authenticatedUserProvider.getUsername();
            // Only trip owner can update member role
            tripAccessService.assertIsOwner(tripId, currentUsername);

            // Find trip member
            TripMemberEntity tripMember = tripMemberRepository
                    .findByTripMemberIdAndTrip_TripId(tripMemberId, tripId)
                    .orElseThrow(() -> new BusinessException(TRIP_MEMBER_NOT_FOUND, TRIP_MEMBER.name()));

            // Prevent changing owner role
            if (tripMember.getRole() == TripEnum.OWNER) {
                log.error("Owner role cannot be changed for tripMemberId: {}", tripMemberId);
                throw new BusinessException(TRIP_OWNER_ROLE_CANNOT_BE_CHANGED, TRIP_MEMBER.name());
            }

            // Update trip member role
            tripMember.setRole(newRole);
            tripMember.setModifiedDate(LocalDateTime.now());

            tripMemberRepository.save(tripMember);
            return getCompleteResponse(
                    errorCodeRepository,
                    TRIP_MEMBER_ROLE_UPDATED_SUCCESS,
                    TRIP_MEMBER.name(),
                    tripMemberMapper.toResponseDTO(tripMember)
            );
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error occurred while updating trip member role", e);
            throw new BusinessException(INTERNAL_SERVER_ERROR, COMMON.name());
        }
    }

    @Override
    public CompleteResponse<Object> removeTripMember(Long tripId, Long tripMemberId) {
        try {
            log.info("Removing trip member for tripId: {}, tripMemberId: {}", tripId, tripMemberId);

            // Validate input
            tripMemberValidator.validateRemoveTripMemberInput(tripId, tripMemberId);
            String currentUsername = authenticatedUserProvider.getUsername();

            // Only trip owner can remove members
            tripAccessService.assertIsOwner(tripId, currentUsername);

            // Find trip member
            TripMemberEntity tripMember = tripMemberRepository
                    .findByTripMemberIdAndTrip_TripId(tripMemberId, tripId)
                    .orElseThrow(() -> new BusinessException(TRIP_MEMBER_NOT_FOUND, TRIP_MEMBER.name()));

            // Prevent removing owner
            if (tripMember.getRole() == TripEnum.OWNER) {
                log.error("Owner cannot be removed from tripId: {}", tripId);
                throw new BusinessException(TRIP_OWNER_CANNOT_BE_REMOVED, TRIP_MEMBER.name());
            }

            // Remove trip member
            tripMemberRepository.delete(tripMember);
            return getCompleteResponse(
                    errorCodeRepository,
                    TRIP_MEMBER_REMOVED_SUCCESS,
                    TRIP_MEMBER.name(),
                    null
            );
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error occurred while removing trip member", e);
            throw new BusinessException(INTERNAL_SERVER_ERROR, COMMON.name());
        }
    }
}