package org.telegram.tutorbot.service.manager.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.tutorbot.bot.Bot;
import org.telegram.tutorbot.model.User;
import org.telegram.tutorbot.model.UserDetails;
import org.telegram.tutorbot.repository.UserRepository;
import org.telegram.tutorbot.service.manager.AbstractManager;
import org.telegram.tutorbot.util.factory.AnswerMethodFactory;
import org.telegram.tutorbot.util.factory.KeyboardFactory;
import java.util.List;
import static org.telegram.tutorbot.util.data.CallbackData.PROFILE_REFRESH_TOKEN;

@Component
public class ProfileManager implements AbstractManager {
    private final AnswerMethodFactory answerMethodFactory;
    private final KeyboardFactory keyboardFactory;
    private final UserRepository userRepository;

    @Autowired
    public ProfileManager(AnswerMethodFactory answerMethodFactory,
                          KeyboardFactory keyboardFactory,
                          UserRepository userRepository) {
        this.answerMethodFactory = answerMethodFactory;
        this.keyboardFactory = keyboardFactory;
        this.userRepository = userRepository;
    }

    @Override
    public BotApiMethod<?> answerCommand(Message message, Bot bot) {
        return showProfile(message);
    }

    @Override
    public BotApiMethod<?> answerMessage(Message message, Bot bot) {
        return null;
    }

    @Override
    public BotApiMethod<?> answerCallbackQuery(CallbackQuery callbackQuery, Bot bot) {
        String callbackData = callbackQuery.getData();
        switch (callbackData) {
            case PROFILE_REFRESH_TOKEN -> {
                return refreshToken(callbackQuery);
            }
        }
        return null;
    }

    private BotApiMethod<?> showProfile(Message message) {
        Long chatId = message.getChatId();
        InlineKeyboardMarkup keyboard = keyboardFactory.getInlineKeyboard(
                List.of("Обновить токен"),
                List.of(1),
                List.of(PROFILE_REFRESH_TOKEN)
        );
        String profileDescription = generateUserDescription(chatId);

        return answerMethodFactory.getSendMessage(
                chatId,
                profileDescription,
                keyboard
        );
    }

    private BotApiMethod<?> showProfile(CallbackQuery callbackQuery) {
        Long chatId = callbackQuery.getMessage().getChatId();
        InlineKeyboardMarkup keyboard = keyboardFactory.getInlineKeyboard(
                List.of("Обновить токен"),
                List.of(1),
                List.of(PROFILE_REFRESH_TOKEN)
        );
        String profileDescription = generateUserDescription(chatId);

        return answerMethodFactory.getEditMessage(
                callbackQuery,
                profileDescription,
                keyboard
        );
    }

    private BotApiMethod<?> refreshToken(CallbackQuery callbackQuery) {
        User user = userRepository.findUserByChatId(callbackQuery.getMessage().getChatId());
        user.refreshToken();
        userRepository.save(user);
        return showProfile(callbackQuery);
    }

    private String generateUserDescription(Long chatId) {
        User user = userRepository.findUserByChatId(chatId);
        UserDetails userDetails = user.getUserDetails();
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
                .append("\n▪\uFE0F Уникальный токен:\n ").append(user.getToken())
                .append("\n\n⚠\uFE0F Токен необходим для установки связи между преподавателем и учеником");

        return userDescription.toString();
    }
}
