package org.telegram.tutorbot.service.manager.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
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

@Slf4j
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

        if (user.getRole().equals(Role.STUDENT)) {
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
                return addTask(user, message, chatId, bot);
            }
            case SENDING_TEXT -> {
                return editText(message, chatId, user, bot);
            }
            case SENDING_MEDIA -> {
                return editMedia(message, chatId, user, bot);
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

        if (MENU.equals(splitCallbackData[1])) {
            return menu(callbackQuery, splitCallbackData[2]);
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
                case TEXT -> {
                    return askText(callbackQuery, splitCallbackData[3]);
                }
                case MEDIA -> {
                    return askMedia(callbackQuery, splitCallbackData[3]);
                }
                case SEND -> {
                    return askConfirmation(callbackQuery, splitCallbackData[3]);
                }
                case CONFIRM -> {
                    return sendTask(callbackQuery, splitCallbackData[3], bot);
                }
                case CHANGE -> {
                    return askUser(callbackQuery, splitCallbackData[4]);
                }
                case STUDENT -> {
                    return editStudent(callbackQuery, splitCallbackData[3]);
                }
                case SUCCESS -> {
                    return sendInfo(callbackQuery, splitCallbackData[3], bot, true);
                }
                case FAIL -> {
                    return sendInfo(callbackQuery, splitCallbackData[3], bot, false);
                }
            }
        }
        return null;
    }

    //TODO: refactor
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
                index = 0;
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

    //TODO: refactor
    private BotApiMethod<?> addTask(User user, Message message, Long chatId, Bot bot) {
        Task task = taskRepository.findTaskByUsersContainingAndIsInCreation(user, true);
        String taskId = String.valueOf(task.getId());
        Integer messageId = messageExecutor
                .executeCopyMessageAndGetId(bot, chatId, chatId, message.getMessageId());
        task.setMessageId(messageId);
        task.setHasMedia(message.hasVideo() || message.hasPhoto() || message.hasAudio() || message.hasDocument());

        if (message.hasText()) {
            task.setTextContent(message.getText());
        }

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

        task.setMenuId(messageExecutor.executeSendMessageAndGetId(
                bot,
                chatId,
                "Настрой задание, и когда оно будет готово - нажми «Отправить»",
                keyboard));
        taskRepository.save(task);
        return null;
    }

    private BotApiMethod<?> sendTask(CallbackQuery callbackQuery, String taskId, Bot bot) {
        Long chatId = callbackQuery.getMessage().getChatId();
        User teacher = userRepository.findUserByChatId(chatId);
        Task task = taskRepository.findTaskByUsersContainingAndIsInCreation(teacher, true);
        InlineKeyboardMarkup keyboard = keyboardFactory.getInlineKeyboard(
                List.of("Готово", "Затрудняюсь с ответом"),
                List.of(1, 1),
                List.of(TASK_ANSWER_SUCCESS + taskId, TASK_ANSWER_FAIL + taskId)
        );

        messageExecutor.executeCopyMessage(bot, task.getStudent().getChatId(), chatId, task.getMessageId(), keyboard);
        messageExecutor.executeAnswerCallbackQuery(bot, callbackQuery, "Задание успешно отправлено");
        messageExecutor.executeDeleteMessage(bot, chatId, task.getMessageId());

        task.setIsInCreation(false);
        taskRepository.save(task);

        return answerMethodFactory.getDeleteMessage(chatId, task.getMenuId());
    }

    //TODO: refactor
    private BotApiMethod<?> askUser(CallbackQuery callbackQuery, String taskId) {
        User teacher = userRepository.findUserByChatId(callbackQuery.getMessage().getChatId());
        List<User> students = teacher.getUsers();

        List<String> buttonsText = new ArrayList<>();
        List<Integer> buttonsConfiguration = new ArrayList<>();
        List<String> buttonsCallbackData = new ArrayList<>();

        int index = 0;
        for (User student : students) {
            buttonsText.add(student.getUserDetails().getFirstName());
            buttonsCallbackData.add(TASK_CREATE_STUDENT + student.getChatId());
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
        buttonsCallbackData.add(TASK_MENU + taskId);
        buttonsConfiguration.add(1);

        InlineKeyboardMarkup keyboard = keyboardFactory.getInlineKeyboard(
                buttonsText,
                buttonsConfiguration,
                buttonsCallbackData
        );

        return answerMethodFactory.getEditMessage(
                callbackQuery,
                "👤 Измени получателя, которому хочешь дать домашнее задание",
                keyboard
        );
    }

    private BotApiMethod<?> askMedia(CallbackQuery callbackQuery, String id) {
        User user = userRepository.findUserByChatId(callbackQuery.getMessage().getChatId());
        user.setAction(Action.SENDING_MEDIA);
        userRepository.save(user);
        InlineKeyboardMarkup keyboard = keyboardFactory.getInlineKeyboard(
                List.of("Назад"),
                List.of(1),
                List.of(TASK_MENU + id)
        );

        return answerMethodFactory.getEditMessage(
                callbackQuery,
                "Отправь измененное Фото|Видео|Документ|Аудио",
                keyboard
        );
    }

    private BotApiMethod<?> editMedia(Message message, Long chatId, User user, Bot bot) {
        Task task = taskRepository.findTaskByUsersContainingAndIsInCreation(user, true);
        if (message.hasText()) {
            return answerMethodFactory.getSendMessage(
                    chatId,
                    "Сообщение должно содержать один медиа файл (Фото, Видео, Документ или Аудио) и " +
                            "не должно содержать текста",
                    keyboardFactory.getInlineKeyboard(
                            List.of("Назад"),
                            List.of(1),
                            List.of(TASK_MENU + task.getId())
                    )
            );
        }
        task.setHasMedia(true);
        int previousId = task.getMessageId();
        task.setMessageId(messageExecutor.executeCopyMessageAndGetId(bot, chatId, chatId, message.getMessageId()));

        if (task.getTextContent() != null && !task.getTextContent().isEmpty()) {
            messageExecutor.executeEditMessageCaption(bot, chatId, task.getMessageId(), task.getTextContent());
        }

        messageExecutor.executeDeleteMessage(bot, chatId, previousId);
        try {
            task.setMenuId(bot.execute(menu(message, user)).getMessageId());
        } catch (TelegramApiException exception) {
            log.error(exception.getMessage());
        }
        taskRepository.save(task);
        return null;
    }

    private BotApiMethod<?> askText(CallbackQuery callbackQuery, String id) {
        var user = userRepository.findUserByChatId(callbackQuery.getMessage().getChatId());
        user.setAction(Action.SENDING_TEXT);
        userRepository.save(user);
        InlineKeyboardMarkup keyboard = keyboardFactory.getInlineKeyboard(
                List.of("Назад"),
                List.of(1),
                List.of(TASK_MENU + id)
        );

        return answerMethodFactory.getEditMessage(
                callbackQuery,
                "Отправьте измененный текст",
                keyboard
        );
    }

    //TODO: refactor
    private BotApiMethod<?> editText(Message message, Long chatId, User user, Bot bot) {
        Task task = taskRepository.findTaskByUsersContainingAndIsInCreation(user, true);
        if (!message.hasText()) {
            return answerMethodFactory.getSendMessage(
                    chatId,
                    "Сообщение должно содержать текст",
                    keyboardFactory.getInlineKeyboard(
                            List.of("Назад"),
                            List.of(1),
                            List.of(TASK_MENU + task.getId())
                    )
            );
        }

        String messageText = message.getText();
        task.setTextContent(messageText);
        int previousId = task.getMessageId();
        if (task.getHasMedia()) {
            messageExecutor.executeEditMessageCaption(bot, chatId, previousId, messageText);
        } else {
            messageExecutor.executeEditMessage(bot, chatId, previousId, messageText);
        }

        task.setMessageId(messageExecutor.executeCopyMessageAndGetId(bot, chatId, chatId, previousId));
        messageExecutor.executeDeleteMessage(bot, chatId, previousId);
        try {
            task.setMenuId(bot.execute(menu(message, user)).getMessageId());
        } catch (TelegramApiException exception) {
            log.error(exception.getMessage());
        }
        taskRepository.save(task);
        return null;
    }

    private BotApiMethod<?> editStudent(CallbackQuery callbackQuery, String userId) {
        Task task = taskRepository.findTaskByUsersContainingAndIsInCreation(
                userRepository.findUserByChatId(callbackQuery.getMessage().getChatId()), true
        );
        User student = userRepository.findUserByChatId(Long.valueOf(userId));
        task.changeStudent(student);
        taskRepository.save(task);
        return menu(callbackQuery, String.valueOf(task.getId()));
    }

    private BotApiMethod<?> sendInfo(CallbackQuery callbackQuery, String id, Bot bot, boolean status) {
        Task task = taskRepository.findById(UUID.fromString(id)).orElseThrow();
        User teacher = task.getTeacher();
        String studentName = task.getStudent().getUserDetails().getFirstName();

        messageExecutor.executeEditMessageReplyMarkup(bot, callbackQuery, null);

        String answer = String.format("Ученик %s ", studentName);
        answer += (status) ? "успешно выполнил(а) задание" : "не справился(ась) с заданием";

        return answerMethodFactory.getSendMessage(teacher.getChatId(), answer, null);
    }

    private BotApiMethod<?> abortCreation(CallbackQuery callbackQuery, String taskId, Bot bot) {
        Task task = taskRepository.findTaskById(UUID.fromString(taskId));
        Integer messageId = task.getMessageId();
        Long chatId = callbackQuery.getMessage().getChatId();

        taskRepository.deleteById(UUID.fromString(taskId));
        messageExecutor.executeAnswerCallbackQuery(bot, callbackQuery, "Операция успешно отменена");
        messageExecutor.executeDeleteMessage(bot, chatId, messageId);

        return answerMethodFactory.getDeleteMessage(
                chatId,
                callbackQuery.getMessage().getMessageId()
        );
    }

    private BotApiMethod<?> askConfirmation(CallbackQuery callbackQuery, String id) {
        return answerMethodFactory.getEditMessage(
                callbackQuery,
                "Подтвердите, что хотите отправить задание выше ученику",
                keyboardFactory.getInlineKeyboard(
                        List.of("Да", "Нет"),
                        List.of(2),
                        List.of(TASK_CREATE_CONFIRM + id, TASK_MENU + id)
                )
        );
    }

    private SendMessage menu(Message message, User user) {
        user.setAction(Action.FREE);
        userRepository.save(user);
        Task task = taskRepository.findTaskByUsersContainingAndIsInCreation(user, true);
        String taskId = String.valueOf(task.getId());
        InlineKeyboardMarkup keyboard = keyboardFactory.getInlineKeyboard(
                List.of("Изменить текст", "Изменить медиа", "Выбрать ученика", "Отправить", "Отмена"),
                List.of(2, 1, 2),
                List.of(TASK_CREATE_TEXT + taskId, TASK_CREATE_MEDIA + taskId,
                        TASK_CREATE_CHANGE_USER + taskId, TASK_CREATE_SEND + taskId,
                        TASK_CREATE_CANCEL + taskId)
        );

        return answerMethodFactory.getSendMessage(
                message.getChatId(),
                "Настройте ваше задание, когда будете готовы- жмите \"Отправить\"",
                keyboard
        );
    }

    private BotApiMethod<?> menu(CallbackQuery callbackQuery, String taskId) {
        InlineKeyboardMarkup keyboard = keyboardFactory.getInlineKeyboard(
                List.of("Изменить текст", "Изменить медиа", "Выбрать ученика", "Отправить", "Отмена"),
                List.of(2, 1, 2),
                List.of(TASK_CREATE_TEXT + taskId, TASK_CREATE_MEDIA + taskId,
                        TASK_CREATE_CHANGE_USER + taskId, TASK_CREATE_SEND + taskId,
                        TASK_CREATE_CANCEL + taskId)
        );

        return answerMethodFactory.getEditMessage(
                callbackQuery,
                "Настройте ваше задание, когда будете готовы- жмите \"Отправить\"",
                keyboard
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
