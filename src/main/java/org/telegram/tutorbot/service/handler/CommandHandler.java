package org.telegram.tutorbot.service.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.tutorbot.bot.Bot;
import org.telegram.tutorbot.service.manager.impl.*;
import static org.telegram.tutorbot.util.data.Command.*;

@Service
public class CommandHandler {
    private final StartManager startManager;
    private final HelpManager helpManager;
    private final FeedbackManager feedbackManager;
    private final UnknownManager unknownManager;
    private final TimetableManager timetableManager;
    private final TaskManager taskManager;
    private final ProgressControlManager progressControlManager;
    private final ProfileManager profileManager;
    private final SearchManager searchManager;

    @Autowired
    public CommandHandler(StartManager startManager,
                          HelpManager helpManager,
                          FeedbackManager feedbackManager,
                          UnknownManager unknownManager,
                          TimetableManager timetableManager,
                          TaskManager taskManager,
                          ProgressControlManager progressControlManager,
                          ProfileManager profileManager,
                          SearchManager searchManager) {
        this.startManager = startManager;
        this.helpManager = helpManager;
        this.feedbackManager = feedbackManager;
        this.unknownManager = unknownManager;
        this.timetableManager = timetableManager;
        this.taskManager = taskManager;
        this.progressControlManager = progressControlManager;
        this.profileManager = profileManager;
        this.searchManager = searchManager;
    }

    public BotApiMethod<?> answer(Message message, Bot bot) {
        String command = message.getText();
        switch (command) {
            case START_COMMAND -> {
                return startManager.answerCommand(message, bot);
            }
            case FEEDBACK_COMMAND -> {
                return feedbackManager.answerCommand(message, bot);
            }
            case HELP_COMMAND -> {
                return helpManager.answerCommand(message, bot);
            }
            case TIMETABLE_COMMAND -> {
                return timetableManager.answerCommand(message, bot);
            }
            case TASK_COMMAND -> {
                return taskManager.answerCommand(message, bot);
            }
            case PROGRESS_COMMAND -> {
                return progressControlManager.answerCommand(message, bot);
            }
            case PROFILE_COMMAND -> {
                return profileManager.answerCommand(message, bot);
            }
            case SEARCH_COMMAND -> {
                return searchManager.answerCommand(message, bot);
            }
            default -> {
                return unknownManager.answerCommand(message);
            }
        }
    }
}

