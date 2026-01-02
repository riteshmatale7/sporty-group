package com.sportygroup.controller;

import com.sportygroup.enums.EventStatus;
import com.sportygroup.model.EventStatusRequest;
import com.sportygroup.service.EventLifecycleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoint that updates the in-memory state for events and triggers scheduling/cancellation.
 */
@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/events")

public class EventStatusController {

    private final EventLifecycleService lifecycleService;

    /**
     * Toggle event status.
     * <p>
     * Example:
     * {
     * "eventId": "1234",
     * "status": "LIVE"
     * }
     */
    @PostMapping("/status")
    public ResponseEntity<Void> updateStatus(@Valid @RequestBody EventStatusRequest request) {
        log.info("Received status update: eventId={}, status={}", request.getEventId(), request.getStatus());

        if (request.getStatus() == EventStatus.LIVE) {
            lifecycleService.markLive(request.getEventId());
        } else {
            lifecycleService.markNotLive(request.getEventId());
        }

        return ResponseEntity.accepted().build();
    }
}
