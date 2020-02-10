package io.snice.networking.codec.diameter.avp;

import java.util.Optional;

/**
 * Helper enum for whether or not the vendor-bit in the AVP should
 * be set. Is used by the code generators when generating the various
 * AVPs.
 */
public enum AvpVendor {
    MUST(true), MAY(false), MUST_NOT(false), SHOULD_NOT(false);

    private final boolean isVendor;

    public static Optional<AvpVendor> getValue(final String name) {
        for (AvpVendor v : values()) {
            if (v.toString().replace("_", "").equalsIgnoreCase(name)) {
                return Optional.of(v);
            }
        }

        return Optional.empty();
    }

    AvpVendor(final boolean isVendor) {
        this.isVendor = isVendor;
    }

    public boolean isVendor() {
        return isVendor;
    }
}
