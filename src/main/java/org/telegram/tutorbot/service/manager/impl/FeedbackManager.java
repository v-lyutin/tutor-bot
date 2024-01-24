package org.telegram.tutorbot.service.manager.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.tutorbot.bot.Bot;
import org.telegram.tutorbot.util.factory.AnswerMethodFactory;
import org.telegram.tutorbot.service.manager.AbstractManager;
import org.telegram.tutorbot.util.factory.KeyboardFactory;
import java.util.List;
import static org.telegram.tutorbot.util.data.CallbackData.START;

@Component
public class FeedbackManager implements AbstractManager {
    private final AnswerMethodFactory answerMethodFactory;
    private final KeyboardFactory keyboardFactory;
    private static final String FEEDBACK_TEXT = """
            🔗 <b>Ссылочки для обратной связи:</b>
                            
            ▪️<a href="https://github.com/v-lyutin">GitHub</a>
            ▪️<a href="https://t.me/wurhez">Telegram</a>
            """;

    @Autowired
    public FeedbackManager(AnswerMethodFactory answerMethodFactory, KeyboardFactory keyboardFactory) {
        this.answerMethodFactory = answerMethodFactory;
        this.keyboardFactory = keyboardFactory;
    }

    @Override
    public BotApiMethod<?> answerCommand(Message message, Bot bot) {
        InlineKeyboardMarkup keyboard = keyboardFactory.getInlineKeyboard(
                List.of("Главное меню"),
                List.of(1),
                List.of(START)
        );

        return answerMethodFactory.getSendMessage(message.getChatId(), FEEDBACK_TEXT, keyboard);
    }

    @Override
    public BotApiMethod<?> answerCallbackQuery(CallbackQuery callbackQuery, Bot bot) {
        InlineKeyboardMarkup keyboard = keyboardFactory.getInlineKeyboard(
                List.of("Назад"),
                List.of(1),
                List.of(START)
        );

        return answerMethodFactory.getEditMessage(callbackQuery, FEEDBACK_TEXT, keyboard);
    }

    @Override
    public BotApiMethod<?> answerMessage(Message message, Bot bot) {
        return null;
    }
}
