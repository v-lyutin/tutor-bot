package org.telegram.tutorbot.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.telegram.tutorbot.user.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}
