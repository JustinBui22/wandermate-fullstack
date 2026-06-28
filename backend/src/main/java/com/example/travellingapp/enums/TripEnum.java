package com.example.travellingapp.enums;

import lombok.Getter;

@Getter
public enum TripEnum {

    INVITATION("INVITATION", "Owner invited user to join trip", Group.REQUEST_TYPE),
    JOIN_REQUEST("JOIN_REQUEST", "User requested to join trip", Group.REQUEST_TYPE),

    PENDING("PENDING", "Waiting for response", Group.REQUEST_STATUS),
    ACCEPTED("ACCEPTED", "Request accepted", Group.REQUEST_STATUS),
    REJECTED("REJECTED", "Request rejected", Group.REQUEST_STATUS),
    CANCELLED("CANCELLED", "Request cancelled", Group.REQUEST_STATUS),

    OWNER("OWNER", "User who created the plan originally", Group.MEMBER_ROLE),
    EDITOR("EDITOR", "User who can modify the plan", Group.MEMBER_ROLE),
    VIEWER("VIEWER", "User who can only view and suggest changes for the plan", Group.MEMBER_ROLE),

    PLANNING("PLANNING", "Trip is being planned", Group.STATUS),
    ONGOING("ONGOING", "Trip is ongoing", Group.STATUS),
    FINISHED("FINISHED", "Trip has finished", Group.STATUS),

    ALL("ALL", "All accessible trips", Group.OWNERSHIP_FILTER),
    CREATED("CREATED", "Trips created by the current user", Group.OWNERSHIP_FILTER),
    JOINED("JOINED", "Trips joined by the current user", Group.OWNERSHIP_FILTER),

    NAME_ASC("NAME_ASC", "Trip sorted alphabetically ascending", Group.SORT),
    NAME_DESC("NAME_DESC", "Trip sorted alphabetically descending", Group.SORT),
    CREATED_DATE_ASC("CREATED_DATE_ASC", "Trip sorted by created date ascending", Group.SORT),
    CREATED_DATE_DESC("CREATED_DATE_DESC", "Trip sorted by created date descending", Group.SORT),
    MODIFIED_DATE_ASC("MODIFIED_DATE_ASC", "Trip sorted by modified date ascending", Group.SORT),
    MODIFIED_DATE_DESC("MODIFIED_DATE_DESC", "Trip sorted by modified date descending", Group.SORT),

    ACTIVE("ACTIVE", "Trip share code is active", Group.SHARE_CODE_STATUS),
    USED("USED", "Trip share code has been used", Group.SHARE_CODE_STATUS),
    EXPIRED("EXPIRED", "Trip share code has expired", Group.SHARE_CODE_STATUS),
    REVOKED("REVOKED", "Trip share code has been revoked", Group.SHARE_CODE_STATUS),

    ;
    private final String code;
    private final String description;
    private final Group group;

    TripEnum(String code, String description, Group group) {
        this.code = code;
        this.description = description;
        this.group = group;
    }

    public enum Group {
        REQUEST_TYPE,
        REQUEST_STATUS,
        MEMBER_ROLE,
        STATUS,
        OWNERSHIP_FILTER,
        SORT,
        SHARE_CODE_STATUS
    }
}