package com.example.travellingapp.enums;

import lombok.Getter;

@Getter
public enum UserSettingEnum {


    LIGHT("LIGHT", "Light theme", Group.THEME),
    DARK("DARK", "Dark theme", Group.THEME),
    SYSTEM("SYSTEM", "Based on system's theme", Group.THEME),

    ;
    private final String code;
    private final String description;
    private final Group group;

    UserSettingEnum(String code, String description, Group group) {
        this.code = code;
        this.description = description;
        this.group = group;
    }

    public enum Group {
        THEME,
    }
}
