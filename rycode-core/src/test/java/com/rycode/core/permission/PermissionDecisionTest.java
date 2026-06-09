package com.rycode.core.permission;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PermissionDecisionTest {

    @Test
    void allowsBlankReasonForAllowDecision() {
        PermissionDecision decision = PermissionDecision.allow(" ");

        assertThat(decision.reason()).isBlank();
    }

    @Test
    void rejectsBlankReasonForAskDecision() {
        assertThatThrownBy(() -> PermissionDecision.ask(" "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("reason must not be blank for ASK decision");
    }

    @Test
    void rejectsBlankReasonForDenyDecision() {
        assertThatThrownBy(() -> PermissionDecision.deny(" "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("reason must not be blank for DENY decision");
    }
}
