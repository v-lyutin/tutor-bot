package org.telegram.tutorbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.telegram.tutorbot.model.Task;
import org.telegram.tutorbot.model.User;
import org.telegram.tutorbot.model.enums.TaskStatus;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {
    boolean existsByUsersContainingAndIsInCreation(User user, Boolean isInCreation);

    Task findTaskByUsersContainingAndIsInCreation(User user, Boolean isInCreation);

    void deleteTaskByUsersContainingAndIsInCreation(User user, Boolean isInCreation);

    Task findTaskById(UUID id);

    int countAllByUsersContainsAndIsFinishedAndTaskStatus(User user, boolean isFinished, TaskStatus taskStatus);
}
