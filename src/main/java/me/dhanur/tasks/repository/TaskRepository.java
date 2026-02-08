package me.dhanur.tasks.repository;

import me.dhanur.tasks.entity.Task;
import me.dhanur.tasks.entity.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    /**
     * Find tasks by status with pagination
     */
    Page<Task> findByStatus(TaskStatus status, Pageable pageable);

    /**
     * Find tasks by title containing (case-insensitive)
     */
    Page<Task> findByTitleContainingIgnoreCase(String title, Pageable pageable);
}
