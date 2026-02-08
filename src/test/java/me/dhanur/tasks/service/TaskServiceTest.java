package me.dhanur.tasks.service;

import me.dhanur.tasks.dto.TaskRequest;
import me.dhanur.tasks.dto.TaskResponse;
import me.dhanur.tasks.entity.Task;
import me.dhanur.tasks.entity.TaskStatus;
import me.dhanur.tasks.exception.ResourceNotFoundException;
import me.dhanur.tasks.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskService Unit Tests")
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskServiceImpl taskService;

    private Task testTask;
    private TaskRequest testRequest;

    @BeforeEach
    void setUp() {
        testTask = Task.builder()
                .id(1L)
                .title("Test Task")
                .description("Test Description")
                .status(TaskStatus.TODO)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testRequest = TaskRequest.builder()
                .title("Test Task")
                .description("Test Description")
                .build();
    }

    @Test
    @DisplayName("Should create task successfully")
    void createTask_ShouldReturnTaskResponse() {
        // Given
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        // When
        TaskResponse response = taskService.createTask(testRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("Test Task");
        assertThat(response.getDescription()).isEqualTo("Test Description");
        assertThat(response.getStatus()).isEqualTo(TaskStatus.TODO);
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    @DisplayName("Should get task by ID successfully")
    void getTaskById_WhenTaskExists_ShouldReturnTaskResponse() {
        // Given
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

        // When
        TaskResponse response = taskService.getTaskById(1L);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("Test Task");
        verify(taskRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when task not found by ID")
    void getTaskById_WhenTaskNotExists_ShouldThrowException() {
        // Given
        when(taskRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> taskService.getTaskById(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Task");
        verify(taskRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should get all tasks with pagination")
    void getAllTasks_ShouldReturnPageOfTasks() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Task> tasks = List.of(testTask);
        Page<Task> taskPage = new PageImpl<>(tasks, pageable, tasks.size());

        when(taskRepository.findAll(pageable)).thenReturn(taskPage);

        // When
        Page<TaskResponse> response = taskService.getAllTasks(pageable, null);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getTitle()).isEqualTo("Test Task");
        verify(taskRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Should get all tasks filtered by status")
    void getAllTasks_WithStatusFilter_ShouldReturnFilteredTasks() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Task> tasks = List.of(testTask);
        Page<Task> taskPage = new PageImpl<>(tasks, pageable, tasks.size());

        when(taskRepository.findByStatus(TaskStatus.TODO, pageable)).thenReturn(taskPage);

        // When
        Page<TaskResponse> response = taskService.getAllTasks(pageable, TaskStatus.TODO);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getStatus()).isEqualTo(TaskStatus.TODO);
        verify(taskRepository, times(1)).findByStatus(TaskStatus.TODO, pageable);
        verify(taskRepository, never()).findAll(pageable);
    }

    @Test
    @DisplayName("Should update task status successfully")
    void updateTaskStatus_WhenTaskExists_ShouldReturnUpdatedTask() {
        // Given
        Task updatedTask = Task.builder()
                .id(1L)
                .title("Test Task")
                .description("Test Description")
                .status(TaskStatus.IN_PROGRESS)
                .createdAt(testTask.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();

        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(updatedTask);

        // When
        TaskResponse response = taskService.updateTaskStatus(1L, TaskStatus.IN_PROGRESS);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        verify(taskRepository, times(1)).findById(1L);
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when updating non-existent task status")
    void updateTaskStatus_WhenTaskNotExists_ShouldThrowException() {
        // Given
        when(taskRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> taskService.updateTaskStatus(1L, TaskStatus.IN_PROGRESS))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(taskRepository, times(1)).findById(1L);
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    @DisplayName("Should update task successfully")
    void updateTask_WhenTaskExists_ShouldReturnUpdatedTask() {
        // Given
        TaskRequest updateRequest = TaskRequest.builder()
                .title("Updated Title")
                .description("Updated Description")
                .build();

        Task updatedTask = Task.builder()
                .id(1L)
                .title("Updated Title")
                .description("Updated Description")
                .status(TaskStatus.TODO)
                .createdAt(testTask.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();

        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(updatedTask);

        // When
        TaskResponse response = taskService.updateTask(1L, updateRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("Updated Title");
        assertThat(response.getDescription()).isEqualTo("Updated Description");
        verify(taskRepository, times(1)).findById(1L);
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when updating non-existent task")
    void updateTask_WhenTaskNotExists_ShouldThrowException() {
        // Given
        when(taskRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> taskService.updateTask(1L, testRequest))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(taskRepository, times(1)).findById(1L);
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    @DisplayName("Should delete task successfully")
    void deleteTask_WhenTaskExists_ShouldDeleteTask() {
        // Given
        when(taskRepository.existsById(1L)).thenReturn(true);
        doNothing().when(taskRepository).deleteById(1L);

        // When
        taskService.deleteTask(1L);

        // Then
        verify(taskRepository, times(1)).existsById(1L);
        verify(taskRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting non-existent task")
    void deleteTask_WhenTaskNotExists_ShouldThrowException() {
        // Given
        when(taskRepository.existsById(anyLong())).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> taskService.deleteTask(1L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(taskRepository, times(1)).existsById(1L);
        verify(taskRepository, never()).deleteById(anyLong());
    }
}
