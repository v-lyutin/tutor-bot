package org.telegram.tutorbot.service.manager.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.tutorbot.bot.Bot;
import org.telegram.tutorbot.util.factory.AnswerMethodFactory;
import org.telegram.tutorbot.util.factory.KeyboardFactory;
import org.telegram.tutorbot.service.manager.AbstractManager;
import org.telegram.tutorbot.model.User;
import org.telegram.tutorbot.model.enums.Action;
import org.telegram.tutorbot.model.enums.Role;
import org.telegram.tutorbot.repository.UserRepository;
import java.util.List;
import static org.telegram.tutorbot.util.data.CallbackData.*;

@Slf4j
@Component
public class AuthManager implements AbstractManager {
    private final AnswerMethodFactory answerMethodFactory;
    private final KeyboardFactory keyboardFactory;
    private final UserRepository userRepository;

    @Autowired
    public AuthManager(AnswerMethodFactory answerMethodFactory,
                       KeyboardFactory keyboardFactory,
                       UserRepository userRepository) {
        this.answerMethodFactory = answerMethodFactory;
        this.keyboardFactory = keyboardFactory;
        this.userRepository = userRepository;
    }

    @Override
    public BotApiMethod<?> answerCommand(Message message, Bot bot) {
        return null;
    }

    @Override
    public BotApiMethod<?> answerMessage(Message message, Bot bot) {
        Long chatId = message.getChatId();
        User user = userRepository.findById(chatId).orElseThrow();
        user.setAction(Action.AUTH);
        userRepository.save(user);

        InlineKeyboardMarkup keyboard = keyboardFactory.getInlineKeyboard(
                List.of("Ученик", "Учитель"),
                List.of(2),
                List.of(AUTH_STUDENT, AUTH_TEACHER)
        );

        return answerMethodFactory.getSendMessage(chatId, "Выбери свою роль", keyboard);
    }

    @Override
    public BotApiMethod<?> answerCallbackQuery(CallbackQuery callbackQuery, Bot bot) {
        Long chatId = callbackQuery.getMessage().getChatId();
        Integer messageId = callbackQuery.getMessage().getMessageId();
        User user = userRepository.findById(chatId).orElseThrow();
        String userRole = callbackQuery.getData();

        if (AUTH_TEACHER.equals(userRole)) {
            user.setRole(Role.TEACHER);
        } else {
            user.setRole(Role.STUDENT);
        }

        user.setAction(Action.FREE);
        userRepository.save(user);

        try {
            bot.execute(answerMethodFactory.getAnswerCallbackQuery(
                            callbackQuery.getId(),
                            "Авторизация прошла успешно, повтори предыдущий запрос!"
                    )
            );
        } catch (TelegramApiException exception) {
            log.error(exception.getMessage());
        }
        return answerMethodFactory.getDeleteMessage(chatId, messageId);
    }
}
