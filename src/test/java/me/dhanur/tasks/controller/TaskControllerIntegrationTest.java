package me.dhanur.tasks.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.dhanur.tasks.dto.TaskRequest;
import me.dhanur.tasks.entity.Task;
import me.dhanur.tasks.entity.TaskStatus;
import me.dhanur.tasks.repository.TaskRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@DisplayName("TaskController Integration Tests")
class TaskControllerIntegrationTest {

        private MockMvc mockMvc;

        @Autowired
        private WebApplicationContext webApplicationContext;

        private ObjectMapper objectMapper;

        @Autowired
        private TaskRepository taskRepository;

        private Task testTask;

        @BeforeEach
        void setUp() {
                mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
                objectMapper = new ObjectMapper();
                objectMapper.findAndRegisterModules();
                taskRepository.deleteAll();

                testTask = Task.builder()
                                .title("Integration Test Task")
                                .description("Testing the full API")
                                .status(TaskStatus.TODO)
                                .build();
                testTask = taskRepository.save(testTask);
        }

        @AfterEach
        void tearDown() {
                taskRepository.deleteAll();
        }

        @Test
        @DisplayName("Should create task and return 201 CREATED")
        void createTask_ShouldReturn201() throws Exception {
                TaskRequest request = TaskRequest.builder()
                                .title("New Task")
                                .description("New task description")
                                .build();

                mockMvc.perform(post("/api/v1/tasks")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").exists())
                                .andExpect(jsonPath("$.title").value("New Task"))
                                .andExpect(jsonPath("$.description").value("New task description"))
                                .andExpect(jsonPath("$.status").value("TODO"))
                                .andExpect(jsonPath("$.createdAt").exists())
                                .andExpect(jsonPath("$.updatedAt").exists());
        }

        @Test
        @DisplayName("Should return 400 BAD REQUEST when creating task with invalid data")
        void createTask_WithInvalidData_ShouldReturn400() throws Exception {
                TaskRequest request = TaskRequest.builder()
                                .title("AB")
                                .description("Description")
                                .build();

                mockMvc.perform(post("/api/v1/tasks")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.status").value(400))
                                .andExpect(jsonPath("$.error").value("Validation Failed"));
        }

        @Test
        @DisplayName("Should return 400 BAD REQUEST when title is blank")
        void createTask_WithBlankTitle_ShouldReturn400() throws Exception {
                TaskRequest request = TaskRequest.builder()
                                .title("")
                                .description("Description")
                                .build();

                mockMvc.perform(post("/api/v1/tasks")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should get all tasks with pagination")
        void getAllTasks_ShouldReturnPageOfTasks() throws Exception {
                mockMvc.perform(get("/api/v1/tasks")
                                .param("page", "0")
                                .param("size", "10"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content").isArray())
                                .andExpect(jsonPath("$.content", hasSize(1)))
                                .andExpect(jsonPath("$.content[0].title").value("Integration Test Task"))
                                .andExpect(jsonPath("$.totalElements").value(1))
                                .andExpect(jsonPath("$.totalPages").value(1))
                                .andExpect(jsonPath("$.size").value(10));
        }

        @Test
        @DisplayName("Should get all tasks filtered by status")
        void getAllTasks_FilteredByStatus_ShouldReturnFilteredTasks() throws Exception {
                // Create another task with different status
                Task inProgressTask = Task.builder()
                                .title("In Progress Task")
                                .description("Task in progress")
                                .status(TaskStatus.IN_PROGRESS)
                                .build();
                taskRepository.save(inProgressTask);

                mockMvc.perform(get("/api/v1/tasks")
                                .param("status", "TODO"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content").isArray())
                                .andExpect(jsonPath("$.content", hasSize(1)))
                                .andExpect(jsonPath("$.content[0].status").value("TODO"));
        }

        @Test
        @DisplayName("Should get task by ID and return 200 OK")
        void getTaskById_WhenTaskExists_ShouldReturn200() throws Exception {
                mockMvc.perform(get("/api/v1/tasks/{id}", testTask.getId()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(testTask.getId()))
                                .andExpect(jsonPath("$.title").value("Integration Test Task"))
                                .andExpect(jsonPath("$.description").value("Testing the full API"))
                                .andExpect(jsonPath("$.status").value("TODO"));
        }

        @Test
        @DisplayName("Should return 404 NOT FOUND when task doesn't exist")
        void getTaskById_WhenTaskNotExists_ShouldReturn404() throws Exception {
                mockMvc.perform(get("/api/v1/tasks/{id}", 999L))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.status").value(404))
                                .andExpect(jsonPath("$.error").value("Not Found"))
                                .andExpect(jsonPath("$.message").value("Task with id 999 not found"));
        }

        @Test
        @DisplayName("Should update task and return 200 OK")
        void updateTask_WhenTaskExists_ShouldReturn200() throws Exception {
                TaskRequest updateRequest = TaskRequest.builder()
                                .title("Updated Task Title")
                                .description("Updated task description")
                                .build();

                mockMvc.perform(put("/api/v1/tasks/{id}", testTask.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(testTask.getId()))
                                .andExpect(jsonPath("$.title").value("Updated Task Title"))
                                .andExpect(jsonPath("$.description").value("Updated task description"));
        }

        @Test
        @DisplayName("Should return 404 NOT FOUND when updating non-existent task")
        void updateTask_WhenTaskNotExists_ShouldReturn404() throws Exception {
                TaskRequest updateRequest = TaskRequest.builder()
                                .title("Updated Task Title")
                                .description("Updated task description")
                                .build();

                mockMvc.perform(put("/api/v1/tasks/{id}", 999L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRequest)))
                                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should update task status and return 200 OK")
        void updateTaskStatus_WhenTaskExists_ShouldReturn200() throws Exception {
                mockMvc.perform(patch("/api/v1/tasks/{id}/status", testTask.getId())
                                .param("status", "IN_PROGRESS"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(testTask.getId()))
                                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                                .andExpect(jsonPath("$.title").value("Integration Test Task"));
        }

        @Test
        @DisplayName("Should return 404 NOT FOUND when updating status of non-existent task")
        void updateTaskStatus_WhenTaskNotExists_ShouldReturn404() throws Exception {
                mockMvc.perform(patch("/api/v1/tasks/{id}/status", 999L)
                                .param("status", "IN_PROGRESS"))
                                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should delete task and return 204 NO CONTENT")
        void deleteTask_WhenTaskExists_ShouldReturn204() throws Exception {
                mockMvc.perform(delete("/api/v1/tasks/{id}", testTask.getId()))
                                .andExpect(status().isNoContent());

                // Verify task is actually deleted
                mockMvc.perform(get("/api/v1/tasks/{id}", testTask.getId()))
                                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 404 NOT FOUND when deleting non-existent task")
        void deleteTask_WhenTaskNotExists_ShouldReturn404() throws Exception {
                mockMvc.perform(delete("/api/v1/tasks/{id}", 999L))
                                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should perform complete CRUD lifecycle")
        void completeCrudLifecycle() throws Exception {
                // Create
                TaskRequest createRequest = TaskRequest.builder()
                                .title("Lifecycle Test Task")
                                .description("Testing complete CRUD lifecycle")
                                .build();

                String createResponse = mockMvc.perform(post("/api/v1/tasks")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createRequest)))
                                .andExpect(status().isCreated())
                                .andReturn()
                                .getResponse()
                                .getContentAsString();

                Long taskId = objectMapper.readTree(createResponse).get("id").asLong();

                // Read
                mockMvc.perform(get("/api/v1/tasks/{id}", taskId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.title").value("Lifecycle Test Task"));

                // Update
                TaskRequest updateRequest = TaskRequest.builder()
                                .title("Updated Lifecycle Task")
                                .description("Updated description")
                                .build();

                mockMvc.perform(put("/api/v1/tasks/{id}", taskId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.title").value("Updated Lifecycle Task"));

                // Update Status
                mockMvc.perform(patch("/api/v1/tasks/{id}/status", taskId)
                                .param("status", "DONE"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("DONE"));

                // Delete
                mockMvc.perform(delete("/api/v1/tasks/{id}", taskId))
                                .andExpect(status().isNoContent());

                // Verify deletion
                mockMvc.perform(get("/api/v1/tasks/{id}", taskId))
                                .andExpect(status().isNotFound());
        }
}
