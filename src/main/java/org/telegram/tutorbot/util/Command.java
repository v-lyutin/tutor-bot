package org.telegram.tutorbot.util;

import org.telegram.tutorbot.model.enums.Role;
import java.util.HashMap;
import java.util.Map;

public class Command {
    private static final Map<String, String> commands = Map.of(
            "start", "начать взаимодействие с Владиком",
            "help", "получить справку по функционалу",
            "feedback", "обратная связь",
            "profile", "посмотреть свой профиль",
            "search", "установить связь с пользователем",
            "timetable", "расписание"
    );

    public static Map<String, String> getCommands(Role role) {
        if (role.equals(Role.STUDENT)) {
            return new HashMap<>(commands);
        } else {
            Map<String, String> teacherCommands = new HashMap<>(commands);
            teacherCommands.put("task", "добавить задание ученику");
            teacherCommands.put("progress", "отслеживание успеваемости");
            return teacherCommands;
        }
    }
}
