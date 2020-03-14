package io.snice.networking.netty;

import io.netty.channel.ChannelHandler;
import io.snice.networking.common.Transport;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static io.snice.preconditions.PreConditions.assertArgument;
import static io.snice.preconditions.PreConditions.assertNotEmpty;
import static io.snice.preconditions.PreConditions.assertNotNull;

public interface ProtocolHandler {

    String getName();

    Supplier<ChannelHandler> getDecoder();

    List<Transport> getTransports();

    static Builder of(final String name) {
        assertNotEmpty(name, "The name of the protocol handler cannot be null or the empty string");
        return new Builder(name);
    }

    class Builder {
        private final String name;
        private Supplier<ChannelHandler> handler;
        private final List<Transport> transports = new ArrayList<>();

        private Builder(final String name) {
            this.name = name;
        }

        public Builder withChannelHandler(final Supplier<ChannelHandler> handler) {
            assertNotNull(handler);
            this.handler = handler;
            return this;
        }

        public Builder withTransport(final Transport transport) {
            assertNotNull(transport);
            transports.add(transport);
            return this;
        }

        public ProtocolHandler build() {
            assertNotNull(handler, "The protocol handler must be defined");
            assertArgument(transports.size() > 0, "You must specify at least one transport ");
            return new DefaultProtocolHandler(name, handler, transports);
        }
    }

    class DefaultProtocolHandler implements ProtocolHandler {
        private final String name;
        private final Supplier<ChannelHandler> handler;
        private final List<Transport> transports;

        private DefaultProtocolHandler(final String name, final Supplier<ChannelHandler> handler, final List<Transport> transports) {
            this.name = name;
            this.handler = handler;
            this.transports = transports;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Supplier<ChannelHandler> getDecoder() {
            return handler;
        }

        @Override
        public List<Transport> getTransports() {
            return transports;
        }
    }

}
