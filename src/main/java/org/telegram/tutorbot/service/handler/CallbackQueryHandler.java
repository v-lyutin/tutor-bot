package org.telegram.tutorbot.service.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.tutorbot.bot.Bot;
import org.telegram.tutorbot.service.manager.impl.*;

import static org.telegram.tutorbot.util.data.CallbackData.*;

@Service
public class CallbackQueryHandler {
    private final FeedbackManager feedbackManager;
    private final HelpManager helpManager;
    private final TimetableManager timetableManager;
    private final TaskManager taskManager;
    private final AuthManager authManager;
    private final ProgressControlManager progressControlManager;

    @Autowired
    public CallbackQueryHandler(FeedbackManager feedbackManager,
                                HelpManager helpManager,
                                TimetableManager timetableManager,
                                TaskManager taskManager,
                                AuthManager authManager,
                                ProgressControlManager progressControlManager) {
        this.feedbackManager = feedbackManager;
        this.helpManager = helpManager;
        this.timetableManager = timetableManager;
        this.taskManager = taskManager;
        this.authManager = authManager;
        this.progressControlManager = progressControlManager;
    }

    public BotApiMethod<?> answer(CallbackQuery callbackQuery, Bot bot) {
        String callbackData = callbackQuery.getData();
        String key = callbackData.split("_")[0];

        if (TIMETABLE.equals(key)) {
            return timetableManager.answerCallbackQuery(callbackQuery, bot);
        } else if (TASK.equals(key)) {
            return taskManager.answerCallbackQuery(callbackQuery, bot);
        } else if (PROGRESS.equals(key)) {
            return progressControlManager.answerCallbackQuery(callbackQuery, bot);
        } else if (AUTH.equals(key)) {
            return authManager.answerCallbackQuery(callbackQuery, bot);
        }

        switch (callbackData) {
            case FEEDBACK -> {
                return feedbackManager.answerCallbackQuery(callbackQuery, bot);
            }
            case HELP -> {
                return helpManager.answerCallbackQuery(callbackQuery, bot);
            }
        }

        return null;
    }
}
