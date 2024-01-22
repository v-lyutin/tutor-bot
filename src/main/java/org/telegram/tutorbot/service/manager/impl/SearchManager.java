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
import org.telegram.tutorbot.model.User;
import org.telegram.tutorbot.model.enums.Action;
import org.telegram.tutorbot.model.enums.Role;
import org.telegram.tutorbot.repository.UserRepository;
import org.telegram.tutorbot.service.manager.AbstractManager;
import org.telegram.tutorbot.util.factory.AnswerMethodFactory;
import org.telegram.tutorbot.util.factory.KeyboardFactory;
import java.util.List;
import static org.telegram.tutorbot.util.data.CallbackData.*;

@Slf4j
@Component
public class SearchManager implements AbstractManager {
    private final UserRepository userRepository;
    private final AnswerMethodFactory answerMethodFactory;
    private final KeyboardFactory keyboardFactory;

    @Autowired
    public SearchManager(UserRepository userRepository,
                         AnswerMethodFactory answerMethodFactory,
                         KeyboardFactory keyboardFactory) {
        this.userRepository = userRepository;
        this.answerMethodFactory = answerMethodFactory;
        this.keyboardFactory = keyboardFactory;
    }

    @Override
    public BotApiMethod<?> answerCommand(Message message, Bot bot) {
        return askToken(message);
    }

    @Override
    public BotApiMethod<?> answerMessage(Message message, Bot bot) {
        try {
            bot.execute(answerMethodFactory.getDeleteMessage(
                    message.getChatId(),
                    message.getMessageId() - 1)
            );
        } catch (TelegramApiException exception) {
            log.error(exception.getMessage());
        }

        User user = userRepository.findUserByChatId(message.getChatId());
        Action action = user.getAction();
        switch (action) {
            case SENDING_TOKEN -> {
                return checkToken(message, user);
            }
        }
        return null;
    }

    @Override
    public BotApiMethod<?> answerCallbackQuery(CallbackQuery callbackQuery, Bot bot) {
        String callbackData = callbackQuery.getData();
        switch (callbackData) {
            case SEARCH_CANCEL -> {
                return cancel(callbackQuery, bot);
            }
        }
        return null;
    }

    private BotApiMethod<?> checkToken(Message message, User user) {
        Long chatId = message.getChatId();
        String token = message.getText();
        User otherUser = userRepository.findUserByToken(token);
        if (otherUser == null) {
            InlineKeyboardMarkup keyboard = keyboardFactory.getInlineKeyboard(
                    List.of("Отмена операции"),
                    List.of(1),
                    List.of(SEARCH_CANCEL)
            );
            return answerMethodFactory.getSendMessage(
                    chatId,
                    "Ничего не нашел по этому токену, пришли корректный токен.\nПовтори попытку!",
                    keyboard);
        }

        if (validateUsers(user, otherUser)) {
            if (user.getRole() == Role.TEACHER) {
                user.addUser(otherUser);
            } else {
                otherUser.addUser(user);
            }

            user.setAction(Action.FREE);
            userRepository.save(user);
            userRepository.save(otherUser);

            return answerMethodFactory.getSendMessage(chatId, "Связь успешно установил.", null);
        }

        InlineKeyboardMarkup keyboard = keyboardFactory.getInlineKeyboard(
                List.of("Отмена операции"),
                List.of(1),
                List.of(SEARCH_CANCEL)
        );
        return answerMethodFactory.getSendMessage(
                chatId,
                "Ты не можешь установить свзяь с пользователем, у которого такая же роль как и у тебя.",
                keyboard);
    }

    private boolean validateUsers(User userOne, User userTwo) {
        return userOne.getRole() != userTwo.getRole();
    }

    private BotApiMethod<?> askToken(Message message) {
        Long chatId = message.getChatId();
        User user = userRepository.findUserByChatId(chatId);
        user.setAction(Action.SENDING_TOKEN);
        userRepository.save(user);

        InlineKeyboardMarkup keyboard = keyboardFactory.getInlineKeyboard(
                List.of("Отмена операции"),
                List.of(1),
                List.of(SEARCH_CANCEL)
        );

        return answerMethodFactory.getSendMessage(
                chatId,
                "Отправь токен того, с кем хочешь настроить коннект",
                keyboard);
    }

    private BotApiMethod<?> cancel(CallbackQuery callbackQuery, Bot bot) {
        Long chatId = callbackQuery.getMessage().getChatId();
        User user = userRepository.findUserByChatId(chatId);
        user.setAction(Action.FREE);
        userRepository.save(user);

        try {
            bot.execute(answerMethodFactory.getAnswerCallbackQuery(
                            callbackQuery.getId(),
                            "Операция отменена"
                    )
            );
        } catch (TelegramApiException exception) {
            log.error(exception.getMessage());
        }

        return answerMethodFactory.getDeleteMessage(
                chatId,
                callbackQuery.getMessage().getMessageId()
        );
    }
}
