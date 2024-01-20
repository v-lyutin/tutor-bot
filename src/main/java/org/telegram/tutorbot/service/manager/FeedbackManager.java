package org.telegram.tutorbot.service.manager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.tutorbot.service.factory.AnswerMethodFactory;

@Component
public class FeedbackManager {
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

    public BotApiMethod<?> answerCommand(Message message) {
        return answerMethodFactory.getSendMessage(message.getChatId(), FEEDBACK_TEXT, null);
    }

    public BotApiMethod<?> answerCallbackQuery(CallbackQuery callbackQuery) {
        return answerMethodFactory.getEditMessage(callbackQuery, FEEDBACK_TEXT, null);
    }
}
