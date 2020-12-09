package io.snice.networking.gtp;

import io.snice.buffer.Buffer;

public interface EpsBearer {

    void send(String remoteAddress, int remotePort, Buffer data);

    void send(String remoteAddress, int remotePort, String data);

}
