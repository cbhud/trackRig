package me.cbhud.trackRig.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.OffsetDateTime;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiErrorResponse(
        String status,
        String message,
        OffsetDateTime timestamp,
        Map<String, String> fieldErrors
) {
    public ApiErrorResponse(String status, String message, OffsetDateTime timestamp) {
        this(status, message, timestamp, null);
    }
}