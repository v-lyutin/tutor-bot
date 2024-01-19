package org.telegram.tutorbot.bot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.tutorbot.config.BotProperties;

@Component
public class Bot extends TelegramWebhookBot {
    private final BotProperties botProperties;

    @Autowired
    public Bot(BotProperties botProperties) {
        super(botProperties.getToken());
        this.botProperties = botProperties;
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        return null;
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
