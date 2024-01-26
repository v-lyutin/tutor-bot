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
public class HelpManager implements AbstractManager {
    private final AnswerMethodFactory answerMethodFactory;
    private final KeyboardFactory keyboardFactory;
    private static final String HELP_TEXT = """
            📝<b> Доступные команды:</b>
            
            /start - <i><b>главное меню</b></i>
            /profile - <i><b>просмотр профиля</b></i>
            /search - <i><b>создать связь с пользователем противоположной роли</b></i>
            /timetable - <i><b>меню расписания</b></i>
            /help - <i><b>помощь</b></i>
            /feedback - <i><b>support</b></i>
            """;

    @Autowired
    public HelpManager(AnswerMethodFactory answerMethodFactory, KeyboardFactory keyboardFactory) {
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

        return answerMethodFactory.getSendMessage(message.getChatId(), HELP_TEXT, keyboard);
    }

    @Override
    public BotApiMethod<?> answerCallbackQuery(CallbackQuery callbackQuery, Bot bot) {
        InlineKeyboardMarkup keyboard = keyboardFactory.getInlineKeyboard(
                List.of("Назад"),
                List.of(1),
                List.of(START)
        );

        return answerMethodFactory.getEditMessage(callbackQuery, HELP_TEXT, keyboard);
    }

    @Override
    public BotApiMethod<?> answerMessage(Message message, Bot bot) {
        return null;
    }
}
