package org.telegram.tutorbot.service.manager;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.tutorbot.bot.Bot;

public interface AbstractManager {
    BotApiMethod<?> answerCommand(Message message, Bot bot);
    BotApiMethod<?> answerMessage(Message message, Bot bot) throws TelegramApiException;
    BotApiMethod<?> answerCallbackQuery(CallbackQuery callbackQuery, Bot bot);
}
