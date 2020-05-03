package io.snice.codecs.codec.diameter.avp;

import java.util.Optional;

/**
 * Helper enum for whether or not the mandatory-bit in the AVP should
 * be set. Is used by the code generators when generating the various
 * AVPs.
 */
public enum AvpProtected {
    MUST(true), MAY(false), MUST_NOT(false), SHOULD_NOT(false);

    private final boolean isProtected;

    public static Optional<AvpProtected> getValue(final String name) {
        for (final AvpProtected p : values()) {
            if (p.toString().replace("_", "").equalsIgnoreCase(name)) {
                return Optional.of(p);
            }
        }

        return Optional.empty();
    }

    AvpProtected(final boolean isProtected) {
        this.isProtected = isProtected;
    }

    public boolean isProtected() {
        return isProtected;
    }
}
