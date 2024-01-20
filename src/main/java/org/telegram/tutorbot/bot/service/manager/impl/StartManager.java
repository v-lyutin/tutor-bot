package org.telegram.tutorbot.bot.service.manager.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.tutorbot.bot.Bot;
import org.telegram.tutorbot.bot.service.factory.AnswerMethodFactory;
import org.telegram.tutorbot.bot.service.factory.KeyboardFactory;
import org.telegram.tutorbot.bot.service.manager.AbstractManager;
import java.util.List;
import static org.telegram.tutorbot.bot.service.data.CallbackData.*;

@Component
public class StartManager implements AbstractManager {
    private final AnswerMethodFactory answerMethodFactory;
    private final KeyboardFactory keyboardFactory;
    private static final String START_TEXT = """
            Подручный Владик приветствует. Я создан для упрощения взаимодействия репититора и ученика.
                                    
            Что вообще умею?
                                    
            📌 Составляю расписание
            📌 Прикрепляю домашние задания
            📌 Веду контроль успеваемости (дааа, бойся меня)
            """;

    @Autowired
    public StartManager(AnswerMethodFactory answerMethodFactory, KeyboardFactory keyboardFactory) {
        this.answerMethodFactory = answerMethodFactory;
        this.keyboardFactory = keyboardFactory;
    }

    @Override
    public BotApiMethod<?> answerCommand(Message message, Bot bot) {
        InlineKeyboardMarkup keyboard = keyboardFactory.getInlineKeyboard(
                List.of("Помощь", "Обратная связь"),
                List.of(2),
                List.of(HELP, FEEDBACK)
        );
        return answerMethodFactory.getSendMessage(message.getChatId(), START_TEXT, keyboard);
    }

    @Override
    public BotApiMethod<?> answerMessage(Message message, Bot bot) {
        return null;
    }

    @Override
    public BotApiMethod<?> answerCallbackQuery(CallbackQuery callbackQuery, Bot bot) {
        return null;
    }
}
