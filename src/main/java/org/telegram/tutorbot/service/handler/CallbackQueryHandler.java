package org.telegram.tutorbot.service.handler;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.tutorbot.bot.Bot;

@Service
public class CallbackQueryHandler {
    public BotApiMethod<?> answer(CallbackQuery callbackQuery, Bot bot) {
        return null;
    }
}
