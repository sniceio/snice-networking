package io.snice.networking.examples.vplmn.fsm.devices.device;

import io.snice.buffer.Buffer;
import io.snice.buffer.Buffers;
import io.snice.codecs.codec.Imei;
import io.snice.codecs.codec.gtp.gtpc.v2.Gtp2Response;
import io.snice.networking.gtp.EpsBearer;
import io.snice.networking.gtp.Transaction;

import static io.snice.preconditions.PreConditions.assertNotNull;

/**
 * Various events for a {@link DeviceFsm}
 * <p>
 * (oh I can't want for Records being a proper feature)
 */
public interface DeviceEvent {

    /**
     * Event for kicking off a new authentication exchange.
     */
    DeviceEvent AUTHENTICATE = new DeviceEvent() {
    };

    /**
     * Event for signalling that the device has already been pre-authorized
     * and as such, we do not need to issue the AIR/AIA "handshake".
     */
    DeviceEvent PRE_AUTHED = new DeviceEvent() {
        @Override
        public String toString() {
            return "PRE_AUTHENTICATED";
        }
    };

    /**
     * Event for signalling that the device has already been pre-attached and as such,
     * we do not need to issue the ULR/ULA "handshake".
     */
    DeviceEvent PRE_ATTACHED = new DeviceEvent() {
        @Override
        public String toString() {
            return "PRE_ATTACHED";
        }
    };

    DeviceEvent INITIATE_SESSION = new DeviceEvent() {
        @Override
        public String toString() {
            return "INITIATE_SESSION";
        }
    };

    enum Service {
        MESSAGING, VOICE, INTERNET;
    }

    /**
     * Indicates that the device is online and what services are available.
     */
    class Online implements DeviceEvent {
        private final Imei imei;

        public static Online fullService(final Imei imei) {
            assertNotNull(imei);
            return new Online(imei);
        }

        private Online(final Imei imei) {
            this.imei = imei;
        }

        /**
         * Check whether we are in limited service mode, which typically means
         * we do not have any "internet" connection.
         */
        public boolean isLimitedService() {
            return false;
        }

        public boolean isFullService() {
            return !isLimitedService();
        }

    }

    class SendDataEvent {
        public final Buffer data;
        public final String remoteIp;
        public final int remotePort;

        public SendDataEvent(final String data, final String remoteIp, final int remotePort) {
            this(Buffers.wrap(data), remoteIp, remotePort);
        }

        public SendDataEvent(final Buffer data, final String remoteIp, final int remotePort) {
            this.data = data;
            this.remoteIp = remoteIp;
            this.remotePort = remotePort;
        }

        @Override
        public String toString() {
            return "SendData [" + data.capacity() + " bytes -> " + remoteIp + ":" + remotePort + "]";
        }
    }

    class EpsBearerEstablished {
        public final EpsBearer bearer;

        public EpsBearerEstablished(final EpsBearer bearer) {
            this.bearer = bearer;
        }
    }

    class GtpResponseEvent {
        public final Transaction transaction;
        public final Gtp2Response response;

        public GtpResponseEvent(final Transaction transaction, final Gtp2Response response) {
            this.transaction = transaction;
            this.response = response;
        }
    }
}

