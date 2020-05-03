package io.snice.codecs.codec.diameter.avp.type;

import io.snice.buffer.Buffer;
import io.snice.buffer.Buffers;
import io.snice.buffer.WritableBuffer;
import io.snice.net.IPv4;

import java.util.Objects;

import static io.snice.preconditions.PreConditions.assertArgument;
import static io.snice.preconditions.PreConditions.assertNotEmpty;
import static io.snice.preconditions.PreConditions.assertNotNull;

public interface IpAddress extends DiameterType {

    static IpAddress parse(final Buffer data) {
        assertNotNull(data);
        assertArgument(data.capacity() >= 2 + 4, "The buffer cannot contain a valid Diameter Address because it is less than 6 bytes long");
        final var isIPv4 = data.getByte(1) == 0x01;
        final var isIPv6 = data.getByte(1) == 0x02;

        // perhaps this is a "human readable" IP address so let's check for that too...
        if (!isIPv4 && !isIPv6) {
            if (data.countOccurences('.') == 3) {
                return IpAddress.createIpv4Address(data);
            }
            throw new IllegalArgumentException("Unknown Address family. Only IPv4 and IPv6 are valid address families");
        }

        final var size = isIPv4 ? 2 + 4 : 2 + 16;
        assertArgument(data.capacity() >= size, "Not enough data in the buffer to create a Diameter Address");

        final var b = data.slice(size);
        return new DefaultIPAddress(isIPv4, b);
    }

    /**
     * Create a IPv4 address based on the given string, which is expected
     * to be in a human readable form, i.e. the 10.36.10.1 form.
     *
     * @param ip
     * @return
     */
    static IpAddress createIpv4Address(final String ip) {
        assertNotEmpty(ip, "The IP address cannot be null or the empty string");
        // 2 for address family, 4 for the 32bit encoding of the ip and 2 for padding since
        // an AVP needs to have 8 bit boundaries.
        final byte[] buffer = new byte[2 + 4];
        buffer[1] = (byte)0x01;
        IPv4.fromString(buffer, 2, ip);
        return new DefaultIPAddress(true, Buffers.wrap(buffer));
    }

    static IpAddress createIpv4Address(final Buffer ip) {
        assertArgument(Buffers.isNotNullOrEmpty(ip), "The IP address cannot be null or the empty buffer");
        return createIpv4Address(ip.toString());
    }

    String asString();

    boolean isIPv4();

    boolean isIPv6();

    class DefaultIPAddress implements IpAddress {
        private final Buffer value;
        private final boolean isIPv4;

        private DefaultIPAddress(final boolean isIPv4, final Buffer value) {
            this.isIPv4 = isIPv4;
            this.value = value;
        }

        @Override
        public void writeValue(final WritableBuffer buffer) {
            buffer.write(value);
        }

        @Override
        public String asString() {
            return toString();
        }

        @Override
        public boolean isIPv4() {
            return isIPv4;
        }

        @Override
        public boolean isIPv6() {
            return !isIPv4;
        }

        @Override
        public int size() {
            return value.capacity();
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final DefaultIPAddress that = (DefaultIPAddress) o;
            return value.equals(that.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

        @Override
        public String toString() {
            if (isIPv4) {
                return value.toIPv4String(2);
            }

            return value.dumpAsHex();
        }
    }
}
