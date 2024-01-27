package org.telegram.tutorbot.model;

import jakarta.persistence.*;
import lombok.*;
import org.telegram.tutorbot.model.enums.Role;
import org.telegram.tutorbot.model.enums.TaskStatus;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tasks")
public class Task {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "text_content")
    private String textContent;

    @Column(name = "actual_message_id")
    private Integer messageId;

    @Column(name = "actual_menu_id")
    private Integer menuId;

    @Column(name = "in_creation")
    private Boolean isInCreation;

    @Column(name = "has_media")
    Boolean hasMedia;

    @Column(name = "task_status")
    private TaskStatus taskStatus;

    @Column(name = "is_finished")
    private Boolean isFinished;

    @ManyToMany
    @JoinTable(
            joinColumns = @JoinColumn(name = "task_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"),
            name = "tasks_teacher_student"
    )
    private List<User> users;

    public User getStudent() {
        for (User user : users) {
            if (Role.STUDENT.equals(user.getRole())) {
                return user;
            }
        }
        throw new NoSuchElementException("No student for task " + id);
    }

    public User getTeacher() {
        for (User user : users) {
            if (Role.TEACHER.equals(user.getRole())) {
                return user;
            }
        }
        throw new NoSuchElementException("No teacher for task " + id);
    }


    public void changeStudent(User student) {
        if (Role.TEACHER.equals(student.getRole())) {
            throw new IllegalArgumentException("Asked student, teacher given");
        }
        users.removeIf(user -> Role.STUDENT.equals(user.getRole()));
        users.add(student);
    }
}
