package io.snice.networking.gtp;

import io.snice.codecs.codec.gtp.gtpc.v2.Gtp2Response;
import io.snice.codecs.codec.gtp.gtpc.v2.messages.tunnel.CreateSessionRequest;
import io.snice.networking.gtp.conf.GtpAppConfig;

import java.util.concurrent.CompletionStage;
import java.util.function.BiConsumer;

public interface PdnSession extends Session {

    static Builder of(final CreateSessionRequest csr) {
        return null;
    }

    PdnSessionContext getContext();

    /**
     * Terminate the {@link PdnSession}, which will be accomplished by sending a Delete Session Request
     * to the remote entity (such as a PGW). Once you have asked a session to terminate there is no going
     * back and any future interaction with this {@link PdnSession} will result in an {@link IllegalStateException}
     * being thrown.
     * <p>
     * Once the {@link PdnSession} has been terminated, the callback as re
     * <p>
     * TODO: not really sure I need this. Probably just have that as part of the original onSessionTerminated in the builder
     * or something...
     */
    void terminate();

    /**
     * Ask the session to establish the default bearer. Assuming this {@link PdnSession} was successfully
     * established, the
     *
     * @return
     */
    EpsBearer establishDefaultBearer();

    interface Builder<C extends GtpAppConfig> {

        /**
         * The IP address of the remote element, such as the PGW. This is a mandatory
         * parameter so if not given, the builder will blow up when you try and
         * {@link #start()} this session
         *
         * @param ipv4 a IPv4 address in human readable form.
         * @throws IllegalArgumentException in case the IPv4 address is not in fact an IPv4 address
         */
        Builder<C> withRemoteIPv4(String ipv4);

        /**
         * The port of the remote element, such as the PGW. If non is specified,
         * port 2123 will be used.
         */
        Builder<C> withRemotePort(int port);

        /**
         * Register a callback that will be called when this session has been terminated (success or fail)
         *
         * @param f the function that will be called when this session terminates.
         */
        Builder<C> onSessionTerminated(BiConsumer<PdnSession, Gtp2Response> f);

        CompletionStage<PdnSession> start();
    }
}
