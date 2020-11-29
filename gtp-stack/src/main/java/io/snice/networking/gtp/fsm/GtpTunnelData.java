package io.snice.networking.gtp.fsm;

import io.hektor.fsm.Data;
import io.snice.buffer.Buffer;
import io.snice.codecs.codec.gtp.GtpMessage;
import io.snice.codecs.codec.gtp.gtpc.v2.Gtp2Request;
import io.snice.networking.gtp.conf.GtpConfig;

import java.util.HashMap;
import java.util.Map;

import static io.snice.preconditions.PreConditions.assertNotNull;

public class GtpTunnelData implements Data {

    private final GtpConfig config;

    private final Map<Buffer, InternalTransaction> transactions;

    public static GtpTunnelData of(final GtpConfig config) {
        assertNotNull(config);
        return new GtpTunnelData(config);
    }

    private GtpTunnelData(final GtpConfig config) {
        this.config = config;
        transactions = new HashMap<>(config.getControlPlane().getInitialTransactionStoreSize());
    }

    /**
     * Store away the transaction. Note: re-transmissions should already have been checked
     * and handled by the FSM before this method is called. Hence, no additional check
     * is done here for that.
     */
    public InternalTransaction storeTransaction(final Gtp2Request request, final boolean isClientTransaction) {
        final var header = request.toGtp2Request().getHeader();
        final var seqNo = header.getSequenceNo();
        final var internalTransaction = InternalTransaction.create(request, seqNo, isClientTransaction);
        transactions.put(seqNo, internalTransaction);
        return internalTransaction;
    }

    public InternalTransaction removeTransaction(final GtpMessage msg) {
        if (msg.isGtpVersion2()) {
            return transactions.remove(msg.toGtp2Message().getHeader().getSequenceNo());
        }
        throw new RuntimeException("No GTPv1 Support right now");
    }
}
