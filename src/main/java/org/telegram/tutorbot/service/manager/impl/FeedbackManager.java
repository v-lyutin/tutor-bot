package org.telegram.tutorbot.service.manager.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.tutorbot.bot.Bot;
import org.telegram.tutorbot.util.factory.AnswerMethodFactory;
import org.telegram.tutorbot.service.manager.AbstractManager;

@Component
public class FeedbackManager implements AbstractManager {
    private final AnswerMethodFactory answerMethodFactory;
    private static final String FEEDBACK_TEXT = """
            📍 Ссылки для обратной связи (с отцом Владика):
                            
            GitHub - https://github.com/v-lyutin
            Telegram - https://t.me/wurhez
            """;

    @Autowired
    public FeedbackManager(AnswerMethodFactory answerMethodFactory) {
        this.answerMethodFactory = answerMethodFactory;
    }

    @Override
    public BotApiMethod<?> answerCommand(Message message, Bot bot) {
        return answerMethodFactory.getSendMessage(message.getChatId(), FEEDBACK_TEXT, null);
    }

    @Override
    public BotApiMethod<?> answerCallbackQuery(CallbackQuery callbackQuery, Bot bot) {
        return answerMethodFactory.getEditMessage(callbackQuery, FEEDBACK_TEXT, null);
    }

    @Override
    public BotApiMethod<?> answerMessage(Message message, Bot bot) {
        return null;
    }
}
