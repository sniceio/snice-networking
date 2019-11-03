package io.snice.networking.codec.gtp.gtpc.v2.Impl;

import io.snice.buffer.Buffer;
import io.snice.networking.codec.gtp.gtpc.v2.Gtp2Message;
import io.snice.networking.codec.gtp.gtpc.v2.tliv.TypeLengthInstanceValue;

public interface Gtp2MesssageBuilder<T extends Gtp2Message> {

    Gtp2MesssageBuilder<T> withBody(Buffer body);

    T build();
}
