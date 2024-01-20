package org.telegram.tutorbot.service.manager;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class FeedbackManager {
    public BotApiMethod<?> answerCommand(Message message) {
        String messageText = """
                📍 Ссылки для обратной связи (с отцом Владика):
                                
                GitHub - https://github.com/v-lyutin
                Telegram - https://t.me/wurhez
                """;

        return SendMessage.builder()
                .chatId(message.getChatId())
                .text(messageText)
                .disableWebPagePreview(true)
                .build();
    }

    public BotApiMethod<?> answerCallbackQuery(CallbackQuery callbackQuery) {
        String messageText = """
                📍 Ссылки для обратной связи (с отцом Владика):
                                
                GitHub - https://github.com/v-lyutin
                Telegram - https://t.me/wurhez
                """;

        return EditMessageText.builder()
                .chatId(callbackQuery.getMessage().getChatId())
                .messageId(callbackQuery.getMessage().getMessageId())
                .text(messageText)
                .disableWebPagePreview(true)
                .build();
    }
}
