package org.telegram.tutorbot.util.factory;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import java.util.ArrayList;
import java.util.List;

@Component
public class KeyboardFactory {
    public InlineKeyboardMarkup getInlineKeyboard(
            List<String> buttonNames,
            List<Integer> rowsConfiguration,
            List<String> callbackData
    ) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        int index = 0;
        for (Integer rowNumber : rowsConfiguration) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            for (int i = 0; i < rowNumber; i++) {
                InlineKeyboardButton button = InlineKeyboardButton.builder()
                        .text(buttonNames.get(index))
                        .callbackData(callbackData.get(index))
                        .build();
                row.add(button);
                index++;
            }
            keyboard.add(row);
        }

        return InlineKeyboardMarkup.builder()
                .keyboard(keyboard)
                .build();
    }

    public ReplyKeyboardMarkup getReplyKeyboard(List<String> buttonNames, List<Integer> rowsConfiguration) {
        List<KeyboardRow> keyboard = new ArrayList<>();
        int index = 0;
        for (Integer rowNumber : rowsConfiguration) {
            KeyboardRow row = new KeyboardRow();
            for (int i = 0; i < rowNumber; i++) {
                KeyboardButton button = KeyboardButton.builder()
                        .text(buttonNames.get(index))
                        .build();
                row.add(button);
                index++;
            }
            keyboard.add(row);
        }

        return ReplyKeyboardMarkup.builder()
                .keyboard(keyboard)
                .build();
    }
}
