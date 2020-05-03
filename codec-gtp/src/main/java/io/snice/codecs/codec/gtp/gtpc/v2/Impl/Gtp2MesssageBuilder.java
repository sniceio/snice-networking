package io.snice.codecs.codec.gtp.gtpc.v2.Impl;

import io.snice.buffer.Buffer;
import io.snice.codecs.codec.gtp.gtpc.v2.Gtp2Message;

public interface Gtp2MesssageBuilder<T extends Gtp2Message> {

    Gtp2MesssageBuilder<T> withBody(Buffer body);

    T build();
}
