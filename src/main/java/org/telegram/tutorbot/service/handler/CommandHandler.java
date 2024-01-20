package org.telegram.tutorbot.service.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.tutorbot.bot.Bot;
import org.telegram.tutorbot.service.manager.impl.*;
import static org.telegram.tutorbot.service.data.Command.*;

@Service
public class CommandHandler {
    private final StartManager startManager;
    private final HelpManager helpManager;
    private final FeedbackManager feedbackManager;
    private final UnknownManager unknownManager;
    private final TimetableManager timetableManager;
    private final TaskManager taskManager;

    @Autowired
    public CommandHandler(StartManager startManager,
                          HelpManager helpManager,
                          FeedbackManager feedbackManager,
                          UnknownManager unknownManager,
                          TimetableManager timetableManager,
                          TaskManager taskManager) {
        this.startManager = startManager;
        this.helpManager = helpManager;
        this.feedbackManager = feedbackManager;
        this.unknownManager = unknownManager;
        this.timetableManager = timetableManager;
        this.taskManager = taskManager;
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
            default -> {
                return unknownManager.answerCommand(message);
            }
        }
    }
}

