package io.snice.networking.app.impl;

import io.snice.buffer.Buffer;
import io.snice.buffer.Buffers;
import io.snice.networking.app.NetworkAppConfig;
import io.snice.networking.common.Connection;
import io.snice.networking.common.ConnectionId;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class NettyBootstrapTest {

    private NetworkAppConfig conf;
    private NettyBootstrap<Connection<Buffer>, Buffer, NetworkAppConfig> bootstrap;

    @Before
    public void setup() throws Exception {
        conf = new NetworkAppConfig();
        bootstrap = new NettyBootstrap(conf);

    }

    @Test
    public void testBootstrapOnConnection() throws Exception {

        bootstrap.onConnection(ConnectionId::isUDP).drop();
        bootstrap.onConnection(ConnectionId::isTCP).accept(rules -> {
            rules.match(Buffer::isEmpty).map(b -> "empty").consume((c, s) -> c.send(Buffers.wrap(s)));
        });

        final var ctxs = bootstrap.getConnectionContexts();
        assertThat(ctxs.size(), is(2));

        assertThat(ctxs.get(0).isDrop(), is(true));

    }
}
