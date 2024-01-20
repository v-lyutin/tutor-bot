package org.telegram.tutorbot.user.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.tutorbot.user.model.User;
import org.telegram.tutorbot.user.repository.UserRepository;
import org.telegram.tutorbot.user.service.UserService;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void createUser(Long chatId) {
        userRepository.save(
                User.builder()
                        .chatId(chatId)
                        .build());
    }
}
