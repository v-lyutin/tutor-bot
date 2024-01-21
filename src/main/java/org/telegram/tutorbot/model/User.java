package org.telegram.tutorbot.model;

import jakarta.persistence.*;
import lombok.*;
import org.telegram.tutorbot.model.enums.Action;
import org.telegram.tutorbot.model.enums.Role;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {
    @Id
    @Column(name = "id")
    private Long chatId;

    @Column(name = "token", unique = true)
    private UUID token;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Enumerated(EnumType.STRING)
    private Action action;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_details_id")
    private UserDetails userDetails;

    @ManyToMany
    @JoinTable(
            joinColumns = @JoinColumn(name = "teacher_id"),
            inverseJoinColumns = @JoinColumn(name = "student_id"),
            name = "relationships")
    private List<User> users;

    @PrePersist
    private void generateUniqueToken() {
        if (token == null) {
            token = UUID.randomUUID();
        }
    }
}
