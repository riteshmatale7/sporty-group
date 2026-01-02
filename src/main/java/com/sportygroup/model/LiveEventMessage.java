package com.sportygroup.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Message published to the broker.
 * <p>
 * In a real system you might use Avro/Protobuf + schema registry.
 * For the assignment we keep it JSON-serializable POJO.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LiveEventMessage {

    private String eventId;
    private String currentScore;
    private Instant observedAt;

}
