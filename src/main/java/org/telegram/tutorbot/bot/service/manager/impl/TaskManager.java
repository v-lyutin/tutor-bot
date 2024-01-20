package org.telegram.tutorbot.bot.service.manager.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.tutorbot.bot.Bot;
import org.telegram.tutorbot.bot.service.factory.AnswerMethodFactory;
import org.telegram.tutorbot.bot.service.factory.KeyboardFactory;
import org.telegram.tutorbot.bot.service.manager.AbstractManager;
import java.util.List;
import static org.telegram.tutorbot.bot.service.data.CallbackData.*;

@Component
public class TaskManager implements AbstractManager {
    private final AnswerMethodFactory answerMethodFactory;
    private final KeyboardFactory keyboardFactory;
    private static final String TASK_TEXT = "\uD83D\uDDC2 Ты можешь добавить домашнее задание вашему ученику";

    @Autowired
    public TaskManager(AnswerMethodFactory answerMethodFactory, KeyboardFactory keyboardFactory) {
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
            case TASK -> {
                return getMenu(callbackQuery);
            }
            case TASK_CREATE -> {
                return create(callbackQuery);
            }
        }
        return null;
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

    private BotApiMethod<?> create(CallbackQuery callbackQuery) {
        InlineKeyboardMarkup keyboard = keyboardFactory.getInlineKeyboard(
                List.of("Назад"),
                List.of(1),
                List.of(TASK)
        );
        return answerMethodFactory.getEditMessage(
                callbackQuery,
                "\uD83D\uDC64 Выбери ученика, которому хочешь дать домашнее задание",
                keyboard);
    }
}
