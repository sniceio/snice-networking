package io.snice.networking.gtp;

import io.snice.codecs.codec.gtp.Teid;
import io.snice.codecs.codec.gtp.gtpc.v2.Gtp2Request;
import io.snice.codecs.codec.gtp.gtpc.v2.Gtp2Response;
import io.snice.codecs.codec.gtp.gtpc.v2.tliv.Paa;
import io.snice.functional.Either;

import java.util.concurrent.CompletionStage;

public interface PdnSession extends Session {

    PdnSessionContext getContext();

}
