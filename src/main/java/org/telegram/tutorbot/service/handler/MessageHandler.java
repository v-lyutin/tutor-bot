package org.telegram.tutorbot.service.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.tutorbot.bot.Bot;
import org.telegram.tutorbot.model.User;
import org.telegram.tutorbot.model.enums.Action;
import org.telegram.tutorbot.repository.UserRepository;
import org.telegram.tutorbot.service.manager.impl.SearchManager;
import org.telegram.tutorbot.service.manager.impl.TaskManager;
import org.telegram.tutorbot.service.manager.impl.TimetableManager;

@Service
public class MessageHandler {
    private final SearchManager searchManager;
    private final UserRepository userRepository;
    private final TimetableManager timetableManager;
    private final TaskManager taskManager;

    @Autowired
    public MessageHandler(SearchManager searchManager,
                          UserRepository userRepository,
                          TimetableManager timetableManager,
                          TaskManager taskManager) {
        this.searchManager = searchManager;
        this.userRepository = userRepository;
        this.timetableManager = timetableManager;
        this.taskManager = taskManager;
    }

    public BotApiMethod<?> answer(Message message, Bot bot) {
        User user = userRepository.findUserByChatId(message.getChatId());
        Action action = user.getAction();
        switch (action) {
            case SENDING_TOKEN -> {
                return searchManager.answerMessage(message, bot);
            }
            case SENDING_TITLE, SENDING_DESCRIPTION -> {
                return timetableManager.answerMessage(message, bot);
            }
            case SENDING_TASK, SENDING_TEXT, SENDING_MEDIA -> {
                return taskManager.answerMessage(message, bot);
            }
        }
        return null;
    }
}
