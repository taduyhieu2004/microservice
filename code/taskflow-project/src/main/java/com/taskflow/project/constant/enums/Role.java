package com.taskflow.project.constant.enums;

public enum Role {
    OWNER(5),
    ADMIN(4),
    EDITOR(3),
    COMMENTER(2),
    VIEWER(1);

    private final int level;

    Role(int level) {
        this.level = level;
    }

    public int level() {
        return level;
    }

    public boolean isAtLeast(Role other) {
        return this.level >= other.level;
    }
}
