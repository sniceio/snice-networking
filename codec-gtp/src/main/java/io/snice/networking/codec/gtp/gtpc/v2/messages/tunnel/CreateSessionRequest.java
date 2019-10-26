package io.snice.networking.codec.gtp.gtpc.v2.messages.tunnel;

import io.snice.networking.codec.gtp.gtpc.v2.Gtp2Header;
import io.snice.networking.codec.gtp.gtpc.v2.Gtp2MessageType;
import io.snice.networking.codec.gtp.gtpc.v2.Gtp2Request;
import io.snice.networking.codec.gtp.gtpc.v2.Impl.Gtp2MesssageBuilder;
import io.snice.networking.codec.gtp.gtpc.v2.messages.tunnel.impl.CreateSessionRequestImpl;

import static io.snice.preconditions.PreConditions.assertNotNull;

public interface CreateSessionRequest extends Gtp2Request {

    static Gtp2MesssageBuilder<CreateSessionRequest> from(final Gtp2Header header) {
        return CreateSessionRequestImpl.from(header);
    }

    @Override
    default Gtp2MessageType getType() {
        return Gtp2MessageType.CREATE_SESSION_REQUEST;
    }

}
