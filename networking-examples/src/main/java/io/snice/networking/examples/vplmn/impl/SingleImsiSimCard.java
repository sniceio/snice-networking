package io.snice.networking.examples.vplmn.impl;

import io.snice.codecs.codec.Iccid;
import io.snice.codecs.codec.Imsi;
import io.snice.codecs.codec.MccMnc;
import io.snice.networking.examples.vplmn.SimCard;

import java.util.List;
import java.util.Optional;

public class SingleImsiSimCard implements SimCard {

    private final Iccid iccid;
    private final Imsi imsi;
    private final List<Imsi> imsis;

    public SingleImsiSimCard(final Iccid iccid, final Imsi imsi) {
        this.iccid = iccid;
        this.imsi = imsi;
        this.imsis = List.of(imsi);
    }

    @Override
    public Iccid getIccid() {
        return iccid;
    }

    @Override
    public List<Imsi> getImsis() {
        return imsis;
    }

    @Override
    public Optional<Imsi> getImsi(final MccMnc mccMnc) {
        return Optional.of(imsi);
    }
}
