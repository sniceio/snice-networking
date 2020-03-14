package io.snice.networking.codec.diameter.impl;

import io.snice.networking.codec.diameter.DiameterMessage;
import io.snice.networking.codec.diameter.avp.api.HostIpAddress;
import io.snice.networking.codec.diameter.avp.api.ProductName;

/**
 *
 */
public class DiameterEquality {

    /**
     * Comparing two {@link DiameterMessage}s is not an easy task. Therefore, only
     * the most basic comparison is done and is dependent on the type of request/answer.
     */
    static boolean equals(final DiameterMessage a, final DiameterMessage b) {
        if (a == b) {
            return true;
        }

        if (a == null || b == null) {
            return false;
        }

        if (a.getHeader().getCommandCode() != b.getHeader().getCommandCode() || a.isRequest() != b.isRequest()) {
            return false;
        }

        // if two answers, make sure the result codees are the same
        if (a.isAnswer() && !a.toAnswer().getResultCode().equals(b.toAnswer().getResultCode())) {
            return false;
        }

        if (!generalEquals(a, b)) {
            return false;
        }


        switch (a.getHeader().getCommandCode()) {
            case 257:
                return assertCerCea(a, b);
            default:
                return true;
        }
    }

    /**
     * Very basic check that rather claims that the two messages are NOT equal than being too relaxed.
     *
     * Put all basic checks here that applies to all types of messages. Put specific checks
     * in the specific equals
     */
    private static boolean generalEquals(final DiameterMessage a, final DiameterMessage b) {
        return a.getOriginHost().equals(b.getOriginHost()) &&
                a.getOriginRealm().equals(b.getOriginRealm()) &&
                a.getDestinationHost().equals(b.getDestinationHost()) &&
                a.getDestinationRealm().equals(b.getDestinationRealm());
    }

    /**
     * Special rules for CER and CEAs. Checks before this one is being called
     * guarantees that both messages are either a request or an answer.
     *
     * According to RFC, the Capabilities-Exchange-Request/Answer (section 5.3.1 and 5.3.2 in RFC6733)
     *
     */
    private static boolean assertCerCea(final DiameterMessage a, final DiameterMessage b) {
        final var ipA = a.getAvp(HostIpAddress.CODE);
        final var ipB = b.getAvp(HostIpAddress.CODE);

        final var productNameA = a.getAvp(ProductName.CODE);
        final var productNameB = b.getAvp(ProductName.CODE);
        return ipA.equals(ipB) && productNameA.equals(productNameB);
    }

}
