package io.snice.networking.codec.diameter.avp.type;

import io.snice.buffer.ReadableBuffer;
import io.snice.networking.codec.diameter.avp.Avp;
import io.snice.networking.codec.diameter.avp.FramedAvp;
import io.snice.networking.codec.diameter.impl.DiameterParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface Grouped extends DiameterType {

    static Grouped parse(final FramedAvp groupedAvp) {
        // for grouped, we expect there to be x number of AVPs present
        // in the data and while we have data left to read, we'll continue
        // to find new ones. Note, we will not go deep, only one "layer"
        // at a time and then you will have to call the ensure method again
        // if you wish to keep diving deeper and deeper into the grouped ones.
        // The reason is that we don't want to spend time on parsing if the
        // user isn't going to use it.

        // perhaps early optimization warning here but trying to limit the
        // array of AVPs to the maximum expected AVPs. By default, the java
        // array list capacity is 10, which is rare to have so let's caluclate
        // the expected maximum AVPs we can get (smallest AVP header size of 8 and 4 bytes of data)
        // if we guesstimate too low, the list will grow anyway so it's fine.
        final int maxNoOfAvps = groupedAvp.getHeader().getLength() / (8 + 4);
        final List<FramedAvp> avps = new ArrayList<>(maxNoOfAvps);
        final ReadableBuffer data = groupedAvp.getData().toReadableBuffer();
        int length = 0;
        while (data.hasReadableBytes()) {
            final FramedAvp avp = DiameterParser.frameRawAvp(data);
            length += avp.getLength();
            avps.add(avp);
        }

        return new DefaultGrouped(length, avps);
    }

    /**
     * <p>
     * Get the {@link FramedAvp} based on its AVP code. Note that this is the "raw" un-parsed
     * AVP and you either have to call {@link FramedAvp#ensure()} if you want to fully ensure it.
     * </p>
     *
     * <p>
     * Note, if there are many {@link Avp}s of the same type, only the first
     * one will be returned.
     * </p>
     *
     * @param code the AVP code
     * @return the first AVP found that has the
     * specified AVP code, or an empty optional if none is found.
     */
    Optional<FramedAvp> getFramedAvp(long code);

    default Optional<FramedAvp> getFramedAvp(final int code) {
        return getFramedAvp((long) code);
    }

    class DefaultGrouped implements Grouped {
        final List<FramedAvp> avps;

        /**
         * The total length of all the AVPs. Needed when calculating the length of this AVP
         */
        private final int length;

        private DefaultGrouped(final int length, final List<FramedAvp> avps) {
            this.length = length;
            this.avps = avps;
        }

        @Override
        public Optional<FramedAvp> getFramedAvp(final long code) {
            return avps.stream().filter(avp -> avp.getCode() == code).findFirst();
        }

        @Override
        public int size() {
            return length;
        }
    }
}
