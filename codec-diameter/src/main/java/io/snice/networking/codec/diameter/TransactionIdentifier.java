package io.snice.networking.codec.diameter;

import io.snice.networking.codec.diameter.avp.api.OriginHost;

import static io.snice.preconditions.PreConditions.assertNotNull;

/**
 * RFC6733: Section 3, Page 35
 *
 * <p>
 * End-to-End Identifier
 * <p>
 * The End-to-End Identifier is an unsigned 32-bit integer field (in
 * network byte order) that is used to detect duplicate messages.
 * Upon reboot, implementations MAY set the high order 12 bits to
 * contain the low order 12 bits of current time, and the low order
 * 20 bits to a random value.  Senders of request messages MUST
 * insert a unique identifier on each message.  The identifier MUST
 * remain locally unique for a period of at least 4 minutes, even
 * across reboots.  The originator of an answer message MUST ensure
 * that the End-to-End Identifier field contains the same value that
 * was found in the corresponding request.  The End-to-End Identifier
 * MUST NOT be modified by Diameter agents of any kind.  The
 * combination of the Origin-Host AVP (Section 6.3) and this field is
 * used to detect duplicates.  Duplicate requests SHOULD cause the
 * same answer to be transmitted (modulo the Hop-by-Hop Identifier
 * <p>
 * The key in the above paragraph is that the end-to-end identifier AND
 * the Origin-Host AVP are the two fields that together identifies
 * a unique message, i.e., a transaction.
 */
public interface TransactionIdentifier {

    static TransactionIdentifier from(final DiameterMessage msg) {
        assertNotNull(msg);
        return new DefaultTransactionIdentifier(msg.getHeader().getEndToEndId(), msg.getOriginHost());
    }

    static TransactionIdentifier of(final long endToEndId, final OriginHost originHost) {
        assertNotNull(originHost);
        return new DefaultTransactionIdentifier(endToEndId, originHost);
    }

    class DefaultTransactionIdentifier implements TransactionIdentifier {
        private final long endToEndId;
        private final OriginHost originHost;

        private DefaultTransactionIdentifier(final long endToEndId, final OriginHost originHost) {
            this.endToEndId = endToEndId;
            this.originHost = originHost;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final DefaultTransactionIdentifier that = (DefaultTransactionIdentifier) o;

            if (endToEndId != that.endToEndId) return false;
            return originHost.equals(that.originHost);
        }

        @Override
        public int hashCode() {
            final int result = (int) (endToEndId ^ (endToEndId >>> 32));
            return 31 * result + originHost.hashCode();
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("TransactionIdentifier [");
            sb.append("End-To-End-ID: [").append(endToEndId);
            sb.append("] ");
            sb.append(originHost);
            sb.append("]");
            return sb.toString();
        }
    }
}
