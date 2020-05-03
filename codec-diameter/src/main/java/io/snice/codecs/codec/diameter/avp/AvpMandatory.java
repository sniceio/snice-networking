package io.snice.codecs.codec.diameter.avp;

import java.util.Optional;

/**
 * Helper enum for whether or not the mandatory-bit in the AVP should
 * be set. Is used by the code generators when generating the various
 * AVPs.
 */
public enum AvpMandatory {
    MUST(true), MAY(false), MUST_NOT(false), SHOULD_NOT(false);

    private final boolean isMandatory;

    AvpMandatory(final boolean isMandatory) {
        this.isMandatory = isMandatory;
    }

    public static Optional<AvpMandatory> getValue(final String name) {
        for (final AvpMandatory m : values()) {
            if (m.toString().replace("_", "").equalsIgnoreCase(name)) {
                return Optional.of(m);
            }
        }

        return Optional.empty();
    }

    public boolean isMandatory() {
        return isMandatory;
    }
}
