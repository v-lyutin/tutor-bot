package org.telegram.tutorbot.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.tutorbot.bot.Bot;
import org.telegram.tutorbot.util.factory.AnswerMethodFactory;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class MessageExecutor {
    private final AnswerMethodFactory answerMethodFactory;

    @Autowired
    public MessageExecutor(AnswerMethodFactory answerMethodFactory) {
        this.answerMethodFactory = answerMethodFactory;
    }

    public void executeAnswerCallbackQuery(Bot bot, CallbackQuery callbackQuery, String messageText) {
        try {
            bot.execute(answerMethodFactory.getAnswerCallbackQuery(callbackQuery.getId(), messageText));
        } catch (TelegramApiException exception) {
            log.error(exception.getMessage());
        }
    }

    public void executeSendMessage(Bot bot, Message message, String messageText, InlineKeyboardMarkup keyboard) {
        try {
            bot.execute(answerMethodFactory.getSendMessage(message.getChatId(), messageText, keyboard));
        } catch (TelegramApiException exception) {
            log.error(exception.getMessage());
        }
    }

    public void executeDeleteMessage(Bot bot, Long chatId, Integer messageId) {

        try {
            bot.execute(answerMethodFactory.getDeleteMessage(chatId, messageId));
        } catch (TelegramApiException exception) {
            log.error(exception.getMessage());
        }
    }

    public void executeSetCommands(Bot bot, Long chatId, Map<String, String> commands) {
        try {
            bot.execute(answerMethodFactory.setCommands(chatId, commands));
        } catch (TelegramApiException exception) {
            log.error(exception.getMessage());
        }
    }
}
