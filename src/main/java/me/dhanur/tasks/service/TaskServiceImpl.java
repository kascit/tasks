package me.dhanur.tasks.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.dhanur.tasks.dto.TaskRequest;
import me.dhanur.tasks.dto.TaskResponse;
import me.dhanur.tasks.entity.Task;
import me.dhanur.tasks.entity.TaskStatus;
import me.dhanur.tasks.exception.ResourceNotFoundException;
import me.dhanur.tasks.repository.TaskRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;

    @Override
    public TaskResponse createTask(TaskRequest request) {
        log.info("Creating new task with title: {}", request.getTitle());

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .status(TaskStatus.TODO)
                .build();

        Task savedTask = taskRepository.save(task);
        log.info("Task created successfully with id: {}", savedTask.getId());

        return mapToResponse(savedTask);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponse getTaskById(Long id) {
        log.debug("Fetching task with id: {}", id);

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", id));

        return mapToResponse(task);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TaskResponse> getAllTasks(Pageable pageable, TaskStatus status) {
        log.debug("Fetching tasks with pagination - page: {}, size: {}, status: {}",
                pageable.getPageNumber(), pageable.getPageSize(), status);

        Page<Task> tasks;

        if (status != null) {
            tasks = taskRepository.findByStatus(status, pageable);
        } else {
            tasks = taskRepository.findAll(pageable);
        }

        log.debug("Found {} tasks", tasks.getTotalElements());
        return tasks.map(this::mapToResponse);
    }

    @Override
    public TaskResponse updateTaskStatus(Long id, TaskStatus status) {
        log.info("Updating status of task {} to {}", id, status);

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", id));

        task.setStatus(status);
        Task updatedTask = taskRepository.save(task);

        log.info("Task {} status updated successfully", id);
        return mapToResponse(updatedTask);
    }

    @Override
    public TaskResponse updateTask(Long id, TaskRequest request) {
        log.info("Updating task with id: {}", id);

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", id));

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());

        Task updatedTask = taskRepository.save(task);
        log.info("Task {} updated successfully", id);

        return mapToResponse(updatedTask);
    }

    @Override
    public void deleteTask(Long id) {
        log.info("Deleting task with id: {}", id);

        if (!taskRepository.existsById(id)) {
            throw new ResourceNotFoundException("Task", id);
        }

        taskRepository.deleteById(id);
        log.info("Task {} deleted successfully", id);
    }

    /**
     * Map Task entity to TaskResponse DTO
     */
    private TaskResponse mapToResponse(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }
}
