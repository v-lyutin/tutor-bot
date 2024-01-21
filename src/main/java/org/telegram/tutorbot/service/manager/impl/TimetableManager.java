package org.telegram.tutorbot.service.manager.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.tutorbot.bot.Bot;
import org.telegram.tutorbot.util.factory.AnswerMethodFactory;
import org.telegram.tutorbot.util.factory.KeyboardFactory;
import org.telegram.tutorbot.service.manager.AbstractManager;
import static org.telegram.tutorbot.util.data.CallbackData.*;
import java.util.List;

@Component
public class TimetableManager implements AbstractManager {
    private final AnswerMethodFactory answerMethodFactory;
    private final KeyboardFactory keyboardFactory;
    private static final String TIMETABLE_TEXT = """
            📆 Здесь ты можешь управлять своим расписанием
            """;

    @Autowired
    public TimetableManager(AnswerMethodFactory answerMethodFactory, KeyboardFactory keyboardFactory) {
        this.answerMethodFactory = answerMethodFactory;
        this.keyboardFactory = keyboardFactory;
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
        }
        return null;
    }

    private BotApiMethod<?> getMenu(Message message) {
        InlineKeyboardMarkup keyboard = keyboardFactory.getInlineKeyboard(
                List.of("Показать расписание", "Добавить занятие", "Удалить занятие"),
                List.of(1, 2),
                List.of(TIMETABLE_SHOW, TIMETABLE_ADD, TIMETABLE_REMOVE)
        );
        return answerMethodFactory.getSendMessage(message.getChatId(), TIMETABLE_TEXT, keyboard);
    }

    private BotApiMethod<?> getMenu(CallbackQuery callbackQuery) {
        InlineKeyboardMarkup keyboard = keyboardFactory.getInlineKeyboard(
                List.of("Показать расписание", "Добавить занятие", "Удалить занятие"),
                List.of(1, 2),
                List.of(TIMETABLE_SHOW, TIMETABLE_ADD, TIMETABLE_REMOVE)
        );
        return answerMethodFactory.getEditMessage(callbackQuery, TIMETABLE_TEXT, keyboard);
    }

    private BotApiMethod<?> show(CallbackQuery callbackQuery) {
        InlineKeyboardMarkup keyboard = keyboardFactory.getInlineKeyboard(
                List.of("Назад"),
                List.of(1),
                List.of(TIMETABLE)
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
}
