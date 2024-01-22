package org.telegram.tutorbot.service.manager.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.tutorbot.bot.Bot;
import org.telegram.tutorbot.exception.ServiceException;
import org.telegram.tutorbot.model.Timetable;
import org.telegram.tutorbot.model.User;
import org.telegram.tutorbot.model.enums.Role;
import org.telegram.tutorbot.repository.TimetableRepository;
import org.telegram.tutorbot.repository.UserRepository;
import org.telegram.tutorbot.util.factory.AnswerMethodFactory;
import org.telegram.tutorbot.util.factory.KeyboardFactory;
import org.telegram.tutorbot.service.manager.AbstractManager;
import static org.telegram.tutorbot.util.data.CallbackData.*;
import java.time.DayOfWeek;
import java.util.List;

@Component
public class TimetableManager implements AbstractManager {
    private final AnswerMethodFactory answerMethodFactory;
    private final KeyboardFactory keyboardFactory;
    private final UserRepository userRepository;
    private final TimetableRepository timetableRepository;

    @Autowired
    public TimetableManager(AnswerMethodFactory answerMethodFactory,
                            KeyboardFactory keyboardFactory,
                            UserRepository userRepository,
                            TimetableRepository timetableRepository) {
        this.answerMethodFactory = answerMethodFactory;
        this.keyboardFactory = keyboardFactory;
        this.userRepository = userRepository;
        this.timetableRepository = timetableRepository;
    }

    @Override
    public BotApiMethod<?> answerCommand(Message message, Bot bot) {
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
            case TIMETABLE -> {
                return getMenu(callbackQuery);
            }
            case TIMETABLE_SHOW -> {
                return show(callbackQuery);
            }
            case TIMETABLE_ADD -> {
                return add(callbackQuery);
            }
            case TIMETABLE_REMOVE -> {
                return remove(callbackQuery);
            }
            case TIMETABLE_MONDAY, TIMETABLE_TUESDAY, TIMETABLE_WEDNESDAY,
                    TIMETABLE_THURSDAY, TIMETABLE_FRIDAY, TIMETABLE_SATURDAY,
                    TIMETABLE_SUNDAY -> {
                return showDayInfo(callbackQuery);
            }
        }
        return null;
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
                        "Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота", "Воскресенье",
                        "Назад"),
                List.of(7, 1),
                List.of(
                        TIMETABLE_MONDAY, TIMETABLE_TUESDAY, TIMETABLE_WEDNESDAY, TIMETABLE_THURSDAY,
                        TIMETABLE_FRIDAY, TIMETABLE_SATURDAY, TIMETABLE_SUNDAY, TIMETABLE
                )
        );

        return answerMethodFactory.getEditMessage(callbackQuery, "\uD83D\uDCC6 Выбери день недели", keyboard);
    }

    private BotApiMethod<?> add(CallbackQuery callbackQuery) {
        InlineKeyboardMarkup keyboard = keyboardFactory.getInlineKeyboard(
                List.of("Назад"),
                List.of(1),
                List.of(TIMETABLE)
        );

        return answerMethodFactory.getEditMessage(
                callbackQuery,
                "✏\uFE0F Выбери день, в который хочешь добавить занятие",
                keyboard);
    }

    private BotApiMethod<?> remove(CallbackQuery callbackQuery) {
        InlineKeyboardMarkup keyboard = keyboardFactory.getInlineKeyboard(
                List.of("Назад"),
                List.of(1),
                List.of(TIMETABLE)
        );

        return answerMethodFactory.getEditMessage(
                callbackQuery,
                "✂\uFE0F Выбери занятие, которое хочешь удалить из своего расписания",
                keyboard);
    }

    private BotApiMethod<?> showDayInfo(CallbackQuery callbackQuery) {
        Long chatId = callbackQuery.getMessage().getChatId();
        User user = userRepository.findUserByChatId(chatId);
        DayOfWeek dayOfWeek = parseDayOfWeekFromCallbackQuery(callbackQuery);
        List<Timetable> timetableList = timetableRepository.findAllByUsersContainingAndDayOfWeek(user, dayOfWeek);
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

    private DayOfWeek parseDayOfWeekFromCallbackQuery(CallbackQuery callbackQuery) {
        String dayOfWeek = callbackQuery.getData().split("_")[1];
        switch (dayOfWeek) {
            case "monday" -> {
                return DayOfWeek.MONDAY;
            }
            case "tuesday" -> {
                return DayOfWeek.TUESDAY;
            }
            case "wednesday" -> {
                return DayOfWeek.WEDNESDAY;
            }
            case "thursday" -> {
                return DayOfWeek.THURSDAY;
            }
            case "friday" -> {
                return DayOfWeek.FRIDAY;
            }
            case "saturday" -> {
                return DayOfWeek.SATURDAY;
            }
            case "sunday" -> {
                return DayOfWeek.SUNDAY;
            }
            default -> {
                throw new ServiceException(String.format("Invalid callbackData {%s}", dayOfWeek));
            }
        }
    }
}
