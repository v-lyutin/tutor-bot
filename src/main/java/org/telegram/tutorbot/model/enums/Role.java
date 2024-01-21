package org.telegram.tutorbot.model.enums;

public enum Role {
    STUDENT("Ученик"),
    TEACHER("Учитель"),
    ADMIN("Отец"),
    EMPTY("Anonymous");

    private final String role;

    Role(String role) {
        this.role = role;
    }

    public String getValue() {
        return role;
    }
}
