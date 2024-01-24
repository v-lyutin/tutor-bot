package org.telegram.tutorbot.service.manager.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.tutorbot.bot.Bot;
import org.telegram.tutorbot.util.Command;
import org.telegram.tutorbot.util.MessageExecutor;
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
    private final MessageExecutor messageExecutor;

    @Autowired
    public AuthManager(AnswerMethodFactory answerMethodFactory,
                       KeyboardFactory keyboardFactory,
                       UserRepository userRepository,
                       MessageExecutor messageExecutor) {
        this.answerMethodFactory = answerMethodFactory;
        this.keyboardFactory = keyboardFactory;
        this.userRepository = userRepository;
        this.messageExecutor = messageExecutor;
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
        User user = userRepository.findUserByChatId(chatId);
        String userRole = callbackQuery.getData();

        if (AUTH_TEACHER.equals(userRole)) {
            messageExecutor.executeSetCommands(bot, chatId, Command.getCommands(Role.TEACHER));
            user.setRole(Role.TEACHER);
        } else {
            messageExecutor.executeSetCommands(bot, chatId, Command.getCommands(Role.STUDENT));
            user.setRole(Role.STUDENT);
        }

        user.setAction(Action.FREE);
        userRepository.save(user);

        String messageText = "Авторизация прошла успешно, повтори предыдущий запрос!";
        messageExecutor.executeAnswerCallbackQuery(bot, callbackQuery, messageText);

        return answerMethodFactory.getDeleteMessage(chatId, messageId);
    }
}
