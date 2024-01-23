package org.telegram.tutorbot.service.manager.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.tutorbot.bot.Bot;
import org.telegram.tutorbot.model.Timetable;
import org.telegram.tutorbot.model.User;
import org.telegram.tutorbot.model.UserDetails;
import org.telegram.tutorbot.model.enums.Action;
import org.telegram.tutorbot.model.enums.Role;
import org.telegram.tutorbot.model.enums.WeekDay;
import org.telegram.tutorbot.repository.TimetableRepository;
import org.telegram.tutorbot.repository.UserDetailsRepository;
import org.telegram.tutorbot.repository.UserRepository;
import org.telegram.tutorbot.util.MessageExecutor;
import org.telegram.tutorbot.util.factory.AnswerMethodFactory;
import org.telegram.tutorbot.util.factory.KeyboardFactory;
import org.telegram.tutorbot.service.manager.AbstractManager;
import static org.telegram.tutorbot.util.data.CallbackData.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class TimetableManager implements AbstractManager {
    private final AnswerMethodFactory answerMethodFactory;
    private final KeyboardFactory keyboardFactory;
    private final UserRepository userRepository;
    private final TimetableRepository timetableRepository;
    private final UserDetailsRepository userDetailsRepository;
    private final MessageExecutor messageExecutor;

    @Autowired
    public TimetableManager(AnswerMethodFactory answerMethodFactory,
                            KeyboardFactory keyboardFactory,
                            UserRepository userRepository,
                            TimetableRepository timetableRepository,
                            UserDetailsRepository userDetailsRepository,
                            MessageExecutor messageExecutor) {
        this.answerMethodFactory = answerMethodFactory;
        this.keyboardFactory = keyboardFactory;
        this.userRepository = userRepository;
        this.timetableRepository = timetableRepository;
        this.userDetailsRepository = userDetailsRepository;
        this.messageExecutor = messageExecutor;
    }

    @Override
    public BotApiMethod<?> answerCommand(Message message, Bot bot) {
        return getMenu(message);
    }

    @Override
    public BotApiMethod<?> answerMessage(Message message, Bot bot) {
        User user = userRepository.findUserByChatId(message.getChatId());
        messageExecutor.executeDeleteMessage(bot, message.getChatId(), message.getMessageId() - 1);
        switch (user.getAction()) {
            case SENDING_TITLE -> {
                return setTitle(user, message, bot);
            }
            case SENDING_DESCRIPTION -> {
                return setDescription(user, message, bot);
            }
        }
        return null;
    }

    @Override
    public BotApiMethod<?> answerCallbackQuery(CallbackQuery callbackQuery, Bot bot) {
        String callbackData = callbackQuery.getData();
        String[] splitCallbackData = callbackData.split("_");
        if (splitCallbackData.length > 1 && "add".equals(splitCallbackData[1])) {
            if (splitCallbackData.length == 2 || splitCallbackData.length == 3) {
                return add(callbackQuery, splitCallbackData);
            }
            switch (splitCallbackData[2]) {
                case WEEKDAY -> {
                    return addWeekDay(callbackQuery, splitCallbackData);
                }
                case HOUR -> {
                    return addHour(callbackQuery, splitCallbackData);
                }
                case MINUTE -> {
                    return addMinute(callbackQuery, splitCallbackData);
                }
                case USER -> {
                    return addUser(callbackQuery, splitCallbackData);
                }
                case TITLE -> {
                    return addTitle(callbackQuery, splitCallbackData);
                }
                case DESCRIPTION -> {
                    return addDescription(callbackQuery, splitCallbackData);
                }
            }
        }

        if (splitCallbackData.length > 1) {
            if (FINISH.equals(splitCallbackData[1])) {
                return finish(callbackQuery, splitCallbackData, bot);
            }
            if (BACK.equals(splitCallbackData[1])) {
                return back(callbackQuery, splitCallbackData);
            }
        }


        switch (callbackData) {
            case TIMETABLE -> {
                return getMenu(callbackQuery);
            }
            case TIMETABLE_SHOW -> {
                return show(callbackQuery);
            }
            case TIMETABLE_REMOVE -> {
                return remove(callbackQuery);
            }
            case TIMETABLE_1, TIMETABLE_2, TIMETABLE_3,
                    TIMETABLE_4, TIMETABLE_5, TIMETABLE_6,
                    TIMETABLE_7 -> {
                return showDayInfo(callbackQuery);
            }
        }
        return null;
    }

    private BotApiMethod<?> addWeekDay(CallbackQuery callbackQuery, String[] splitCallbackData) {
        UUID timetableId = UUID.fromString(splitCallbackData[4]);
        Timetable timetable = timetableRepository.findTimetableById(timetableId);
        WeekDay weekDay = WeekDay.getWeekDay(splitCallbackData[3]);
        timetable.setWeekDay(weekDay);
        timetableRepository.save(timetable);

        List<String> buttonsText = new ArrayList<>();
        List<String> buttonsCallbackData = new ArrayList<>();

        for (int i = 1; i <= 24; i++) {
            buttonsText.add(String.valueOf(i));
            buttonsCallbackData.add(TIMETABLE_ADD_HOUR + i + "_" + splitCallbackData[4]);
        }
        buttonsText.add("Назад");
        buttonsCallbackData.add(TIMETABLE_ADD + "_" + splitCallbackData[4]);

        InlineKeyboardMarkup keyboard = keyboardFactory.getInlineKeyboard(
                buttonsText,
                List.of(6, 6, 6, 6, 1),
                buttonsCallbackData
        );

        return answerMethodFactory.getEditMessage(
                callbackQuery,
                "Выбери час",
                keyboard
        );
    }

    private BotApiMethod<?> addHour(CallbackQuery callbackQuery, String[] splitCallbackData) {
        UUID timetableId = UUID.fromString(splitCallbackData[4]);
        Timetable timetable = timetableRepository.findTimetableById(timetableId);
        timetable.setHour(Short.valueOf(splitCallbackData[3]));
        timetableRepository.save(timetable);

        List<String> buttonsText = new ArrayList<>();
        List<String> buttonsCallbackData = new ArrayList<>();
        for (int i = 0; i <= 59; i++) {
            buttonsText.add(String.valueOf(i));
            buttonsCallbackData.add(TIMETABLE_ADD_MINUTE + i + "_" + timetableId);
        }
        buttonsText.add("Назад");
        buttonsCallbackData.add(TIMETABLE_ADD_WEEKDAY
                + WeekDay.getWeekDayNumber(timetable.getWeekDay()) + "_" + timetableId);

        InlineKeyboardMarkup keyboard = keyboardFactory.getInlineKeyboard(
                buttonsText,
                List.of(6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 1),
                buttonsCallbackData
        );

        return answerMethodFactory.getEditMessage(
                callbackQuery,
                "Выбери минуту",
                keyboard
        );
    }

    private BotApiMethod<?> addMinute(CallbackQuery callbackQuery, String[] data) {
        UUID timetableId = UUID.fromString(data[4]);
        Timetable timetable = timetableRepository.findTimetableById(timetableId);
        Long chatId = callbackQuery.getMessage().getChatId();
        User user = userRepository.findUserByChatId(chatId);
        timetable.setMinute(Short.valueOf(data[3]));
        timetableRepository.save(timetable);

        List<String> buttonsText = new ArrayList<>();
        List<String> buttonsCallbackData = new ArrayList<>();
        List<Integer> buttonsConfiguration = new ArrayList<>();

        int index = 0;
        for (User dependedUser : user.getUsers()) {
            buttonsText.add(dependedUser.getUserDetails().getFirstName());
            buttonsCallbackData.add(TIMETABLE_ADD_USER + dependedUser.getChatId() + "_" + timetableId);
            if (index == 5) {
                buttonsConfiguration.add(5);
                index = 0;
            } else {
                index++;
            }
        }
        if (index != 0) {
            buttonsConfiguration.add(index);
        }

        buttonsConfiguration.add(1);
        buttonsCallbackData.add(TIMETABLE_ADD_HOUR + timetable.getHour() + "_" + timetableId);
        buttonsText.add("Назад");
        String messageText = (buttonsConfiguration.size() == 1) ? "У тебя нет ни одного ученика" : "Выбери ученика";
        InlineKeyboardMarkup keyboard = keyboardFactory.getInlineKeyboard(
                buttonsText,
                buttonsConfiguration,
                buttonsCallbackData
        );

        return answerMethodFactory.getEditMessage(
                callbackQuery,
                messageText,
                keyboard
        );
    }

    private BotApiMethod<?> addUser(CallbackQuery callbackQuery, String[] data) {
        UUID timetableId = UUID.fromString(data[4]);
        Timetable timetable = timetableRepository.findTimetableById(timetableId);
        Long chatId = Long.valueOf(data[3]);
        User user = userRepository.findUserByChatId(chatId);

        timetable.addUser(user);
        timetable.setTitle(user.getUserDetails().getFirstName());
        timetableRepository.save(timetable);

        InlineKeyboardMarkup keyboard = keyboardFactory.getInlineKeyboard(
                List.of("Добавить название", "Добавить описание", "Завершить"),
                List.of(2, 1),
                List.of(
                        TIMETABLE_ADD_TITLE + timetableId,
                        TIMETABLE_ADD_DESCRIPTION + timetableId,
                        TIMETABLE_FINISH + timetableId
                )
        );

        return answerMethodFactory.getEditMessage(
                callbackQuery,
                "Все гуд, запись добавил. Теперь можешь добавить заголовок и описание задачи",
                keyboard
        );
    }

    private BotApiMethod<?> addTitle(CallbackQuery callbackQuery, String[] splitCallbackData) {
        String timetableId = splitCallbackData[3];
        Long chatId = callbackQuery.getMessage().getChatId();
        User user = userRepository.findUserByChatId(chatId);
        user.setAction(Action.SENDING_TITLE);
        UserDetails userDetails = user.getUserDetails();
        userDetails.setTimetableId(timetableId);
        user.setUserDetails(userDetails);

        userDetailsRepository.save(userDetails);
        userRepository.save(user);

        InlineKeyboardMarkup keyboard = keyboardFactory.getInlineKeyboard(
                List.of("Назад"),
                List.of(1),
                List.of(TIMETABLE_BACK + timetableId)
        );

        return answerMethodFactory.getEditMessage(
                callbackQuery,
                "Окей, пришли название",
                keyboard);
    }

    private BotApiMethod<?> addDescription(CallbackQuery callbackQuery, String[] splitCallbackData) {
        String timetableId = splitCallbackData[3];
        Long chatId = callbackQuery.getMessage().getChatId();
        User user = userRepository.findUserByChatId(chatId);
        user.setAction(Action.SENDING_DESCRIPTION);
        UserDetails userDetails = user.getUserDetails();
        userDetails.setTimetableId(timetableId);
        user.setUserDetails(userDetails);

        userDetailsRepository.save(userDetails);
        userRepository.save(user);

        InlineKeyboardMarkup keyboard = keyboardFactory.getInlineKeyboard(
                List.of("Назад"),
                List.of(1),
                List.of(TIMETABLE_BACK + timetableId)
        );

        return answerMethodFactory.getEditMessage(
                callbackQuery,
                "Пришли описание к задаче",
                keyboard);
    }

    private BotApiMethod<?> finish(CallbackQuery callbackQuery, String[] splitCallbackData, Bot bot) {
        UUID timetableId = UUID.fromString(splitCallbackData[2]);
        Timetable timetable = timetableRepository.findTimetableById(timetableId);
        timetable.setInCreation(false);
        timetableRepository.save(timetable);

        messageExecutor.executeAnswerCallbackQuery(bot, callbackQuery, "Запись в расписание успешно добавлена");

        return answerMethodFactory.getDeleteMessage(
                callbackQuery.getMessage().getChatId(),
                callbackQuery.getMessage().getMessageId()
        );
    }

    private BotApiMethod<?> back(Message message, String timetableId) {
        InlineKeyboardMarkup keyboard = keyboardFactory.getInlineKeyboard(
                List.of("Изменить название", "Изменить описание", "Завершить создание"),
                List.of(2, 1),
                List.of(
                        TIMETABLE_ADD_TITLE + timetableId,
                        TIMETABLE_ADD_DESCRIPTION + timetableId,
                        TIMETABLE_FINISH + timetableId
                )
        );

        return answerMethodFactory.getSendMessage(
                message.getChatId(),
                "Ты можешь настроить заголовок и описание к записи в расписании",
                keyboard
        );
    }

    private BotApiMethod<?> back(CallbackQuery callbackQuery, String[] splitCallbackData) {
        String timetableId = splitCallbackData[2];
        Long chatId = callbackQuery.getMessage().getChatId();
        User user = userRepository.findUserByChatId(chatId);
        user.setAction(Action.FREE);
        userRepository.save(user);
        InlineKeyboardMarkup keyboard = keyboardFactory.getInlineKeyboard(
                List.of("Изменить название", "Изменить описание", "Завершить создание"),
                List.of(2, 1),
                List.of(
                        TIMETABLE_ADD_TITLE + timetableId,
                        TIMETABLE_ADD_DESCRIPTION + timetableId,
                        TIMETABLE_FINISH + timetableId
                )
        );

        return answerMethodFactory.getEditMessage(
                callbackQuery,
                "Ты можешь настроить заголовок и описание к записи в расписании",
                keyboard
        );
    }

    private BotApiMethod<?> setTitle(User user, Message message, Bot bot) {
        UUID timetableId = UUID.fromString(user.getUserDetails().getTimetableId());
        user.setAction(Action.FREE);
        userRepository.save(user);
        Timetable timetable = timetableRepository.findTimetableById(timetableId);
        timetable.setTitle(message.getText());
        timetableRepository.save(timetable);

        messageExecutor.executeSendMessage(bot, message, "Заголовок сохранен", null);

        return back(message, String.valueOf(timetableId));
    }

    private BotApiMethod<?> setDescription(User user, Message message, Bot bot) {
        UUID timetableId = UUID.fromString(user.getUserDetails().getTimetableId());
        user.setAction(Action.FREE);
        userRepository.save(user);
        Timetable timetable = timetableRepository.findTimetableById(timetableId);
        timetable.setDescription(message.getText());
        timetableRepository.save(timetable);

        messageExecutor.executeSendMessage(bot, message, "Описание сохранено", null);

        return back(message, String.valueOf(timetableId));
    }

    private BotApiMethod<?> getMenu(Message message) {
        Long chatId = message.getChatId();
        User user = userRepository.findUserByChatId(chatId);
        Role role = user.getRole();

        if (role == Role.STUDENT) {
            InlineKeyboardMarkup keyboard = keyboardFactory.getInlineKeyboard(
                    List.of("Показать расписание"),
                    List.of(1),
                    List.of(TIMETABLE_SHOW)
            );

            return answerMethodFactory.getSendMessage(
                    chatId,
                    "📆 Здесь ты можешь посмотреть свое расписание",
                    keyboard);
        }

        InlineKeyboardMarkup keyboard = keyboardFactory.getInlineKeyboard(
                List.of("Показать расписание", "Добавить занятие", "Удалить занятие"),
                List.of(1, 2),
                List.of(TIMETABLE_SHOW, TIMETABLE_ADD, TIMETABLE_REMOVE)
        );

        return answerMethodFactory.getSendMessage(
                chatId,
                "📆 Здесь ты можешь управлять своим расписанием",
                keyboard);
    }

    private BotApiMethod<?> getMenu(CallbackQuery callbackQuery) {
        Long chatId = callbackQuery.getMessage().getChatId();
        User user = userRepository.findUserByChatId(chatId);
        Role role = user.getRole();

        if (role == Role.STUDENT) {
            InlineKeyboardMarkup keyboard = keyboardFactory.getInlineKeyboard(
                    List.of("Показать расписание"),
                    List.of(1),
                    List.of(TIMETABLE_SHOW)
            );

            return answerMethodFactory.getEditMessage(
                    callbackQuery,
                    "📆 Здесь ты можешь посмотреть свое расписание",
                    keyboard);
        }

        InlineKeyboardMarkup keyboard = keyboardFactory.getInlineKeyboard(
                List.of("Показать расписание", "Добавить занятие", "Удалить занятие"),
                List.of(1, 2),
                List.of(TIMETABLE_SHOW, TIMETABLE_ADD, TIMETABLE_REMOVE)
        );

        return answerMethodFactory.getEditMessage(
                callbackQuery,
                "📆 Здесь ты можешь управлять своим расписанием",
                keyboard);
    }

    private BotApiMethod<?> show(CallbackQuery callbackQuery) {
        InlineKeyboardMarkup keyboard = keyboardFactory.getInlineKeyboard(
                List.of(
                        "Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс",
                        "Назад"),
                List.of(7, 1),
                List.of(
                        TIMETABLE_1, TIMETABLE_2, TIMETABLE_3, TIMETABLE_4,
                        TIMETABLE_5, TIMETABLE_6, TIMETABLE_7, TIMETABLE
                )
        );

        return answerMethodFactory.getEditMessage(callbackQuery, "\uD83D\uDCC6 Выбери день недели", keyboard);
    }

    private BotApiMethod<?> add(CallbackQuery callbackQuery, String[] splitCallbackData) {
        String timetableId;
        if (splitCallbackData.length == 2) {
            Long chatId = callbackQuery.getMessage().getChatId();
            Timetable timetable = new Timetable();
            timetable.addUser(userRepository.findUserByChatId(chatId));
            timetable.setInCreation(true);
            timetableId = timetableRepository.save(timetable).getId().toString();
        } else {
            timetableId = splitCallbackData[2];
        }
        List<String> buttonsCallbackData = new ArrayList<>();
        for (int i = 1; i <= 7; i++) {
            buttonsCallbackData.add(TIMETABLE_ADD_WEEKDAY + i + "_" + timetableId);
        }
        buttonsCallbackData.add(TIMETABLE);

        InlineKeyboardMarkup keyboard = keyboardFactory.getInlineKeyboard(
                List.of(
                        "Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс",
                        "Назад"),
                List.of(7, 1),
                buttonsCallbackData
        );

        return answerMethodFactory.getEditMessage(
                callbackQuery,
                "✏️ Выбери день, в который хочешь добавить занятие",
                keyboard
        );
    }

    private BotApiMethod<?> remove(CallbackQuery callbackQuery) {
        InlineKeyboardMarkup keyboard = keyboardFactory.getInlineKeyboard(
                List.of("Назад"),
                List.of(1),
                List.of(TIMETABLE)
        );

        return answerMethodFactory.getEditMessage(
                callbackQuery,
                "✂️ Выбери занятие, которое хочешь удалить из своего расписания",
                keyboard);
    }

    private BotApiMethod<?> showDayInfo(CallbackQuery callbackQuery) {
        Long chatId = callbackQuery.getMessage().getChatId();
        User user = userRepository.findUserByChatId(chatId);
        String weekDayNumber = callbackQuery.getData().split("_")[1];
        WeekDay weekDay = WeekDay.getWeekDay(weekDayNumber);
        List<Timetable> timetableList = timetableRepository.findAllByUsersContainingAndWeekDay(user, weekDay);
        String sendMessage = getTimetableInfoText(timetableList);
        InlineKeyboardMarkup keyboard = keyboardFactory.getInlineKeyboard(
                List.of("Назад"),
                List.of(1),
                List.of(TIMETABLE_SHOW)
        );

        return answerMethodFactory.getEditMessage(callbackQuery, sendMessage, keyboard);
    }

    private String getTimetableInfoText(List<Timetable> timetableList) {
        StringBuilder text = new StringBuilder("У тебя нет занятий в этой день, так что отдыхай...");
        if (timetableList == null || timetableList.isEmpty()) {
            return text.toString();
        } else {
            text = new StringBuilder("Твои занятия на сегодня:\n\n");
            for (Timetable timetable : timetableList) {
                text.append(
                        String.format("%d:%d %s%n", timetable.getHour(), timetable.getMinute(), timetable.getTitle())
                );
            }
        }

        return text.toString();
    }
}
