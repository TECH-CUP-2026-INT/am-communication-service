package co.edu.escuelaing.techcup.communications.entity.enums;

/**
 * Support escalation levels ordered from first to last.
 * The escalation flow is: FAQ -> CHATBOT -> MODERATOR -> ORGANIZER -> PENDING.
 */
public enum SupportLevel {
    FAQ,
    CHATBOT,
    MODERATOR,
    ORGANIZER,
    PENDING;

    /**
     * @return the next level in the escalation chain; PENDING is terminal and returns itself.
     */
    public SupportLevel next() {
        SupportLevel[] levels = values();
        int nextIndex = Math.min(ordinal() + 1, levels.length - 1);
        return levels[nextIndex];
    }

    public boolean isTerminal() {
        return this == PENDING;
    }
}
