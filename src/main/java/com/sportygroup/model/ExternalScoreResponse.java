package com.sportygroup.model;

import lombok.Data;

/**
 * DTO representing the response from the external REST API.
 * <p>
 * Example payload:
 * {
 * "eventId": "1234",
 * "currentScore": "0:0"
 * }
 */
@Data
public class ExternalScoreResponse {

    private String eventId;
    private String currentScore;
}

