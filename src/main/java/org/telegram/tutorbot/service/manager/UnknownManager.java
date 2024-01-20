package org.telegram.tutorbot.service.manager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.tutorbot.service.factory.AnswerMethodFactory;
import java.util.List;
import java.util.Random;

@Component
public class UnknownManager {
    private final AnswerMethodFactory answerMethodFactory;
    private static final List<String> answers = List.of(
            "Сори, но я нефига не понял, так что повтори попытку))",
            "Такс, ты что-то путаешь... I don't understand \uD83D\uDE15",
            "Что-то не то написано, я таких команд не знаю, Jesuuuuuus...\uD83D\uDC7A",
            "Чо? \uD83E\uDD21",
            "Правильные мысли преследуют тебя, но ты все же быстрее них",
            "Встань на путь Божий и посмотри плиз названия команд"
    );

    @Autowired
    public UnknownManager(AnswerMethodFactory answerMethodFactory) {
        this.answerMethodFactory = answerMethodFactory;
    }

    public BotApiMethod<?> answerCommand(Message message) {
        String answer = getRandomAnswer();
        return answerMethodFactory.getSendMessage(message.getChatId(), answer, null);
    }

    private String getRandomAnswer() {
        Random random = new Random();
        int randomIndex = random.nextInt(answers.size());
        return answers.get(randomIndex);
    }
}
