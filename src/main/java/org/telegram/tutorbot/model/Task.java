package org.telegram.tutorbot.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;
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

    @Column(name = "title")
    private String title;

    @Column(name = "text_content")
    private String textContent;

    @Column(name = "actual_message_id")
    private Integer messageId;

    @Column(name = "in_creation")
    private Boolean isInCreation;

    @ManyToMany
    @JoinTable(
            joinColumns = @JoinColumn(name = "task_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"),
            name = "tasks_teacher_student"
    )
    private List<User> users;
}
