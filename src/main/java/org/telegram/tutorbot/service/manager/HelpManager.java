package org.telegram.tutorbot.service.manager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.tutorbot.service.factory.AnswerMethodFactory;

@Component
public class HelpManager {
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

    public BotApiMethod<?> answerCommand(Message message) {
        return answerMethodFactory.getSendMessage(message.getChatId(), HELP_TEXT, null);
    }

    public BotApiMethod<?> answerCallbackQuery(CallbackQuery callbackQuery) {
        return answerMethodFactory.getEditMessage(callbackQuery, HELP_TEXT, null);
    }
}
