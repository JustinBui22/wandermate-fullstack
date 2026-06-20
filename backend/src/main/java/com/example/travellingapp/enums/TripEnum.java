package com.example.travellingapp.enums;

import lombok.Getter;


@Getter
public enum TripCollaborationEnum {

    INVITATION("INVITATION", "Owner invited user to join trip", Group.REQUEST_TYPE),
    JOIN_REQUEST("JOIN_REQUEST", "User requested to join trip", Group.REQUEST_TYPE),

    PENDING("PENDING", "Waiting for response", Group.REQUEST_STATUS),
    ACCEPTED("ACCEPTED", "Request accepted", Group.REQUEST_STATUS),
    REJECTED("REJECTED", "Request rejected", Group.REQUEST_STATUS),
    CANCELLED("CANCELLED", "Request cancelled", Group.REQUEST_STATUS),

    OWNER("OWNER", "User who created the plan originally", Group.MEMBER_ROLE),
    EDITOR("EDITOR", "User who can modify the plan", Group.MEMBER_ROLE),
    VIEWER("VIEWER", "User who can only view and suggest changes for the plan", Group.MEMBER_ROLE),

    ;
    private final String code;
    private final String description;
    private final Group group;

    TripCollaborationEnum(String code, String description, Group group) {
        this.code = code;
        this.description = description;
        this.group = group;
    }

    public enum Group {
        REQUEST_TYPE,
        REQUEST_STATUS,
        MEMBER_ROLE
    }
}
