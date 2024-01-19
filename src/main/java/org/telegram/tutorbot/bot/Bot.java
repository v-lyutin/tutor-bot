package org.telegram.tutorbot.bot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.tutorbot.config.BotProperties;
import org.telegram.tutorbot.service.UpdateDispatcher;

@Component
public class Bot extends TelegramWebhookBot {
    private final BotProperties botProperties;
    private final UpdateDispatcher updateDispatcher;

    @Autowired
    public Bot(BotProperties botProperties, UpdateDispatcher updateDispatcher) {
        super(botProperties.getToken());
        this.botProperties = botProperties;
        this.updateDispatcher = updateDispatcher;
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        return updateDispatcher.distribute(update, this);
    }

    @Override
    public String getBotPath() {
        return botProperties.getPath();
    }

    @Override
    public String getBotUsername() {
        return botProperties.getUsername();
    }
}
