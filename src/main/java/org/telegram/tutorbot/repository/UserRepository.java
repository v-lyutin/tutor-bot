package org.telegram.tutorbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.telegram.tutorbot.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}
