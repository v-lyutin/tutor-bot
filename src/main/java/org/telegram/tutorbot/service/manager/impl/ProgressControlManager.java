package org.telegram.tutorbot.service.manager.impl;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.tutorbot.bot.Bot;
import org.telegram.tutorbot.service.manager.AbstractManager;

@Component
public class ProgressControlManager implements AbstractManager {
    @Override
    public BotApiMethod<?> answerCommand(Message message, Bot bot) {
        return null;
    }

    @Override
    public BotApiMethod<?> answerMessage(Message message, Bot bot) {
        return null;
    }

    @Override
    public BotApiMethod<?> answerCallbackQuery(CallbackQuery callbackQuery, Bot bot) {
        return null;
    }
}
