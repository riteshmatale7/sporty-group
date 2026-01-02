package com.sportygroup.model;

import com.sportygroup.enums.EventStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Request payload for POST /events/status.
 */
@Data
public class EventStatusRequest {

    @NotBlank
    private String eventId;

    @NotNull
    private EventStatus status;
}
