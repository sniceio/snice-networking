package io.snice.networking.gtp;

import io.snice.buffer.Buffer;
import io.snice.codecs.codec.gtp.Teid;

public interface EpsBearer {


    Bearer getLocalBearer();

    /**
     * Get the {@link Teid} for the local {@link Bearer}
     * <p>
     * This is just a convenience method for <code> getLocalBearer().getTeid(); </code>
     */
    default Teid getLocalBearerTeid() {
        return getLocalBearer().getTeid();
    }

    Bearer getRemoteBearer();

    default Teid getRemoteBearerTeid() {
        return getRemoteBearer().getTeid();
    }

    void send(String remoteAddress, int remotePort, Buffer data);

    void send(String remoteAddress, int remotePort, String data);

}
