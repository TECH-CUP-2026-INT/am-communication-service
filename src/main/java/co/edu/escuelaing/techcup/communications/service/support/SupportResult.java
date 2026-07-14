package co.edu.escuelaing.techcup.communications.service.support;

import co.edu.escuelaing.techcup.communications.entity.enums.SupportLevel;
import co.edu.escuelaing.techcup.communications.entity.enums.SupportOutcome;

public record SupportResult(SupportOutcome outcome, SupportLevel from, SupportLevel to, String message) {

    public static SupportResult escalated(SupportLevel from, SupportLevel to) {
        return new SupportResult(SupportOutcome.ESCALATED, from, to, "Escalated from %s to %s".formatted(from, to));
    }

    public static SupportResult finalized(SupportLevel from, SupportLevel to) {
        return new SupportResult(SupportOutcome.FINALIZED, from, to, "Finalized at %s".formatted(to));
    }

    /**
     * A tier answered without advancing the ticket — a workflow signal only. It does not call
     * {@code SupportTicket#resolve()} (the unrelated terminal ticket-status concept); the ticket
     * stays open at {@code level}, waiting for the user to decide whether to escalate.
     */
    public static SupportResult resolved(SupportLevel level) {
        return new SupportResult(SupportOutcome.RESOLVED, level, level, "Resolved at %s".formatted(level));
    }

    public static SupportResult pending(SupportLevel level) {
        return new SupportResult(SupportOutcome.PENDING, level, level, "No handler available; ticket left pending");
    }
}
