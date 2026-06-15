package com.example.travellingapp.enums;

import lombok.Getter;

@Getter
public enum TripMemberRoleEnum {

    OWNER("OWNER", "User who created the plan originally"),
    EDITOR("EDITOR", "User who can modify the plan"),
    VIEWER("VIEWER", "User who can only view and suggest changes for the plan"),

    ;

    private final String code;
    private final String description;

    TripMemberRoleEnum(String code, String description) {
        this.code = code;
        this.description = description;

    }
}
