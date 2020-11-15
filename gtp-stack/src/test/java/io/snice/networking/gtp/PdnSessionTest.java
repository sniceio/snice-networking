package io.snice.networking.gtp;

import io.snice.buffer.Buffers;
import io.snice.codecs.codec.gtp.GtpMessage;
import io.snice.codecs.codec.gtp.Teid;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class PdnSessionTest extends GtpStackTestBase {

    @Test
    public void testCreatePdnSession() throws Exception {
        final var req = GtpMessage.frame(loadRaw("create_session_request.raw")).toGtp2Request();
        final var res = GtpMessage.frame(loadRaw("create_session_response.raw")).toGtp2Response();

        final var session = PdnSession.of(req, res);

        final var localBearer = session.getDefaultLocalBearer();
        final var remoteBearer = session.getDefaultRemoteBearer();

        assertThat(localBearer.getEbi().get().getValue().getId(), is(5));
        assertThat(remoteBearer.getEbi().get().getValue().getId(), is(5));


        assertThat(localBearer.getIPv4Address().get(), is(Buffers.wrapAsIPv4("52.202.165.16")));
        assertThat(localBearer.getIPv4AddressAsString().get(), is("52.202.165.16"));
        assertThat(localBearer.getTeid(), is(Teid.of(0x12, 0xed, 0xe3, 0x72)));

        assertThat(remoteBearer.getIPv4Address().get(), is(Buffers.wrapAsIPv4("172.22.184.67")));
        assertThat(remoteBearer.getIPv4AddressAsString().get(), is("172.22.184.67"));
        assertThat(remoteBearer.getTeid(), is(Teid.of(0x4c, 0xa4, 0x0c, 0xe5)));

        assertThat(session.getPaa().getValue().getIPv4Address().get().toIPv4String(0), is("100.64.12.229"));
    }
}