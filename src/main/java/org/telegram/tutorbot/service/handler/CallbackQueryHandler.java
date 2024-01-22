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
    private final ProfileManager profileManager;
    private final SearchManager searchManager;

    @Autowired
    public CallbackQueryHandler(FeedbackManager feedbackManager,
                                HelpManager helpManager,
                                TimetableManager timetableManager,
                                TaskManager taskManager,
                                AuthManager authManager,
                                ProgressControlManager progressControlManager,
                                ProfileManager profileManager,
                                SearchManager searchManager) {
        this.feedbackManager = feedbackManager;
        this.helpManager = helpManager;
        this.timetableManager = timetableManager;
        this.taskManager = taskManager;
        this.authManager = authManager;
        this.progressControlManager = progressControlManager;
        this.profileManager = profileManager;
        this.searchManager = searchManager;
    }

    public BotApiMethod<?> answer(CallbackQuery callbackQuery, Bot bot) {
        String callbackData = callbackQuery.getData();
        String key = callbackData.split("_")[0];

        switch (key) {
            case TIMETABLE -> {
                return timetableManager.answerCallbackQuery(callbackQuery, bot);
            }
            case TASK -> {
                return taskManager.answerCallbackQuery(callbackQuery, bot);
            }
            case PROGRESS -> {
                return progressControlManager.answerCallbackQuery(callbackQuery, bot);
            }
            case AUTH -> {
                return authManager.answerCallbackQuery(callbackQuery, bot);
            }
            case PROFILE -> {
                return profileManager.answerCallbackQuery(callbackQuery, bot);
            }
            case SEARCH -> {
                return searchManager.answerCallbackQuery(callbackQuery, bot);
            }
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
