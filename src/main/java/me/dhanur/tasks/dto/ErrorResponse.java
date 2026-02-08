package me.dhanur.tasks.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Error response object")
public class ErrorResponse {

    @Schema(description = "Timestamp of the error", example = "2026-02-08T10:30:00")
    private LocalDateTime timestamp;

    @Schema(description = "HTTP status code", example = "404")
    private int status;

    @Schema(description = "Error type", example = "Not Found")
    private String error;

    @Schema(description = "Error message", example = "Task with id 5 not found")
    private String message;

    @Schema(description = "Request path", example = "/api/v1/tasks/5")
    private String path;

    @Schema(description = "Additional error details (e.g., validation errors)")
    private Map<String, String> details;
}
