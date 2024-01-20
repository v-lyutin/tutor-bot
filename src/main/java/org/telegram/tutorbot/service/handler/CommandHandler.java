package org.telegram.tutorbot.service.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.tutorbot.bot.Bot;
import org.telegram.tutorbot.service.manager.FeedbackManager;
import org.telegram.tutorbot.service.manager.HelpManager;
import org.telegram.tutorbot.service.manager.StartManager;
import org.telegram.tutorbot.service.manager.UnknownManager;

import static org.telegram.tutorbot.service.data.Command.*;

@Service
public class CommandHandler {
    private final StartManager startManager;
    private final HelpManager helpManager;
    private final FeedbackManager feedbackManager;
    private final UnknownManager unknownManager;

    @Autowired
    public CommandHandler(StartManager startManager,
                          HelpManager helpManager,
                          FeedbackManager feedbackManager,
                          UnknownManager unknownManager) {
        this.startManager = startManager;
        this.helpManager = helpManager;
        this.feedbackManager = feedbackManager;
        this.unknownManager = unknownManager;
    }

    public BotApiMethod<?> answer(Message message, Bot bot) {
        String command = message.getText();
        switch (command) {
            case START_COMMAND -> {
                return startManager.answerCommand(message);
            }
            case FEEDBACK_COMMAND -> {
                return feedbackManager.answerCommand(message);
            }
            case HELP_COMMAND -> {
                return helpManager.answerCommand(message);
            }
            default -> {
                return unknownManager.answerCommand(message);
            }
        }
    }
}

