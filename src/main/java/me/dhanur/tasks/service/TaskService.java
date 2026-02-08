package me.dhanur.tasks.service;

import me.dhanur.tasks.dto.TaskRequest;
import me.dhanur.tasks.dto.TaskResponse;
import me.dhanur.tasks.entity.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TaskService {

    /**
     * Create a new task
     */
    TaskResponse createTask(TaskRequest request);

    /**
     * Get task by ID
     */
    TaskResponse getTaskById(Long id);

    /**
     * Get all tasks with pagination and optional filtering
     */
    Page<TaskResponse> getAllTasks(Pageable pageable, TaskStatus status);

    /**
     * Update task status
     */
    TaskResponse updateTaskStatus(Long id, TaskStatus status);

    /**
     * Update complete task
     */
    TaskResponse updateTask(Long id, TaskRequest request);

    /**
     * Delete task by ID
     */
    void deleteTask(Long id);
}
