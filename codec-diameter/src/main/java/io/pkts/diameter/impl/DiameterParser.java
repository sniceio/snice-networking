package io.pkts.diameter.impl;

import io.pkts.diameter.DiameterHeader;
import io.pkts.diameter.DiameterMessage;
import io.pkts.diameter.DiameterParseException;
import io.pkts.diameter.avp.AuthApplicationId;
import io.pkts.diameter.avp.Avp;
import io.pkts.diameter.avp.AvpHeader;
import io.pkts.diameter.avp.FramedAvp;
import io.pkts.diameter.avp.OriginHost;
import io.pkts.diameter.avp.OriginRealm;
import io.pkts.diameter.avp.VendorId;
import io.pkts.diameter.avp.VendorSpecificApplicationId2;
import io.pkts.diameter.avp.impl.ImmutableAvpHeader;
import io.pkts.diameter.avp.impl.ImmutableFramedAvp;
import io.snice.buffer.Buffer;
import io.snice.buffer.ReadableBuffer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author jonas@jonasborjesson.com
 */
public class DiameterParser {

    public static DiameterMessage frame(final ReadableBuffer buffer) throws DiameterParseException, IOException {
        final int start = buffer.getReaderIndex();
        final DiameterHeader header = frameHeader(buffer);
        // the +20 because we have already consumed 20 bytes for the header but the length as stated
        // in the diameter header actually includes these 20 bytes so...
        if (header.getLength() > buffer.getReadableBytes() + 20) {
            throw new DiameterParseException(20, "Not enough bytes in message to parse out the full message");
        }

        // TODO: we may want to slice out the entire buffer and store that as well
        // for those applications that just write back out to another network connection
        // and doesn't really need to mess with the data. Kind of what I do for SIP.
        // Also, the way this will be read off of the network, we will probably always have
        // the exact buffer we need and perhaps we should just assume that's the case.

        final ReadableBuffer avps = (ReadableBuffer) buffer.readBytes(header.getLength() - 20);
        final List<FramedAvp> list = new ArrayList<>(); // TODO: what's a sensible default?
        while (avps.getReadableBytes() > 0) {
            final int readerIndex = avps.getReaderIndex();
            final FramedAvp avp = FramedAvp.frame(avps);
            list.add(avp);
            final int padding = avp.getPadding();
            if (padding != 0) {
                avps.readBytes(padding);
            }

            // fail safe - if we are not making any progress
            // then we need to bail out.
            if (readerIndex == avps.getReaderIndex()) {
                throw new DiameterParseException(readerIndex, "Seems like we are stuck parsing " +
                        "AVPs for diameter message " + header.getCommandCode() + ". Bailing out");

            }
        }

        final ReadableBuffer entireMsg = (ReadableBuffer) buffer.slice(start, buffer.getReaderIndex());

        return new ImmutableDiameterMessage(entireMsg, header, list);
    }


    public static DiameterHeader frameHeader(final ReadableBuffer buffer) throws DiameterParseException, IOException {
        if (buffer.getReadableBytes() < 20) {
            throw new DiameterParseException(0, "Cannot be a Diameter message because the header is less than 20 bytes");
        }

        final ReadableBuffer header = (ReadableBuffer) buffer.readBytes(20);
        return new ImmutableDiameterHeader(header);
    }

    /**
     * Convenience method for checking if this could indeed by a {@link DiameterMessage}. Use this when
     * you just want to check and not handle the {@link DiameterParseException} that would be thrown as a
     * result of this not being a diameter message.
     * <p>
     * TODO: may actually need a more specific parse exception because right now, you don't konw if
     * it "blew" up because it is not a diameter message or because there is a "real" parse exception.
     *
     * @param buffer
     * @return
     * @throws IOException
     */
    public static boolean couldBeDiameterMessage(final ReadableBuffer buffer) throws IOException {
        final int index = buffer.getReaderIndex();
        try {
            final DiameterHeader header = frameHeader(buffer);
            return header.validate();
        } catch (final DiameterParseException e) {
            return false;
        } finally {
            buffer.setReaderIndex(index);
        }
    }

    public static Avp parseAvp(final FramedAvp raw) {

        // TODO: I believe the way this is compiled will be super fast because it is just a lookup
        // and a jump. Will do some performance test doing this vs map or something else. We will
        // auto generate all of this anyway.
        switch ((int) raw.getCode()) {
            case OriginHost.CODE:
                return OriginHost.parse(raw);
            case OriginRealm.CODE:
                return OriginRealm.parse(raw);
            case VendorSpecificApplicationId2.CODE:
                return VendorSpecificApplicationId2.parse(raw);
            case VendorId.CODE:
                return VendorId.parse(raw);
            case AuthApplicationId.CODE:
                return AuthApplicationId.parse(raw);
            // case ResultCode.CODE:
            // return ResultCode.parse(raw);
            default:
                throw new RuntimeException("AVP " + raw.getCode() + " has not yet been implemented");
        }

    }

    public static FramedAvp frameRawAvp(final ReadableBuffer buffer) throws DiameterParseException {
        final AvpHeader header = frameAvpHeader(buffer);
        final int avpHeaderLength = header.isVendorSpecific() ? 12 : 8;
        final Buffer data = buffer.readBytes(header.getLength() - avpHeaderLength);
        return new ImmutableFramedAvp(header, data);
    }

    public static AvpHeader frameAvpHeader(final ReadableBuffer buffer) throws DiameterParseException {
        if (buffer.getReadableBytes() < 8) {
            throw new DiameterParseException("Unable to read 8 bytes from the buffer, not enough data to parse AVP.");
        }

        // these are the flags and we need to check if the Vendor-ID bit is set and if so we need
        // another 4 bytes for the AVP Header.
        final byte flags = buffer.getByte(buffer.getReaderIndex() + 4);
        final boolean isVendorIdPresent = (flags & 0b10000000) == 0b10000000;
        final Buffer avpHeader = isVendorIdPresent ? buffer.readBytes(12) : buffer.readBytes(8);
        final Optional<Long> vendorId = isVendorIdPresent ? Optional.of(avpHeader.getUnsignedInt(8)) : Optional.empty();
        return new ImmutableAvpHeader(avpHeader, vendorId);

    }

    /**
     * It is quite common in the various diameter headers that the length is stored in 3 octects. This will return
     * an int based on those.
     *
     * @param a
     * @param b
     * @param c
     * @return
     */
    public static int getIntFromThreeOctets(final byte a, final byte b, final byte c) {
        return (a & 0xff) << 16 | (b & 0xff) << 8 | (c & 0xff) << 0;
    }
}
