package org.telegram.tutorbot.service.manager.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.tutorbot.bot.Bot;
import org.telegram.tutorbot.model.User;
import org.telegram.tutorbot.model.UserDetails;
import org.telegram.tutorbot.model.enums.Role;
import org.telegram.tutorbot.model.enums.TaskStatus;
import org.telegram.tutorbot.repository.TaskRepository;
import org.telegram.tutorbot.repository.UserRepository;
import org.telegram.tutorbot.util.factory.AnswerMethodFactory;
import org.telegram.tutorbot.util.factory.KeyboardFactory;
import org.telegram.tutorbot.service.manager.AbstractManager;

import java.util.ArrayList;
import java.util.List;
import static org.telegram.tutorbot.util.data.CallbackData.*;

@Component
public class ProgressControlManager implements AbstractManager {
    private final AnswerMethodFactory answerMethodFactory;
    private final KeyboardFactory keyboardFactory;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    @Autowired
    public ProgressControlManager(AnswerMethodFactory answerMethodFactory,
                                  KeyboardFactory keyboardFactory,
                                  UserRepository userRepository,
                                  TaskRepository taskRepository) {
        this.answerMethodFactory = answerMethodFactory;
        this.keyboardFactory = keyboardFactory;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
    }

    @Override
    public BotApiMethod<?> answerCommand(Message message, Bot bot) {
        User user = userRepository.findUserByChatId(message.getChatId());

        if(user.getRole().equals(Role.STUDENT)) {
            return null;
        }
        return getMenu(message);
    }

    @Override
    public BotApiMethod<?> answerMessage(Message message, Bot bot) {
        return null;
    }

    @Override
    public BotApiMethod<?> answerCallbackQuery(CallbackQuery callbackQuery, Bot bot) {
        String callbackData = callbackQuery.getData();
        switch (callbackData) {
            case PROGRESS -> {
                return getMenu(callbackQuery);
            }
            case PROGRESS_STAT -> {
                return getStat(callbackQuery);
            }
        }

        String[] splitCallbackData = callbackData.split("_");
        switch (splitCallbackData[1]) {
            case USER -> {
                return showUserStat(callbackQuery, splitCallbackData[2]);
            }
        }

        return null;
    }

    //TODO: refactor
    private BotApiMethod<?> showUserStat(CallbackQuery callbackQuery, String id) {
        User student = userRepository.findUserByToken(id);
        UserDetails userDetails = student.getUserDetails();

        int success = taskRepository.countAllByUsersContainsAndIsFinishedAndTaskStatus(student, true, TaskStatus.SUCCESS);
        int fail = taskRepository.countAllByUsersContainsAndIsFinishedAndTaskStatus(student, true, TaskStatus.FAIL);

        String stat = String.format("""
                \uD83D\uDD39Статистика по пользователю %s
                
                Решено - %d
                Провалено - %d
                Всего - %d
                """, userDetails.getFirstName(), success, fail, success + fail);

        return answerMethodFactory.getEditMessage(
                callbackQuery,
                stat,
                keyboardFactory.getInlineKeyboard(
                        List.of("Назад"),
                        List.of(1),
                        List.of(PROGRESS_STAT)
                )
        );
    }

    //TODO: refactor
    private BotApiMethod<?> getStat(CallbackQuery callbackQuery) {
        User teacher = userRepository.findUserByChatId(callbackQuery.getMessage().getChatId());
        List<User> students = teacher.getUsers();

        List<String> buttonsText = new ArrayList<>();
        List<Integer> buttonsConfiguration = new ArrayList<>();
        List<String> buttonsCallbackData = new ArrayList<>();

        int index = 0;
        for (User student : students) {
            buttonsText.add(student.getUserDetails().getFirstName());
            buttonsCallbackData.add(PROGRESS_USER + student.getToken());
            if (index == 4) {
                buttonsConfiguration.add(index);
                index = 0;
            } else {
                index++;
            }
        }
        if (index != 0) {
            buttonsConfiguration.add(index);
        }

        buttonsText.add("Назад");
        buttonsCallbackData.add(PROGRESS);
        buttonsConfiguration.add(1);

        InlineKeyboardMarkup keyboard = keyboardFactory.getInlineKeyboard(
                buttonsText,
                buttonsConfiguration,
                buttonsCallbackData
        );

        return answerMethodFactory.getEditMessage(
                callbackQuery,
                "Выбери ученика",
                keyboard
        );
    }

    private BotApiMethod<?> getMenu(Message message) {
        InlineKeyboardMarkup keyboard = keyboardFactory.getInlineKeyboard(
                List.of("Статистика успеваемости"),
                List.of(1),
                List.of(PROGRESS_STAT)
        );

        return answerMethodFactory.getSendMessage(
                message.getChatId(),
                "Здесь ты можешь увидеть статистику по каждому ученику",
                keyboard);
    }

    private BotApiMethod<?> getMenu(CallbackQuery callbackQuery) {
        InlineKeyboardMarkup keyboard = keyboardFactory.getInlineKeyboard(
                List.of("Статистика успеваемости"),
                List.of(1),
                List.of(PROGRESS_STAT)
        );

        return answerMethodFactory.getEditMessage(
                callbackQuery,
                "Здесь ты можешь увидеть статистику по каждому ученику",
                keyboard);
    }
}
