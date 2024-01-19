package org.telegram.tutorbot.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.tutorbot.bot.Bot;

@RestController
@RequestMapping("/")
public class BotController {
    private final Bot bot;

    @Autowired
    public BotController(Bot bot) {
        this.bot = bot;
    }

    @PostMapping
    public BotApiMethod<?> listener(@RequestBody Update update) {
        if (update.hasMessage()) {
            return echo(update.getMessage());
        }

        return bot.onWebhookUpdateReceived(update);
    }

    private BotApiMethod<?> echo(Message message) {
        return SendMessage.builder()
                .chatId(message.getChatId())
                .text(message.getText())
                .build();
    }
}
