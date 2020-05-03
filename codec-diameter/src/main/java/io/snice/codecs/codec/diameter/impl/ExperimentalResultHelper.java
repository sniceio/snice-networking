package io.snice.codecs.codec.diameter.impl;

import io.snice.codecs.codec.diameter.avp.Avp;
import io.snice.codecs.codec.diameter.avp.AvpMandatory;
import io.snice.codecs.codec.diameter.avp.Vendor;
import io.snice.codecs.codec.diameter.avp.api.ExperimentalResult;
import io.snice.codecs.codec.diameter.avp.api.ExperimentalResultCode;
import io.snice.codecs.codec.diameter.avp.api.VendorId;
import io.snice.codecs.codec.diameter.avp.type.Grouped;
import io.snice.codecs.codec.diameter.avp.type.Unsigned32;

import java.util.List;

import static io.snice.preconditions.PreConditions.assertNotNull;

/**
 * Simple class for mapping {@link ExperimentalResultCode} to a {@link VendorId} and then
 * wrap that in the grouped AVP {@link ExperimentalResult}
 *
 * TODO: should move the vendor id into the ExperimentalResultCode
 *
 */
public class ExperimentalResultHelper {

    public static ExperimentalResult map(final ExperimentalResultCode code) {
        assertNotNull(code);

        final var vendorId = VendorId.of(Unsigned32.of(Vendor.TGPP.getCode()));
        final var grouped = Grouped.of(List.of(vendorId, code));

        final Avp<Grouped> avp = Avp.ofType(Grouped.class)
                .withValue(grouped)
                .withAvpCode(ExperimentalResult.CODE)
                .isMandatory(AvpMandatory.MUST.isMandatory())
                .build();

        return avp.ensure().toExperimentalResult();
    }
}
