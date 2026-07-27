package com.example.travellingapp.validator;

import com.example.travellingapp.dto.request.create.CreateDestinationDTO;
import com.example.travellingapp.dto.request.create.CreateTripDTO;
import com.example.travellingapp.entity.DestinationEntity;
import com.example.travellingapp.exception_handler.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static com.example.travellingapp.enums.ErrorCodeEnum.ACTIVITY_OUTSIDE_DESTINATION_RANGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DateTimeConsistencyTest {

    private final TripValidator tripValidator = new TripValidator();
    private final DestinationValidator destinationValidator = new DestinationValidator();
    private final ActivityValidator activityValidator = new ActivityValidator();

    @Test
    void tripValidator_shouldAllowSameDayCalendarTrip() {
        CreateTripDTO request = new CreateTripDTO();
        request.setTripName("Adelaide day trip");
        request.setDestination("Adelaide");
        request.setStartDate(LocalDate.of(2027, 4, 1));
        request.setEndDate(LocalDate.of(2027, 4, 1));

        assertThatCode(() -> tripValidator.validateCreateInput(request))
                .doesNotThrowAnyException();
    }

    @Test
    void destinationValidator_shouldAllowSameDayCalendarDestination() {
        CreateDestinationDTO request = new CreateDestinationDTO();
        request.setDestinationName("Kyoto");
        request.setStartDate(LocalDate.of(2027, 4, 5));
        request.setEndDate(LocalDate.of(2027, 4, 5));

        assertThatCode(() -> destinationValidator.validateCreateInput(1L, request))
                .doesNotThrowAnyException();
    }

    @Test
    void activityValidator_shouldAllowLocalActivityOnDestinationEndDate() {
        DestinationEntity destination = new DestinationEntity();
        destination.setStartDate(LocalDate.of(2027, 4, 4));
        destination.setEndDate(LocalDate.of(2027, 4, 5));

        assertThatCode(() -> activityValidator.validateActivityInsideDestination(
                LocalDateTime.of(2027, 4, 5, 20, 0),
                LocalDateTime.of(2027, 4, 5, 22, 0),
                destination
        )).doesNotThrowAnyException();
    }

    @Test
    void activityValidator_shouldRejectActivityAfterDestinationEndDate() {
        DestinationEntity destination = new DestinationEntity();
        destination.setStartDate(LocalDate.of(2027, 4, 4));
        destination.setEndDate(LocalDate.of(2027, 4, 5));

        assertThatThrownBy(() -> activityValidator.validateActivityInsideDestination(
                LocalDateTime.of(2027, 4, 5, 23, 0),
                LocalDateTime.of(2027, 4, 6, 1, 0),
                destination
        ))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCodeEnum())
                                .isEqualTo(ACTIVITY_OUTSIDE_DESTINATION_RANGE)
                );
    }
}
