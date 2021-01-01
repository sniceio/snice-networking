package io.snice.networking.examples.vplmn.fsm.devices.device;

import io.hektor.actors.fsm.FsmActorContextSupport;
import io.hektor.fsm.Context;
import io.snice.buffer.Buffer;
import io.snice.buffer.Buffers;
import io.snice.codecs.codec.Imei;
import io.snice.codecs.codec.gtp.GtpMessage;
import io.snice.codecs.codec.gtp.gtpc.v2.messages.tunnel.CreateSessionRequest;
import io.snice.codecs.codec.gtp.gtpc.v2.messages.tunnel.CreateSessionResponse;
import io.snice.codecs.codec.gtp.gtpc.v2.tliv.Paa;
import io.snice.networking.examples.vplmn.SimCard;
import io.snice.networking.gtp.Bearer;
import io.snice.networking.gtp.PdnSessionContext;

public interface DeviceContext extends Context, FsmActorContextSupport {

    Imei getImei();

    SimCard getSimCard();

    DeviceConfiguration getConfiguration();

    /**
     * Send the given {@link GtpMessage} and if this is a request, its corresponding
     * response will be wrapped and delivered to the FSM that sent it through the
     * event {@link DeviceEvent.GtpResponseEvent}.
     *
     * @param msg
     */
    void send(GtpMessage msg);

    /**
     * Attempt to establish a new bearer.
     *
     * <p>
     * If successful, the FSM will be getting back a {@link DeviceEvent.EpsBearerEstablished} and if not,
     * then
     *
     * @param local             the local {@link Bearer}
     * @param remote            the remote {@link Bearer}
     * @param assignedIpAddress the IP address assigned to this device. Typically, this is conveyed in the {@link Paa}
     *                          (PDN Address Allocation) but it is up to you if you fetch it from the PAA or if
     *                          you just make something up (perhaps because you are testing a PGW and want to
     *                          ignore what it assigned you?)
     * @param localPort the local port that we'll stamp into the packets we'll send over the GTP-U tunnel. Can be
     *                  anything.
     */
    void establishBearer(Bearer local, Bearer remote, Buffer assignedIpAddress, int localPort);

    /**
     * Convenience method where the assigned IP Address is a human readable IP Address, which will
     * be converted into a {@link Buffer} and then {@link #establishBearer(Bearer, Bearer, Buffer, int)}
     * will be called.
     */
    default void establishBearer(final Bearer local, final Bearer remote, final String assignedIpAddress, final int localPort) {
        establishBearer(local, remote, Buffers.wrapAsIPv4(assignedIpAddress), localPort);
    }

    PdnSessionContext createPdnSessionContext(CreateSessionRequest req, CreateSessionResponse resp);

}
