package com.rycode.core.permission;

import java.util.Objects;

public record PermissionDecision(PermissionDecisionType type, String reason) {

    public PermissionDecision {
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(reason, "reason must not be null");
        if (type == PermissionDecisionType.ASK && reason.isBlank()) {
            throw new IllegalArgumentException("reason must not be blank for ASK decision");
        }
        if (type == PermissionDecisionType.DENY && reason.isBlank()) {
            throw new IllegalArgumentException("reason must not be blank for DENY decision");
        }
    }

    public static PermissionDecision allow(String reason) {
        return new PermissionDecision(PermissionDecisionType.ALLOW, reason);
    }

    public static PermissionDecision ask(String reason) {
        return new PermissionDecision(PermissionDecisionType.ASK, reason);
    }

    public static PermissionDecision deny(String reason) {
        return new PermissionDecision(PermissionDecisionType.DENY, reason);
    }
}
