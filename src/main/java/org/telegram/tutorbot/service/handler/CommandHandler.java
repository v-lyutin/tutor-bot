package org.telegram.tutorbot.service.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.tutorbot.bot.Bot;
import org.telegram.tutorbot.service.data.DefaultMessage;
import org.telegram.tutorbot.service.factory.KeyboardFactory;
import org.telegram.tutorbot.service.manager.FeedbackManager;
import org.telegram.tutorbot.service.manager.HelpManager;
import java.util.List;
import static org.telegram.tutorbot.service.data.Command.*;
import static org.telegram.tutorbot.service.data.CallbackData.*;

@Service
public class CommandHandler {
    private final KeyboardFactory keyboardFactory;
    private final HelpManager helpManager;
    private final FeedbackManager feedbackManager;

    @Autowired
    public CommandHandler(KeyboardFactory keyboardFactory,
                          HelpManager helpManager,
                          FeedbackManager feedbackManager) {
        this.keyboardFactory = keyboardFactory;
        this.helpManager = helpManager;
        this.feedbackManager = feedbackManager;
    }

    public BotApiMethod<?> answer(Message message, Bot bot) {
        String command = message.getText();
        switch (command) {
            case START_COMMAND -> {
                return start(message);
            }
            case FEEDBACK_COMMAND -> {
                return feedbackManager.answerCommand(message);
            }
            case HELP_COMMAND -> {
                return helpManager.answerCommand(message);
            }
            default -> {
                return handleUnknownCommand(message);
            }
        }
    }

    private BotApiMethod<?> handleUnknownCommand(Message message) {
        String messageText = DefaultMessage.getDefaultMessage();
        return sendMessage(message, messageText);
    }

    private BotApiMethod<?> start(Message message) {
        String messageText = """
                Подручный Владик приветствует. Я создан для упрощения взаимодействия репититора и ученика.
                                        
                Что вообще умею?
                                        
                📌 Составляю расписание
                📌 Прикрепляю домашние задания
                📌 Веду контроль успеваемости (дааа, бойся меня)
                """;

        return SendMessage.builder()
                .chatId(message.getChatId())
                .replyMarkup(keyboardFactory.getInlineKeyboard(
                        List.of("Помощь", "Обратная связь"),
                        List.of(2),
                        List.of(HELP, FEEDBACK)
                ))
                .text(messageText)
                .build();
    }

    private BotApiMethod<?> sendMessage(Message message, String messageText) {
        return SendMessage.builder()
                .chatId(message.getChatId())
                .text(messageText)
                .disableWebPagePreview(true)
                .build();
    }
}

