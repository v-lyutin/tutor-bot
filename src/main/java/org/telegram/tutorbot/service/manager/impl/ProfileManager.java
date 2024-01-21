package org.telegram.tutorbot.service.manager.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.tutorbot.bot.Bot;
import org.telegram.tutorbot.model.User;
import org.telegram.tutorbot.model.UserDetails;
import org.telegram.tutorbot.repository.UserRepository;
import org.telegram.tutorbot.service.manager.AbstractManager;
import org.telegram.tutorbot.util.factory.AnswerMethodFactory;

@Component
public class ProfileManager implements AbstractManager {
    private final AnswerMethodFactory answerMethodFactory;
    private final UserRepository userRepository;

    @Autowired
    public ProfileManager(AnswerMethodFactory answerMethodFactory, UserRepository userRepository) {
        this.answerMethodFactory = answerMethodFactory;
        this.userRepository = userRepository;
    }

    @Override
    public BotApiMethod<?> answerCommand(Message message, Bot bot) {
        return showUserDescription(message);
    }

    @Override
    public BotApiMethod<?> answerMessage(Message message, Bot bot) {
        return null;
    }

    @Override
    public BotApiMethod<?> answerCallbackQuery(CallbackQuery callbackQuery, Bot bot) {
        return null;
    }

    private BotApiMethod<?> showUserDescription(Message message) {
        Long chatId = message.getChatId();
        User user = userRepository.findById(chatId).orElseThrow();
        UserDetails userDetails = user.getUserDetails();
        String userDescription = generateUserDescription(user, userDetails);

        return answerMethodFactory.getSendMessage(chatId, userDescription, null);
    }

    private String generateUserDescription(User user, UserDetails userDetails) {
        StringBuilder userDescription = new StringBuilder("\uD83D\uDC64 Твой профиль\n\n");
        String username;

        if (userDetails.getFirstName() == null) {
            username = userDetails.getUsername();
        } else {
            username = userDetails.getFirstName();
        }



        userDescription
                .append("▪\uFE0F Имя: ").append(username)
                .append("\n▪\uFE0F Роль: ").append(user.getRole().getValue())
                .append("\n▪\uFE0F Уникальный токен:\n ").append(user.getToken().toString())
                .append("\n\n⚠\uFE0F Токен необходим для установки связи между преподавателем и учеником");

        return userDescription.toString();
    }
}
