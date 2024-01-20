package org.telegram.tutorbot.bot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.tutorbot.bot.config.BotConfig;
import org.telegram.tutorbot.bot.service.UpdateDispatcher;

@Component
public class Bot extends TelegramWebhookBot {
    private final BotConfig botConfig;
    private final UpdateDispatcher updateDispatcher;

    @Autowired
    public Bot(BotConfig botConfig, UpdateDispatcher updateDispatcher) {
        super(botConfig.getToken());
        this.botConfig = botConfig;
        this.updateDispatcher = updateDispatcher;
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        return updateDispatcher.distribute(update, this);
    }

    @Override
    public String getBotPath() {
        return botConfig.getPath();
    }

    @Override
    public String getBotUsername() {
        return botConfig.getUsername();
    }
}
