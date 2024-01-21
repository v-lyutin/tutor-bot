package org.telegram.tutorbot.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.DayOfWeek;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "timetable")
public class Timetable {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    private DayOfWeek dayOfWeek;

    @Column(name = "hour")
    private Short hour;

    @Column(name = "minute")
    private Short minute;
}
