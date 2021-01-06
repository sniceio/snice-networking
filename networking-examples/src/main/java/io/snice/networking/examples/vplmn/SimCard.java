package io.snice.networking.examples.vplmn;

import io.snice.codecs.codec.Iccid;
import io.snice.codecs.codec.Imsi;
import io.snice.codecs.codec.MccMnc;

import java.util.List;
import java.util.Optional;

public interface SimCard {

    /**
     * The ICCID of this SIM Card.
     */
    Iccid getIccid();

    /**
     * Get a list of all IMSIs on this SIM card. Typically, a SIM Card only has a single IMSI.
     */
    List<Imsi> getImsis();

    /**
     * Get the preferred IMSI to use for the given network.
     *
     * @return if able to roam onto this network, an IMSI will be returned, otherwise an empty optional.
     */
    Optional<Imsi> getImsi(MccMnc mccMnc);

}
