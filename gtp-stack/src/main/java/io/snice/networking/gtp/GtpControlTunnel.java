package io.snice.networking.gtp;

import io.snice.codecs.codec.gtp.GtpMessage;
import io.snice.codecs.codec.gtp.GtpRequest;
import io.snice.codecs.codec.gtp.gtpc.v2.Gtp2Request;

public interface GtpControlTunnel extends GtpTunnel {

    @Override
    default boolean isControlTunnel() {
        return true;
    }

    @Override
    default GtpControlTunnel toControlTunnel() {
        return this;
    }

    /**
     * Have the given {@link GtpRequest} run within a {@link Transaction}, which means that the
     * underlying GTP stack will handle re-transmissions etc.
     * <p>
     * Note: not all {@link GtpRequest}s expect a reply and as such cannot run within a {@link Transaction}.
     * If asked to do so anyway, a {@link IllegalGtpMessageException} will be thrown.
     *
     * @param request
     * @return
     * @throws IllegalGtpMessageException in case the given {@link GtpMessage} cannot be run within a {@link Transaction}
     */
    Transaction.Builder createNewTransaction(Gtp2Request request) throws IllegalGtpMessageException;
}
