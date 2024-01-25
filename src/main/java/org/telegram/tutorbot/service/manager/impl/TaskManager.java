package org.telegram.tutorbot.service.manager.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.tutorbot.bot.Bot;
import org.telegram.tutorbot.model.Task;
import org.telegram.tutorbot.model.User;
import org.telegram.tutorbot.model.enums.Action;
import org.telegram.tutorbot.model.enums.Role;
import org.telegram.tutorbot.repository.TaskRepository;
import org.telegram.tutorbot.repository.UserRepository;
import org.telegram.tutorbot.util.MessageExecutor;
import org.telegram.tutorbot.util.factory.AnswerMethodFactory;
import org.telegram.tutorbot.util.factory.KeyboardFactory;
import org.telegram.tutorbot.service.manager.AbstractManager;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.telegram.tutorbot.util.data.CallbackData.*;

@Component
public class TaskManager implements AbstractManager {
    private final AnswerMethodFactory answerMethodFactory;
    private final KeyboardFactory keyboardFactory;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final MessageExecutor messageExecutor;
    private static final String TASK_TEXT = "\uD83D\uDDC2 Ты можешь добавить домашнее задание своему ученику";

    @Autowired
    public TaskManager(AnswerMethodFactory answerMethodFactory,
                       KeyboardFactory keyboardFactory,
                       UserRepository userRepository,
                       TaskRepository taskRepository,
                       MessageExecutor messageExecutor) {
        this.answerMethodFactory = answerMethodFactory;
        this.keyboardFactory = keyboardFactory;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.messageExecutor = messageExecutor;
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
        Long chatId = message.getChatId();
        User user = userRepository.findUserByChatId(chatId);

        messageExecutor.executeDeleteMessage(bot, chatId, message.getMessageId() - 1);

        switch (user.getAction()) {
            case SENDING_TASK -> {
                return addTask(user, message, chatId);
            }
        }

        return null;
    }

    @Override
    @Transactional
    public BotApiMethod<?> answerCallbackQuery(CallbackQuery callbackQuery, Bot bot) {
        String callbackData = callbackQuery.getData();
        String[] splitCallbackData = callbackData.split("_");
        switch (callbackData) {
            case TASK -> {
                return getMenu(callbackQuery);
            }
            case TASK_CREATE -> {
                return startTaskCreating(callbackQuery);
            }
        }

        if (splitCallbackData.length > 2) {
            String key = splitCallbackData[2];
            switch (key) {
                case USER -> {
                    return addUser(callbackQuery, splitCallbackData);
                }
                case CANCEL -> {
                    return abortCreation(callbackQuery, splitCallbackData[3], bot);
                }
            }
        }

        return null;
    }

    private BotApiMethod<?> startTaskCreating(CallbackQuery callbackQuery) {
        User teacher = userRepository.findUserByChatId(callbackQuery.getMessage().getChatId());
        List<User> students = teacher.getUsers();

        List<String> buttonsText = new ArrayList<>();
        List<Integer> buttonsConfiguration = new ArrayList<>();
        List<String> buttonsCallbackData = new ArrayList<>();

        int index = 0;
        for (User student : students) {
            buttonsText.add(student.getUserDetails().getFirstName());
            buttonsCallbackData.add(TASK_CREATE_USER + student.getChatId());
            if (index == 4) {
                buttonsConfiguration.add(index);
            } else {
                index++;
            }
        }
        if (index != 0) {
            buttonsConfiguration.add(index);
        }

        buttonsText.add("Назад");
        buttonsConfiguration.add(1);
        buttonsCallbackData.add(TASK);

        InlineKeyboardMarkup keyboard = keyboardFactory.getInlineKeyboard(
                buttonsText,
                buttonsConfiguration,
                buttonsCallbackData
        );

        return answerMethodFactory.getEditMessage(
                callbackQuery,
                "\uD83D\uDC64 Выбери ученика, которому хочешь дать домашнее задание",
                keyboard);
    }

    private BotApiMethod<?> addUser(CallbackQuery callbackQuery, String[] splitCallbackData) {
        User teacher = userRepository.findUserByChatId(callbackQuery.getMessage().getChatId());
        taskRepository.deleteTaskByUsersContainingAndIsInCreation(teacher, true);
        User student = userRepository.findUserByChatId(Long.valueOf(splitCallbackData[3]));
        Task task = Task.builder()
                .users(List.of(student, teacher))
                .isInCreation(true)
                .build();
        taskRepository.save(task);
        teacher.setAction(Action.SENDING_TASK);
        userRepository.save(teacher);

        InlineKeyboardMarkup keyboard = keyboardFactory.getInlineKeyboard(
                List.of("Назад"),
                List.of(1),
                List.of(TASK_CREATE)
        );

        return answerMethodFactory.getEditMessage(
                callbackQuery,
                "Отправь задание одним сообщением",
                keyboard
        );
    }

    private BotApiMethod<?> addTask(User user, Message message, Long chatId) {
        Task task = taskRepository.findTaskByUsersContainingAndIsInCreation(user, true);
        String taskId = String.valueOf(task.getId());
        task.setMessageId(message.getMessageId());
        taskRepository.save(task);

        user.setAction(Action.FREE);
        userRepository.save(user);

        InlineKeyboardMarkup keyboard = keyboardFactory.getInlineKeyboard(
                List.of("Изменить текст", "Изменить медиа", "Выбрать ученика", "Отмена", "Отправить"),
                List.of(2, 1, 2),
                List.of(TASK_CREATE_TEXT + taskId,
                        TASK_CREATE_MEDIA + taskId,
                        TASK_CREATE_CHANGE_USER + taskId,
                        TASK_CREATE_CANCEL + taskId,
                        TASK_CREATE_SEND + taskId)
        );

        return answerMethodFactory.getSendMessage(
                chatId,
                "Настрой задание, и когда оно будет готово - нажми «Отправить»",
                keyboard);
    }

    private BotApiMethod<?> abortCreation(CallbackQuery callbackQuery, String taskId, Bot bot) {
        taskRepository.deleteById(UUID.fromString(taskId));
        messageExecutor.executeAnswerCallbackQuery(bot, callbackQuery, "Операция успешно отменена");

        return answerMethodFactory.getDeleteMessage(
                callbackQuery.getMessage().getChatId(),
                callbackQuery.getMessage().getMessageId()
        );
    }


    private BotApiMethod<?> getMenu(Message message) {
        InlineKeyboardMarkup keyboard = keyboardFactory.getInlineKeyboard(
                List.of("Прикрепить домашнее задание"),
                List.of(1),
                List.of(TASK_CREATE)
        );

        return answerMethodFactory.getSendMessage(message.getChatId(), TASK_TEXT, keyboard);
    }

    private BotApiMethod<?> getMenu(CallbackQuery callbackQuery) {
        InlineKeyboardMarkup keyboard = keyboardFactory.getInlineKeyboard(
                List.of("Прикрепить домашнее задание"),
                List.of(1),
                List.of(TASK_CREATE)
        );

        return answerMethodFactory.getEditMessage(callbackQuery, TASK_TEXT, keyboard);
    }
}
