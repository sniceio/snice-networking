package io.snice.networking.gtp.impl;

import io.snice.codecs.codec.gtp.GtpMessage;
import io.snice.codecs.codec.gtp.gtpc.v2.Gtp2Request;
import io.snice.networking.gtp.DataTunnel;
import io.snice.networking.gtp.GtpStack;
import io.snice.networking.gtp.IllegalGtpMessageException;
import io.snice.networking.gtp.Transaction;
import io.snice.networking.gtp.conf.GtpAppConfig;
import io.snice.networking.gtp.event.GtpMessageWriteEvent;

/**
 * Operations needed for the implementation but that should not be exposed to the application
 * users.
 */
public interface InternalGtpStack<C extends GtpAppConfig> extends GtpStack<C> {

    void send(GtpMessageWriteEvent event);

    void send(GtpMessageWriteEvent msg, InternalGtpControlTunnel tunnel);

    void send(GtpMessage msg, InternalGtpControlTunnel tunnel);

    void send(GtpMessage msg, InternalGtpUserTunnel tunnel);


    Transaction.Builder createNewTransaction(InternalGtpControlTunnel tunnel, Gtp2Request request) throws IllegalGtpMessageException;

    <T> DataTunnel.Builder<T> createDataTunnel(InternalGtpUserTunnel tunnel, Class<T> type, String remoteHost, int port);
}
