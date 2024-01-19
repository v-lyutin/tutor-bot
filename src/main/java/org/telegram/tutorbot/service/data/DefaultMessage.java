package org.telegram.tutorbot.service.data;

import java.util.List;
import java.util.Random;

public class DefaultMessage {
    private static final List<String> messages = List.of(
            "Сори, но я нефига не понял, так что повтори попытку))",
            "Такс, ты что-то путаешь... I don't understand \uD83D\uDE15",
            "Что-то не то написано, я таких команд не знаю, Jesuuuuuus...\uD83D\uDC7A",
            "Чо? \uD83E\uDD21",
            "Правильные мысли преследуют тебя, но ты все же быстрее них",
            "Встань на путь Божий и посмотри плиз названия команд"
    );

    public static String getDefaultMessage() {
        Random random = new Random();
        int randomIndex = random.nextInt(messages.size());
        return messages.get(randomIndex);
    }
}
