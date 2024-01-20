package org.telegram.tutorbot.service.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.tutorbot.bot.Bot;
import org.telegram.tutorbot.service.manager.FeedbackManager;
import org.telegram.tutorbot.service.manager.HelpManager;
import static org.telegram.tutorbot.service.data.CallbackData.*;

@Service
public class CallbackQueryHandler {
    private final FeedbackManager feedbackManager;
    private final HelpManager helpManager;

    @Autowired
    public CallbackQueryHandler(FeedbackManager feedbackManager, HelpManager helpManager) {
        this.feedbackManager = feedbackManager;
        this.helpManager = helpManager;
    }

    public BotApiMethod<?> answer(CallbackQuery callbackQuery, Bot bot) {
        String callbackData = callbackQuery.getData();
        switch (callbackData) {
            case FEEDBACK -> {
                return feedbackManager.answerCallbackQuery(callbackQuery);
            }
            case HELP -> {
                return helpManager.answerCallbackQuery(callbackQuery);
            }
        }

        return null;
    }
}
