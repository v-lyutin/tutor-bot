package org.telegram.tutorbot.service.manager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.tutorbot.service.factory.AnswerMethodFactory;
import org.telegram.tutorbot.service.factory.KeyboardFactory;
import java.util.List;
import static org.telegram.tutorbot.service.data.CallbackData.*;

@Component
public class StartManager {
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

    public BotApiMethod<?> answerCommand(Message message) {
        InlineKeyboardMarkup keyboard = keyboardFactory.getInlineKeyboard(
                List.of("Помощь", "Обратная связь"),
                List.of(2),
                List.of(HELP, FEEDBACK)
        );
        return answerMethodFactory.getSendMessage(message.getChatId(), START_TEXT, keyboard);
    }
}
