package io.snice.networking.codec.diameter.avp.type;

import io.snice.buffer.Buffers;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class IpAddressTest {

    @Test
    public void testDecode() throws Exception {
        final var b = Buffers.wrap((byte)0x00, (byte)0x01, (byte)0xac, (byte)0x16, (byte)0x12, (byte)0x78);
        final var ip = IpAddress.parse(b);
        assertThat(ip.isIPv4(), is(true));
        assertThat(ip.isIPv6(), is(false));

        assertThat(ip.asString(), is("172.22.18.120"));
    }

    @Test
    public void createIPv4Address() {
        final var ip = IpAddress.createIpv4Address("10.36.10.10");
        assertThat(ip.isIPv4(), is(true));
        assertThat(ip.asString(), is("10.36.10.10"));
    }

    @Test
    public void testEquality() {
        ensureEquals("127.0.0.1", "127.0.0.1");
        ensureEquals("10.36.10.10", "10.36.10.10");

        final var buffer = Buffers.wrap((byte)0x00, (byte)0x01, (byte)0xac, (byte)0x16, (byte)0x12, (byte)0x78);
        final var a = IpAddress.parse(buffer);
        final var b = IpAddress.createIpv4Address("172.22.18.120");
        ensureEquals(a, b);
    }

    private void ensureEquals(final String a, final String b)  {
        final var ipA = IpAddress.createIpv4Address(a);
        final var ipB = IpAddress.createIpv4Address(b);
        ensureEquals(ipA, ipB);
    }

    private void ensureEquals(final IpAddress a, final IpAddress b) {
        assertThat(a, is(b));
        assertThat(b, is(a));
        assertThat(a, is(a));
        assertThat(b, is(b));
    }

    @Test
    public void testDecodeNotEnoughBytes() {
        // IPv4
        ensureNotEnoughBytes((byte)0x00, (byte)0x01);
        ensureNotEnoughBytes((byte)0x00, (byte)0x01, (byte)0xac);
        ensureNotEnoughBytes((byte)0x00, (byte)0x01, (byte)0xac, (byte)0x16);
        ensureNotEnoughBytes((byte)0x00, (byte)0x01, (byte)0xac, (byte)0x16, (byte)0x12);

        // IPv6
        ensureNotEnoughBytes((byte)0x00, (byte)0x02, (byte)0xac, (byte)0x16, (byte)0x12, (byte)0x78);
    }

    private static void ensureNotEnoughBytes(final byte... bytes) {
        try {
            IpAddress.parse(Buffers.wrap(bytes));
            fail("Expected to blow up on a " + IllegalArgumentException.class);
        } catch (final IllegalArgumentException e) {
            // expected
        }
    }

}