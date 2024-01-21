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
import java.util.List;
import static org.telegram.tutorbot.util.data.CallbackData.*;

@Component
public class ProgressControlManager implements AbstractManager {
    private final AnswerMethodFactory answerMethodFactory;
    private final KeyboardFactory keyboardFactory;

    @Autowired
    public ProgressControlManager(AnswerMethodFactory answerMethodFactory, KeyboardFactory keyboardFactory) {
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
            case PROGRESS -> {
                return getMenu(callbackQuery);
            }
            case PROGRESS_STAT -> {
                return getStat(callbackQuery);
            }
        }
        return null;
    }

    private BotApiMethod<?> getMenu(Message message) {
        InlineKeyboardMarkup keyboard = keyboardFactory.getInlineKeyboard(
                List.of("Прикрепить домашнее задание"),
                List.of(1),
                List.of(PROGRESS_STAT)
        );
        return answerMethodFactory.getSendMessage(message.getChatId(), "Здесь вы можете увидеть...", keyboard);
    }

    private BotApiMethod<?> getMenu(CallbackQuery callbackQuery) {
        InlineKeyboardMarkup keyboard = keyboardFactory.getInlineKeyboard(
                List.of("Статистика успеваемости"),
                List.of(1),
                List.of(PROGRESS_STAT)
        );
        return answerMethodFactory.getEditMessage(callbackQuery, "Здесь вы можете увидеть...", keyboard);
    }

    private BotApiMethod<?> getStat(CallbackQuery callbackQuery) {
        InlineKeyboardMarkup keyboard = keyboardFactory.getInlineKeyboard(
                List.of("Назад"),
                List.of(1),
                List.of(PROGRESS)
        );
        return answerMethodFactory.getEditMessage(callbackQuery, "Здесь будет статистика", keyboard);
    }
}
