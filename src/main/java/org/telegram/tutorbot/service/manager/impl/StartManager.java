package org.telegram.tutorbot.service.manager.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.tutorbot.bot.Bot;
import org.telegram.tutorbot.util.factory.AnswerMethodFactory;
import org.telegram.tutorbot.util.factory.KeyboardFactory;
import org.telegram.tutorbot.service.manager.AbstractManager;
import java.util.List;
import static org.telegram.tutorbot.util.data.CallbackData.*;

@Component
public class StartManager implements AbstractManager {
    private final AnswerMethodFactory answerMethodFactory;
    private final KeyboardFactory keyboardFactory;
    private static final String START_TEXT = """
            👋 <b>Подручный Владик приветствует. Я создан для упрощения взаимодействия репититора и ученика.
                                    
            Что вообще умею?</b>
                                    
            📌 <i><b>Составляю расписание</b></i>
            📌 <i><b>Прикрепляю домашние задания</b></i>
            📌 <i><b>Веду контроль успеваемости (дааа, бойся меня)</b></i>
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
        InlineKeyboardMarkup keyboard = keyboardFactory.getInlineKeyboard(
                List.of("Помощь", "Обратная связь"),
                List.of(2),
                List.of(HELP, FEEDBACK)
        );

        return answerMethodFactory.getEditMessage(callbackQuery, START_TEXT, keyboard);
    }
}
