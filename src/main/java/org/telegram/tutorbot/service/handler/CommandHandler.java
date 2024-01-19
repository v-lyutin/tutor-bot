package org.telegram.tutorbot.service.handler;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.tutorbot.bot.Bot;

@Service
public class CommandHandler {
    public BotApiMethod<?> answer(Message message, Bot bot) {
        return null;
    }
}
