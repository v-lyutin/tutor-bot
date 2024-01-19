package org.telegram.tutorbot.service.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.tutorbot.bot.Bot;
import org.telegram.tutorbot.service.data.DefaultMessage;
import org.telegram.tutorbot.service.factory.KeyboardFactory;

import java.util.List;

import static org.telegram.tutorbot.service.data.Command.*;

@Service
public class CommandHandler {
    private final KeyboardFactory keyboardFactory;

    @Autowired
    public CommandHandler(KeyboardFactory keyboardFactory) {
        this.keyboardFactory = keyboardFactory;
    }

    public BotApiMethod<?> answer(Message message, Bot bot) {
        String command = message.getText();
        switch (command) {
            case START -> {
                return start(message);
            }
            case FEEDBACK -> {
                return feedback(message);
            }
            case HELP -> {
                return help(message);
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

    private BotApiMethod<?> feedback(Message message) {
        String textMessage = """
                📍 Ссылки для обратной связи (с отцом Владика):
                                
                GitHub - https://github.com/v-lyutin
                Telegram - https://t.me/wurhez
                """;

        return sendMessage(message, textMessage);
    }

    private BotApiMethod<?> help(Message message) {
        String messageText = """
                📍 Доступные команды:
                - start
                - help
                - feedback
                                                                
                📍 Доступные функции:
                - Расписание
                - Домашнее задание
                - Контроль успеваемости
                """;

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
                        List.of("help", "feedback")
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

