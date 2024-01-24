package org.telegram.tutorbot.util.factory;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeChat;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class AnswerMethodFactory {
    public SendMessage getSendMessage(Long chatId, String text, ReplyKeyboard keyboard) {
        return SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .replyMarkup(keyboard)
                .parseMode("HTML")
                .disableWebPagePreview(true)
                .build();
    }

    public EditMessageText getEditMessage(CallbackQuery callbackQuery, String text, InlineKeyboardMarkup keyboard) {
        return EditMessageText.builder()
                .chatId(callbackQuery.getMessage().getChatId())
                .messageId(callbackQuery.getMessage().getMessageId())
                .text(text)
                .replyMarkup(keyboard)
                .parseMode("HTML")
                .disableWebPagePreview(true)
                .build();
    }

    public DeleteMessage getDeleteMessage(Long chatId, Integer messageId) {
        return DeleteMessage.builder()
                .chatId(chatId)
                .messageId(messageId)
                .build();
    }

    public AnswerCallbackQuery getAnswerCallbackQuery(String callbackQueryId, String text) {
        return AnswerCallbackQuery.builder()
                .callbackQueryId(callbackQueryId)
                .text(text)
                .build();
    }

    public SetMyCommands setCommands(Long chatId, Map<String, String> commands) {
        List<BotCommand> botCommands = new ArrayList<>();
        BotCommandScopeChat botCommandScopeChat = BotCommandScopeChat.builder().chatId(chatId).build();

        for (String command : commands.keySet()) {
            botCommands.add(
                    BotCommand.builder()
                            .command(command)
                            .description(commands.get(command))
                            .build()
            );
        }

        return SetMyCommands.builder()
                .scope(botCommandScopeChat)
                .commands(botCommands)
                .build();
    }
}
