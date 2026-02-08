package me.dhanur.tasks.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.dhanur.tasks.entity.TaskStatus;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Task response object")
public class TaskResponse {

    @Schema(description = "Task ID", example = "1")
    private Long id;

    @Schema(description = "Task title", example = "Complete project documentation")
    private String title;

    @Schema(description = "Task description", example = "Write comprehensive README and API documentation")
    private String description;

    @Schema(description = "Task status", example = "TODO")
    private TaskStatus status;

    @Schema(description = "Creation timestamp", example = "2026-02-08T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp", example = "2026-02-08T15:45:00")
    private LocalDateTime updatedAt;
}
