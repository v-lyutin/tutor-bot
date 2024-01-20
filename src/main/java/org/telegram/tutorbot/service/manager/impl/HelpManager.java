package org.telegram.tutorbot.service.manager.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.tutorbot.bot.Bot;
import org.telegram.tutorbot.service.factory.AnswerMethodFactory;
import org.telegram.tutorbot.service.manager.AbstractManager;

@Component
public class HelpManager implements AbstractManager {
    private final AnswerMethodFactory answerMethodFactory;
    private static final String HELP_TEXT = """
            📍 Доступные команды:
            - start
            - help
            - feedback
                                                            
            📍 Доступные функции:
            - Расписание
            - Домашнее задание
            - Контроль успеваемости
            """;

    @Autowired
    public HelpManager(AnswerMethodFactory answerMethodFactory) {
        this.answerMethodFactory = answerMethodFactory;
    }

    @Override
    public BotApiMethod<?> answerCommand(Message message, Bot bot) {
        return answerMethodFactory.getSendMessage(message.getChatId(), HELP_TEXT, null);
    }

    @Override
    public BotApiMethod<?> answerCallbackQuery(CallbackQuery callbackQuery, Bot bot) {
        return answerMethodFactory.getEditMessage(callbackQuery, HELP_TEXT, null);
    }

    @Override
    public BotApiMethod<?> answerMessage(Message message, Bot bot) {
        return null;
    }
}
