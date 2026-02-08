package me.dhanur.tasks.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.dhanur.tasks.dto.ErrorResponse;
import me.dhanur.tasks.dto.TaskRequest;
import me.dhanur.tasks.dto.TaskResponse;
import me.dhanur.tasks.entity.TaskStatus;
import me.dhanur.tasks.service.TaskService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Task Management", description = "REST API for managing tasks")
public class TaskController {

        private final TaskService taskService;

        @PostMapping
        @Operation(summary = "Create a new task", description = "Creates a new task with the provided details")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Task created successfully", content = @Content(schema = @Schema(implementation = TaskResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid request parameters", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
        public ResponseEntity<TaskResponse> createTask(
                        @Valid @RequestBody TaskRequest request) {

                log.info("Received request to create task: {}", request.getTitle());
                TaskResponse response = taskService.createTask(request);
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }

        @GetMapping
        @Operation(summary = "Get all tasks", description = "Retrieves all tasks with pagination and optional filtering")
        @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully")
        public ResponseEntity<Page<TaskResponse>> getAllTasks(
                        @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,

                        @Parameter(description = "Number of items per page") @RequestParam(defaultValue = "10") int size,

                        @Parameter(description = "Sort field and direction (e.g., createdAt,desc)") @RequestParam(defaultValue = "createdAt,desc") String sort,

                        @Parameter(description = "Filter by task status") @RequestParam(required = false) TaskStatus status) {

                log.info("Received request to get all tasks - page: {}, size: {}, status: {}", page, size, status);

                String[] sortParams = sort.split(",");
                Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc")
                                ? Sort.Direction.ASC
                                : Sort.Direction.DESC;

                Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParams[0]));
                Page<TaskResponse> tasks = taskService.getAllTasks(pageable, status);

                return ResponseEntity.ok(tasks);
        }

        @GetMapping("/{id}")
        @Operation(summary = "Get task by ID", description = "Retrieves a single task by its ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Task found", content = @Content(schema = @Schema(implementation = TaskResponse.class))),
                        @ApiResponse(responseCode = "404", description = "Task not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
        public ResponseEntity<TaskResponse> getTaskById(
                        @Parameter(description = "Task ID", required = true) @PathVariable Long id) {

                log.info("Received request to get task with id: {}", id);
                TaskResponse response = taskService.getTaskById(id);
                return ResponseEntity.ok(response);
        }

        @PutMapping("/{id}")
        @Operation(summary = "Update task", description = "Updates an existing task's title and description")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Task updated successfully", content = @Content(schema = @Schema(implementation = TaskResponse.class))),
                        @ApiResponse(responseCode = "404", description = "Task not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid request parameters", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
        public ResponseEntity<TaskResponse> updateTask(
                        @Parameter(description = "Task ID", required = true) @PathVariable Long id,
                        @Valid @RequestBody TaskRequest request) {

                log.info("Received request to update task {}: {}", id, request.getTitle());
                TaskResponse response = taskService.updateTask(id, request);
                return ResponseEntity.ok(response);
        }

        @PatchMapping("/{id}/status")
        @Operation(summary = "Update task status", description = "Updates only the status of an existing task")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Task status updated successfully", content = @Content(schema = @Schema(implementation = TaskResponse.class))),
                        @ApiResponse(responseCode = "404", description = "Task not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
        public ResponseEntity<TaskResponse> updateTaskStatus(
                        @Parameter(description = "Task ID", required = true) @PathVariable Long id,

                        @Parameter(description = "New task status", required = true) @RequestParam TaskStatus status) {

                log.info("Received request to update status of task {} to {}", id, status);
                TaskResponse response = taskService.updateTaskStatus(id, status);
                return ResponseEntity.ok(response);
        }

        @DeleteMapping("/{id}")
        @Operation(summary = "Delete task", description = "Deletes a task by its ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "204", description = "Task deleted successfully"),
                        @ApiResponse(responseCode = "404", description = "Task not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
        public ResponseEntity<Void> deleteTask(
                        @Parameter(description = "Task ID", required = true) @PathVariable Long id) {

                log.info("Received request to delete task with id: {}", id);
                taskService.deleteTask(id);
                return ResponseEntity.noContent().build();
        }
}
