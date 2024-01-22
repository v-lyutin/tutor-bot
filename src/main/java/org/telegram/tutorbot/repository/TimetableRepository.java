package org.telegram.tutorbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.telegram.tutorbot.model.Timetable;
import org.telegram.tutorbot.model.User;

import java.time.DayOfWeek;
import java.util.List;
import java.util.UUID;

@Repository
public interface TimetableRepository extends JpaRepository<Timetable, UUID> {
    List<Timetable> findAllByUsersContainingAndDayOfWeek(User user, DayOfWeek dayOfWeek);
}
